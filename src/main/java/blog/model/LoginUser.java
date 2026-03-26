package blog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author admin
 * 当前登录用户信息
 * 用于从 JWT 中解析后，保存到上下文中
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUser {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

}
