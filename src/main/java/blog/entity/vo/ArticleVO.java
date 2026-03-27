package blog.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author admin
 * 文章视图对象
 */
@Data
public class ArticleVO {

    /**
     * 文章ID
     */
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 封面地址
     */
    private String coverUrl;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 状态：0-草稿，1-已发布
     */
    private Integer status;

    /**
     * 是否置顶：0-否，1-是
     */
    private Integer isTop;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 作者ID
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    private Long updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
