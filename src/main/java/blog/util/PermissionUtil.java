package blog.util;

import blog.common.constant.UserConstants;
import blog.common.exception.ForbiddenException;
import blog.model.LoginUser;

/**
 * @author admin
 *
 * 权限工具
 */
public final class PermissionUtil {

    private PermissionUtil() {
    }

    public static LoginUser requireLogin() {
        LoginUser loginUser = UserContext.getUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new RuntimeException("请先登录");
        }
        return loginUser;
    }

    public static void requireAdmin() {
        LoginUser loginUser = requireLogin();
        if (!Integer.valueOf(UserConstants.ROLE_ADMIN).equals(loginUser.getRole())) {
            throw new ForbiddenException("无权访问管理员功能");
        }
    }

    public static void requireUser() {
        LoginUser loginUser = requireLogin();
        if (!Integer.valueOf(UserConstants.ROLE_USER).equals(loginUser.getRole())) {
            throw new ForbiddenException("无权访问普通用户功能");
        }
    }
}
