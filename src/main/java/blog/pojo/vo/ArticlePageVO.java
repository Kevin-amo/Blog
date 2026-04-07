package blog.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author admin
 *
 * 文章分页项
 */
@Data
public class ArticlePageVO {

    private Long id;

    private String title;

    private Long categoryId;

    private String categoryName;

    private String summary;

    private String coverImage;

    private Integer status;

    /**
     * 0 待审核，1 通过，2 驳回
     */
    private Integer auditStatus;

    private Integer isTop;

    private LocalDateTime createTime;
}
