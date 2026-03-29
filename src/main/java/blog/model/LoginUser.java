package blog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUser {

    private Long userId;

    private String username;

    /**
     * 0 普通用户，1 管理员
     */
    private Integer role;
}
