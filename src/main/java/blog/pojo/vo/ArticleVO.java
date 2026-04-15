package blog.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author admin
 *
 * 文章视图对象
 */
@Data
public class ArticleVO {

    private Long id;

    private String title;

    private String summary;

    private String content;

    private String coverUrl;

    private Long categoryId;

    private Integer status;

    /**
     * 0 待审核，1 通过，2 驳回
     */
    private Integer auditStatus;

    private Integer isTop;

    private Integer viewCount;

    private Long createBy;

    private LocalDateTime createTime;

    private Long updateBy;

    private LocalDateTime updateTime;
}
