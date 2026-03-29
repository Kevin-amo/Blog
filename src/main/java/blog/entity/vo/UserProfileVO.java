package blog.entity.vo;

import lombok.Data;

/**
 * @author admin
 * 当前登录用户信息。
 */
@Data
public class UserProfileVO {

    /**
     * 用户ID。
     */
    private Long userId;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 昵称。
     */
    private String nickname;

    /**
     * 头像URL。
     */
    private String avatar;
}
