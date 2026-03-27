package blog.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author admin
 * 文章实体类
 */
@Data
public class Article {

    /**
     * 主键
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
     * 创建人ID
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

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    private Integer isDeleted;
}
