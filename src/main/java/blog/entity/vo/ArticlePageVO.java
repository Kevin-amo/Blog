package blog.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author admin
 */
@Data
public class ArticlePageVO {

    /**
     * 文章ID
     */
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 封面图
     */
    private String coverImage;

    /**
     * 文章状态：0-草稿 1-已发布
     */
    private Integer status;

    /**
     * 是否置顶：0-否，1-是
     */
    private Integer isTop;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
