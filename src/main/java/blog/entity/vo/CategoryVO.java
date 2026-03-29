package blog.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author admin
 *
 * 分类返回 VO
 */
@Data
public class CategoryVO {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 排序值
     */
    private Integer sort;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
