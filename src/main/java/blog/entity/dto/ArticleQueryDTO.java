package blog.entity.dto;

import lombok.Data;

/**
 * @author admin
 * 文章列表查询 DTO
 */
@Data
public class ArticleQueryDTO {

    /**
     * 标题（模糊查询）
     */
    private String title;

    /**
     * 状态：0-草稿，1-已发布
     */
    private Integer status;

    /**
     * 分类ID
     */
    private Long categoryId;

}
