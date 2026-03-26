package blog.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author admin
 * JWT 工具类
 * 负责：
 * 1. 生成 token
 * 2. 解析 token
 * 3. 读取 token 中的用户信息
 */
@Component
public class JwtUtil {

    /**
     * 从配置文件中读取 JWT 密钥
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * 从配置文件中读取 JWT 过期时间
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username) {
        // 获取当前时间
        Date now = new Date();

        // token过期时间
        Date expireTime = new Date(now.getTime() + expiration);

        // 自定义载荷数据
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                // 设置自定义数据
                .setClaims(claims)
                // 签发时间
                .setIssuedAt(now)
                // 过期时间
                .setExpiration(expireTime)
                .signWith(getSecretKey())
                // 生成token
                .compact();
    }

    /**
     * 解析 JWT
     * 如果 token 非法、过期、签名错误，这里会抛异常
     * @param token token 字符串
     * @return claims 载荷内容
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUserId(String token) {
        return Long.valueOf(parseToken(token).get("userId").toString());
    }

    public String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }
}
