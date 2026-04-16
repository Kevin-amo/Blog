package blog.ai.summary.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 生成文章摘要请求参数。
 */
@Data
public class ArticleSummaryDTO {

    @NotBlank(message = "文章标题不能为空")
    private String title;

    @NotBlank(message = "文章内容不能为空")
    private String content;

    @Min(value = 100, message = "摘要长度不能少于 100")
    @Max(value = 300, message = "摘要长度不能超过 300")
    private Integer maxLength = 180;
}
