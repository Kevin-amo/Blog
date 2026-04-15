package blog.service.impl;

import blog.common.constant.UserConstants;
import blog.pojo.dto.LoginDTO;
import blog.pojo.po.User;
import blog.mapper.UserMapper;
import blog.service.LoginService;
import blog.util.BCryptUtil;
import blog.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录认证服务实现类。
 */
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    /**
     * 校验账号密码并生成登录令牌。
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        User user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (Integer.valueOf(UserConstants.STATUS_DISABLED).equals(user.getStatus())) {
            throw new RuntimeException("该用户已被禁用");
        }
        if (!BCryptUtil.match(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误，请重试");
        }

        Integer role = user.getRole() == null ? UserConstants.ROLE_USER : user.getRole();
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("tokenType", "Bearer");
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("role", role);
        return result;
    }
}
