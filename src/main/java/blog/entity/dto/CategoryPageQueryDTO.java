package blog.entity.dto;

import lombok.Data;

/**
 * @author admin
 *
 * @author admin
 */
@Data
public class CategoryPageQueryDTO {

    /**
     * 分类名称（模糊查询）
     */
    private String name;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;

}
