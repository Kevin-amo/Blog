package blog.service.impl;

import blog.entity.po.User;
import blog.entity.dto.LoginDTO;
import blog.mapper.UserMapper;
import blog.service.LoginService;
import blog.util.BCryptUtil;
import blog.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author admin
 */
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final UserMapper userMapper;

    private final JwtUtil jwtUtil;

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        // 根据用户名查询用户密码
        User user = userMapper.selectByUsername(loginDTO.getUsername());

        // 用户不存在
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 用户已被封禁
        if (user.getStatus() == 0) {
            throw new RuntimeException("该用户已被封禁！");
        }
        // 密码核对
        if (!BCryptUtil.match(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误，请重试！");
        }

        // 登录成功, 生成jwt
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("tokenType", "Bearer");
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        return result;
    }
}
