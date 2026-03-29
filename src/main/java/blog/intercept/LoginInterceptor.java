package blog.intercept;

import blog.model.LoginUser;
import blog.util.JwtUtil;
import blog.util.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author admin
 * 登录拦截器
 * 功能：
 * 1. 拦截需要登录的请求
 * 2. 从请求头中获取 token
 * 3. 校验 token 是否合法
 * 4. 解析当前登录用户信息
 * 5. 将用户信息保存到 UserContext 中
 */
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 从请求头中获取 Authorization
        String authHeader = request.getHeader("Authorization");

        // 判断请求头是否存在，且是否以 Bearer 开头
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 设置响应编码和内容类型
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");

            // 返回 401 未登录
            response.getWriter().write("""
                    {"code":401,"message":"请先登录","data":null}
                    """);
            return false;
        }

        // 截取真正的 token 内容
        String token = authHeader.substring(7);

        try {
            // 解析 token
            Claims claims = jwtUtil.parseToken(token);

            // 从 token 中获取用户信息
            Long userId = Long.valueOf(claims.get("userId").toString());
            String username = claims.get("username", String.class);

            // 封装当前登录用户
            LoginUser loginUser = new LoginUser(userId, username);

            // 保存到线程上下文中，后续 Controller / Service 可以直接取
            UserContext.setUser(loginUser);

            // 放行请求
            return true;
        } catch (Exception e) {
            // token 非法、过期、签名错误，都会走这里
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("""
                    {"code":401,"message":"token无效或已过期","data":null}
                    """);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

}
