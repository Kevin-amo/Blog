package blog.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author admin
 *
 * 修改个人资料 DTO
 */
@Data
public class UserProfileUpdateDTO {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;
}
