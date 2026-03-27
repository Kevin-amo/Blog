package blog.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author admin
 * 新增文章DTO
 */
@Data
public class ArticleAddDTO {

    /**
     * 文章标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 文章内容
     */
    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 封面地址
     */
    private String coverUrl;

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
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
