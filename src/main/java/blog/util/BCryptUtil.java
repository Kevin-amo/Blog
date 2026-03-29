package blog.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author admin
 * 密码工具类
 * 功能：
 * 1. 对明文密码进行加密
 * 2. 校验明文密码和加密密码是否一致
 */
public class BCryptUtil {

    /**
     * BCrypt 密码加密器
     */
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    /**
     * 对明文密码进行加密
     * @param rawPassword 明文密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    /**
     * 校验明文密码与加密后的密码是否匹配
     * @param rawPassword 明文密码
     * @param encode 加密后密码
     * @return true-匹配  false-不匹配
     */
    public static Boolean match(String rawPassword, String encode) {
        return PASSWORD_ENCODER.matches(rawPassword, encode);
    }

}
