package blog.intercept;

import blog.model.LoginUser;
import blog.common.constant.UserConstants;
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
 *
 * 登录拦截器
 */
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            write401(response, "请先登录");
            return false;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtil.parseToken(token);
            Long userId = Long.valueOf(claims.get("userId").toString());
            String username = claims.get("username", String.class);
            Integer role = claims.get("role", Integer.class);
            if (role == null) {
                role = UserConstants.ROLE_USER;
            }
            UserContext.setUser(new LoginUser(userId, username, role));
            return true;
        } catch (Exception e) {
            write401(response, "token无效或已过期");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private void write401(HttpServletResponse response, String message) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
    }
}
