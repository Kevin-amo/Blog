package blog.util;

import blog.model.LoginUser;

/**
 * @author admin
 */
public class UserContext {

    /**
     * ThreadLocal 用于在当前线程中保存用户信息
     */
    private static final ThreadLocal<LoginUser> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置当前登录用户
     */
    public static void setUser(LoginUser loginUser) {
        THREAD_LOCAL.set(loginUser);
    }

    /**
     * 获取当前登录用户
     */
    public static LoginUser getUser() {
        return THREAD_LOCAL.get();
    }

    /**
     * 清除当前线程中的用户信息
     * 非常重要，防止线程复用导致数据串掉
     */
    public static void clear() {
        THREAD_LOCAL.remove();
    }

}
