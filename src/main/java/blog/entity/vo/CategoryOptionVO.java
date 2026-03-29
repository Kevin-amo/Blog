package blog.entity.vo;

import lombok.Data;

/**
 * @author admin
 *
 * 分类下拉选项
 */
@Data
public class CategoryOptionVO {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

}
