package blog.ai.summary;

import blog.ai.summary.dto.ArticleSummaryDTO;
import blog.ai.summary.prompt.ArticleSummaryPrompt;
import blog.util.PermissionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.function.Consumer;

/**
 * 文章 AI 摘要服务实现。
 */
@Service
@RequiredArgsConstructor
public class ArticleSummaryServiceImpl implements ArticleSummaryService {

    /**
     * 送入模型的正文最大长度，避免提示词过长影响响应速度和生成稳定性。
     */
    private static final int CONTENT_MAX_LENGTH = 6000;

    private final ChatClient chatClient;

    /**
     * 以流式方式生成摘要，并将模型返回的文本片段持续回调给上层。
     */
    @Override
    public void summaryStream(ArticleSummaryDTO generateDTO, Consumer<String> chunkConsumer) {
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

    /**
     * 根据标题、正文和期望摘要长度组装最终提示词。
     */
    private String buildPrompt(ArticleSummaryDTO generateDTO) {
        String title = generateDTO.getTitle().trim();
        String content = normalizeContent(generateDTO.getContent());
        Integer maxLength = generateDTO.getMaxLength() == null ? 180 : generateDTO.getMaxLength();

        return ArticleSummaryPrompt.TEMPLATE.formatted(
                maxLength,
                title,
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
}
