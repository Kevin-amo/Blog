package blog.entity.dto;

import lombok.Data;

/**
 * @author admin
 *
 * 文章分页查询 DTO
 */
@Data
public class ArticlePageQueryDTO {

    private String title;

    private Long categoryId;

    /**
     * 0 草稿，1 提交发布
     */
    private Integer status;

    /**
     * 0 待审，1 通过，2 驳回
     */
    private Integer auditStatus;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
