package blog.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员用户分页项
 */
@Data
public class UserAdminPageVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private Integer role;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
