package blog.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author admin
 */
@Data
public class CategoryUpdateDTO {

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long id;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String name;

    /**
     * 排序值
     */
    @NotNull(message = "排序值不能为空")
    private Integer sort;

    /**
     * 状态：1-启用 0-禁用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;


}
