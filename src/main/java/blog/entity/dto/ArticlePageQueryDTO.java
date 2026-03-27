package blog.entity.dto;

import lombok.Data;

/**
 * @author admin
 */
@Data
public class ArticlePageQueryDTO {

    /**
     * 文章标题（模糊查询）
     */
    private String title;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 文章状态：0-草稿 1-已发布
     */
    private Integer status;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;

}
