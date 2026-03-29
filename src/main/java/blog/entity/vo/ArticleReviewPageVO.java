package blog.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员审核分页项
 */
@Data
public class ArticleReviewPageVO {

    private Long id;

    private String title;

    private String summary;

    private String content;

    private Integer status;

    private Integer auditStatus;

    private Long createBy;

    private String authorUsername;

    private String authorNickname;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
