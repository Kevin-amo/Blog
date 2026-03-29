package blog.common.constant;

/**
 * @author admin
 * 用户常量
 */
public final class UserConstants {

    private UserConstants() {
    }

    /**
     * 普通用户
     */
    public static final int ROLE_USER = 0;

    /**
     * 管理员
     */
    public static final int ROLE_ADMIN = 1;

    /**
     * 启用
     */
    public static final int STATUS_ENABLED = 1;

    /**
     * 禁用
     */
    public static final int STATUS_DISABLED = 0;

    /**
     * 管理员重置的默认密码
     */
    public static final String DEFAULT_RESET_PASSWORD = "Blog@123456";
}
