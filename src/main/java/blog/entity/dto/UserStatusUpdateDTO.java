package blog.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员修改用户状态 DTO
 */
@Data
public class UserStatusUpdateDTO {

    @NotNull(message = "状态不能为空")
    private Integer status;
}
