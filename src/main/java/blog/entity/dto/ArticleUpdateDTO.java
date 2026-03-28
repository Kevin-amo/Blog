package blog.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author admin
 * 更新文章 DTO
 */
@Data
public class ArticleUpdateDTO {

    /**
     * 文章ID
     */
    @NotNull(message = "文章ID不能为空")
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 封面地址
     */
    private String coverUrl;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 状态：0-草稿，1-已发布
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 是否置顶：0-否，1-是
     */
    private Integer isTop;
}
