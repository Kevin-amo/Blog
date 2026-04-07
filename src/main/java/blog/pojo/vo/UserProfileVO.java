package blog.pojo.vo;

import lombok.Data;

/**
 * @author admin
 *
 * 当前登录用户信息
 */
@Data
public class UserProfileVO {

    private Long userId;

    private String username;

    private String nickname;

    private String avatar;

    /**
     * 0 普通用户，1 管理员
     */
    private Integer role;
}
