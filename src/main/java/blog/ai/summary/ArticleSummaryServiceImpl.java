package blog.ai.summary;

import blog.ai.summary.dto.ArticleSummaryGenerateDTO;
import blog.ai.summary.prompt.ArticleSummaryPrompt;
import blog.util.PermissionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.function.Consumer;

/**
 * 文章 AI 总结服务实现。
 */
@Service
@RequiredArgsConstructor
public class ArticleSummaryServiceImpl implements ArticleSummaryService {

    private static final int CONTENT_MAX_LENGTH = 6000;

    private final ChatClient chatClient;

    @Override
    public String generateSummary(ArticleSummaryGenerateDTO generateDTO) {
        PermissionUtil.requireUser();
        String prompt = buildPrompt(generateDTO);
        int maxLength = generateDTO.getMaxLength() == null ? 150 : generateDTO.getMaxLength();

        String summary;
        try {
            summary = chatClient.prompt(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            throw new RuntimeException("AI 摘要生成失败，请稍后重试");
        }

        if (!StringUtils.hasText(summary)) {
            throw new RuntimeException("AI 未返回有效摘要，请稍后重试");
        }

        return normalizeSummary(summary, maxLength);
    }

    @Override
    public void generateSummaryStream(ArticleSummaryGenerateDTO generateDTO, Consumer<String> chunkConsumer) {
        PermissionUtil.requireUser();
        String prompt = buildPrompt(generateDTO);

        try {
            chatClient.prompt(prompt)
                    .stream()
                    .content()
                    .doOnNext(chunk -> {
                        if (StringUtils.hasText(chunk)) {
                            chunkConsumer.accept(chunk);
                        }
                    })
                    .blockLast();
        } catch (Exception e) {
            throw new RuntimeException("AI 摘要生成失败，请稍后重试");
        }
    }

    private String buildPrompt(ArticleSummaryGenerateDTO generateDTO) {
        String title = generateDTO.getTitle().trim();
        String content = normalizeContent(generateDTO.getContent());
        Integer maxLength = generateDTO.getMaxLength() == null ? 150 : generateDTO.getMaxLength();

        return ArticleSummaryPrompt.TEMPLATE.formatted(
                maxLength,
                title,
                truncateContent(content)
        );
    }

    private String truncateContent(String content) {
        if (content.length() <= CONTENT_MAX_LENGTH) {
            return content;
        }
        return content.substring(0, CONTENT_MAX_LENGTH);
    }

    private String normalizeContent(String content) {
        return content == null ? "" : content.trim().replace("\r\n", "\n");
    }

    private String normalizeSummary(String summary, int maxLength) {
        String normalized = summary.trim()
                .replace("\r", "")
                .replace("\n", " ");

        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength).trim();
    }
}
