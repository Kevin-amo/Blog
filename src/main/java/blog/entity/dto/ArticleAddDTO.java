package blog.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新增文章 DTO
 */
@Data
public class ArticleAddDTO {

    private String title;

    private String summary;

    private String content;

    private String coverUrl;

    private Long categoryId;

    /**
     * 0 草稿，1 提交发布
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    private Integer isTop;

    /**
     * 仅用于展示/扩展，实际写入由服务端根据发布动作自动计算
     */
    private Integer auditStatus;
}
