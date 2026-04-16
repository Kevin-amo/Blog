package blog.ai.summary;

import blog.ai.summary.dto.ArticleSummaryDTO;

import java.util.function.Consumer;

/**
 * 文章 AI 摘要服务。
 */
public interface ArticleSummaryService {

    /**
     * 流式生成文章摘要。
     *
     * @param generateDTO 摘要生成参数
     * @param chunkConsumer 摘要分片消费器
     */
    void summaryStream(ArticleSummaryDTO generateDTO, Consumer<String> chunkConsumer);
}
