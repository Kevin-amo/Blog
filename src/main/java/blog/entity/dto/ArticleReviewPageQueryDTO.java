package blog.entity.dto;

import lombok.Data;

/**
 * 管理员审核分页查询 DTO
 */
@Data
public class ArticleReviewPageQueryDTO {

    private String title;

    /**
     * 0 待审，1 通过，2 驳回
     */
    private Integer auditStatus;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
