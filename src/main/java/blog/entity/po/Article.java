package blog.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author admin
 *
 * 文章实体
 */
@Data
public class Article {

    private Long id;

    private String title;

    private String summary;

    private String content;

    private String coverUrl;

    private Long categoryId;

    /**
     * 0 草稿，1 已提交发布
     */
    private Integer status;

    /**
     * 0 待审核，1 通过，2 驳回
     */
    private Integer auditStatus;

    private Integer isTop;

    private Integer viewCount;

    private Integer commentCount;

    private Long createBy;

    private LocalDateTime createTime;

    private Long updateBy;

    private LocalDateTime updateTime;

    private Integer isDeleted;
}
