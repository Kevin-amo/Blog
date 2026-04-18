package blog.ai.summary;

import blog.ai.summary.dto.ArticleSummaryDTO;
import blog.ai.summary.prompt.ArticleSummaryPrompt;
import blog.mapper.ArticleMapper;
import blog.model.LoginUser;
import blog.pojo.po.Article;
import blog.util.PermissionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 文章 AI 摘要服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleSummaryServiceImpl implements ArticleSummaryService {

    /**
     * 送入模型的正文最大长度，避免提示词过长影响响应速度和生成稳定性。
     */
    private static final int CONTENT_MAX_LENGTH = 6000;

    /**
     * 摘要结果缓存 key 前缀。
     */
    private static final String CACHE_KEY_PREFIX = "blog:ai:summary:cache:";

    /**
     * 摘要生成分布式锁 key 前缀。
     */
    private static final String LOCK_KEY_PREFIX = "blog:ai:summary:lock:";

    /**
     * 用户维度限流 key 前缀。
     */
    private static final String USER_RATE_KEY_PREFIX = "blog:ai:summary:rate:user:";

    /**
     * IP 维度限流 key 前缀。
     */
    private static final String IP_RATE_KEY_PREFIX = "blog:ai:summary:rate:ip:";

    /**
     * 固定窗口限流脚本：计数自增，并在首次命中时设置过期时间。
     */
    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>(
            "local current = redis.call('INCR', KEYS[1]); " +
                    "if current == 1 then redis.call('PEXPIRE', KEYS[1], ARGV[1]); end; " +
                    "return current;",
            Long.class
    );

    /**
     * 安全释放分布式锁：只有锁值匹配时才删除，避免误删其他请求的锁。
     */
    private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('GET', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('DEL', KEYS[1]); " +
                    "else return 0; end",
            Long.class
    );

    private final ChatClient chatClient;

    private final ArticleMapper articleMapper;

    private final ArticleSummaryProperties articleSummaryProperties;

    /**
     * Redis 访问入口，用于摘要缓存、限流计数和分布式锁操作。
     */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 以流式方式生成摘要，并将模型返回的文本片段持续回调给上层。
     */
    @Override
    public void summaryStream(ArticleSummaryDTO generateDTO, String clientIp, Consumer<String> chunkConsumer) {
        LoginUser loginUser = PermissionUtil.requireLogin();
        enforceRateLimit(loginUser.getUserId(), clientIp);

        Article article = loadPublishedArticle(generateDTO.getArticleId());
        String normalizedContent = normalizeContent(article.getContent());
        Integer maxLength = resolveMaxLength(generateDTO.getMaxLength());
        String key = buildKey(article.getId(), article.getTitle(), normalizedContent, maxLength);
        String cacheKey = CACHE_KEY_PREFIX + key;

        String cachedSummary = getCachedSummary(cacheKey);
        // 先查 Redis 缓存，命中后直接返回，避免重复调用模型。
        if (StringUtils.hasText(cachedSummary)) {
            chunkConsumer.accept(cachedSummary);
            return;
        }

        String lockKey = LOCK_KEY_PREFIX + key;
        String lockValue = UUID.randomUUID().toString();
        // 尝试抢占 Redis 分布式锁，只有拿到锁的请求才负责生成摘要。
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, articleSummaryProperties.getLockTtl());

        if (Boolean.TRUE.equals(locked)) {
            try {
                // 拿到锁后再查一次缓存，避免其他请求刚好已经写入结果。
                cachedSummary = getCachedSummary(cacheKey);
                if (StringUtils.hasText(cachedSummary)) {
                    chunkConsumer.accept(cachedSummary);
                    return;
                }

                String summary = generateSummary(article.getTitle(), normalizedContent, maxLength, chunkConsumer);
                // 生成成功后写入 Redis 缓存，后续相同请求可直接复用结果。
                stringRedisTemplate.opsForValue().set(cacheKey, summary, articleSummaryProperties.getCacheTtl());
                return;
            } finally {
                releaseLock(lockKey, lockValue);
            }
        }

        // 未拿到锁时等待  首个请求把结果写入 Redis 缓存。
        String waitedSummary = waitForSummary(cacheKey);
        if (StringUtils.hasText(waitedSummary)) {
            chunkConsumer.accept(waitedSummary);
            return;
        }

        throw new RuntimeException("摘要正在生成中，请稍后重试");
    }

    private String generateSummary(String title, String content, Integer maxLength, Consumer<String> chunkConsumer) {
        String prompt = buildPrompt(title, content, maxLength);
        StringBuilder summaryBuilder = new StringBuilder();

        try {
            chatClient.prompt(prompt)
                    .stream()
                    .content()
                    .doOnNext(chunk -> {
                        if (StringUtils.hasText(chunk)) {
                            summaryBuilder.append(chunk);
                            chunkConsumer.accept(chunk);
                        }
                    })
                    .blockLast();

            String summary = normalizeSummary(summaryBuilder.toString(), maxLength);
            if (!StringUtils.hasText(summary)) {
                throw new RuntimeException("AI 摘要生成失败，请稍后重试");
            }
            return summary;
        } catch (Exception e) {
            log.error("AI summary generation failed, title='{}', maxLength={}, contentLength={}",
                    title,
                    maxLength,
                    content == null ? 0 : content.length(),
                    e);
            throw new RuntimeException("AI 摘要生成失败，请稍后重试", e);
        }
    }

    /**
     * 根据标题、正文和期望摘要长度组装最终提示词。
     */
    private String buildPrompt(String title, String content, Integer maxLength) {
        return ArticleSummaryPrompt.TEMPLATE.formatted(
                maxLength,
                title.trim(),
                truncateContent(content)
        );
    }

    /**
     * 对正文做截断，避免一次性传入过长内容。
     */
    private String truncateContent(String content) {
        if (content.length() <= CONTENT_MAX_LENGTH) {
            return content;
        }
        return content.substring(0, CONTENT_MAX_LENGTH);
    }

    /**
     * 统一处理空值、首尾空白和换行符格式，减少提示词中的噪声。
     */
    private String normalizeContent(String content) {
        return content == null ? "" : content.trim().replace("\r\n", "\n");
    }

    private String normalizeSummary(String summary, Integer maxLength) {
        String normalized = summary == null ? "" : summary.replace("\r", "")
                .replaceAll("\n+", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (!StringUtils.hasText(normalized)) {
            return normalized;
        }
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength).trim();
    }

    private Article loadPublishedArticle(Long articleId) {
        Article article = articleMapper.selectPublishedById(articleId);
        if (article == null) {
            throw new RuntimeException("文章不存在或暂未发布");
        }
        if (!StringUtils.hasText(article.getTitle()) || !StringUtils.hasText(article.getContent())) {
            throw new RuntimeException("文章内容不足，暂时无法生成摘要");
        }
        return article;
    }

    private Integer resolveMaxLength(Integer maxLength) {
        return maxLength == null ? 180 : maxLength;
    }

    /**
     * 先执行用户/IP 双维度限流，避免高频请求持续打到模型和缓存层。
     */
    private void enforceRateLimit(Long userId, String clientIp) {
        checkWindow(USER_RATE_KEY_PREFIX + userId,
                articleSummaryProperties.getUserWindow(),
                articleSummaryProperties.getUserWindowMaxRequests(),
                "操作过于频繁，请稍后再试");

        if (StringUtils.hasText(clientIp)) {
            checkWindow(IP_RATE_KEY_PREFIX + clientIp,
                    articleSummaryProperties.getIpWindow(),
                    articleSummaryProperties.getIpWindowMaxRequests(),
                    "当前请求过于频繁，请稍后再试");
        }
    }

    /**
     * 执行单个 Redis 固定窗口限流检查；超过阈值时直接拒绝本次请求。
     */
    private void checkWindow(String key, Duration window, int maxRequests, String message) {
        if (maxRequests <= 0 || window == null || window.isZero() || window.isNegative()) {
            return;
        }

        Long current = stringRedisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                List.of(key),
                String.valueOf(window.toMillis())
        );

        if (current != null && current > maxRequests) {
            throw new RuntimeException(message);
        }
    }

    /**
     * 从 Redis 读取已生成好的摘要缓存；空值统一按未命中处理。
     */
    private String getCachedSummary(String cacheKey) {
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        return StringUtils.hasText(cached) ? cached.trim() : "";
    }

    /**
     * 当前请求未抢到锁时，短轮询等待其他请求写入 Redis 缓存结果。
     */
    private String waitForSummary(String cacheKey) {
        Duration waitTimeout = articleSummaryProperties.getWaitTimeout();
        Duration pollInterval = articleSummaryProperties.getWaitPollInterval();
        long deadline = System.currentTimeMillis() + waitTimeout.toMillis();

        while (System.currentTimeMillis() < deadline) {
            String cached = getCachedSummary(cacheKey);
            if (StringUtils.hasText(cached)) {
                return cached;
            }
            sleepQuietly(pollInterval);
        }
        return "";
    }

    private void sleepQuietly(Duration duration) {
        try {
            Thread.sleep(Math.max(duration.toMillis(), 50L));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("等待摘要结果时被中断", e);
        }
    }

    /**
     * 使用 Lua 脚本按锁值释放 Redis 锁，避免并发场景下误删他人的锁。
     */
    private void releaseLock(String lockKey, String lockValue) {
        try {
            stringRedisTemplate.execute(RELEASE_LOCK_SCRIPT, List.of(lockKey), lockValue);
        } catch (Exception e) {
            log.warn("Release summary lock failed, lockKey={}", lockKey, e);
        }
    }

    private String buildKey(Long articleId, String title, String content, Integer maxLength) {
        return articleId + ":" + maxLength + ":" + sha256Hex(title + "\n" + truncateContent(content));
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not supported", e);
        }
    }
}