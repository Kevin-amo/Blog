package blog.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author admin
 *
 * 用户实体
 */
@Data
public class User {

    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String avatar;

    /**
     * 0 普通用户，1 管理员
     */
    private Integer role;

    /**
     * 1 启用，0 禁用
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
