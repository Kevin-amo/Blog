package blog.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author admin
 *
 * 文章分类实体类
 */
@Data
public class Category {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 排序值，越小越靠前
     */
    private Integer sort;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    private Integer isDeleted;
}