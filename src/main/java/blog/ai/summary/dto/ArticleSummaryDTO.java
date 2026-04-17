package blog.ai.summary.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 生成文章摘要请求参数。
 */
@Data
public class ArticleSummaryDTO {

    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    @Min(value = 100, message = "摘要长度不能少于 100")
    @Max(value = 300, message = "摘要长度不能超过 300")
    private Integer maxLength = 180;
}