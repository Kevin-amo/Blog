package blog.ai.summary;

import blog.ai.summary.dto.ArticleSummaryGenerateDTO;

import java.util.function.Consumer;

/**
 * 文章 AI 摘要服务。
 */
public interface ArticleSummaryService {

    /**
     * 生成文章摘要。
     *
     * @param generateDTO 摘要生成参数
     * @return 摘要文本
     */
    String generateSummary(ArticleSummaryGenerateDTO generateDTO);

    /**
     * 流式生成文章摘要。
     *
     * @param generateDTO 摘要生成参数
     * @param chunkConsumer 摘要分片消费器
     */
    void generateSummaryStream(ArticleSummaryGenerateDTO generateDTO, Consumer<String> chunkConsumer);
}
