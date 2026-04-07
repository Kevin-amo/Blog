package blog.pojo.dto;

import lombok.Data;

/**
 * @author admin
 *
 * 管理员用户分页查询 DTO
 */
@Data
public class UserAdminPageQueryDTO {

    private String username;

    private String nickname;

    /**
     * 1 启用，0 禁用
     */
    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
