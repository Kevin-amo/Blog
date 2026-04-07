package blog.pojo.dto;

import lombok.Data;

/**
 * @author admin
 *
 * 文章列表查询 DTO
 */
@Data
public class ArticleQueryDTO {

    private String title;

    /**
     * 0 草稿，1 提交发布
     */
    private Integer status;

    /**
     * 0 待审，1 通过，2 驳回
     */
    private Integer auditStatus;

    private Long categoryId;
}
