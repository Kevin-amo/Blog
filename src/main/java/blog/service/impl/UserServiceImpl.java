package blog.service.impl;

import blog.entity.dto.UserRegisterDTO;
import blog.entity.po.User;
import blog.mapper.UserMapper;
import blog.service.UserService;
import blog.util.BCryptUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author admin
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    /**
     * 用户注册
     *
     * @param registerDTO 注册参数
     */
    @Override
    public void register(UserRegisterDTO registerDTO) {
        // 判断两次输入密码是否一致
        String password = registerDTO.getPassword();
        String rePassword = registerDTO.getRePassword();
        if (!password.equals(rePassword)) {
            throw new RuntimeException("两次输入密码不一致，请重新输入！");
        }
        // 判断用户名是否已存在
        User dbUser = userMapper.selectByUsername(registerDTO.getUsername());
        if (dbUser != null) {
            throw new RuntimeException("用户名已存在！");
        }
        // 封装用户对象
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        // 调用工具类进行密码加密
        user.setPassword(BCryptUtil.encode(registerDTO.getPassword()));
        // 昵称为空时，使用默认用户名
        if (StringUtils.hasText(registerDTO.getNickname())) {
            user.setNickname(registerDTO.getNickname());
        } else {
            user.setNickname(registerDTO.getUsername());
        }
        // 保存到数据库
        userMapper.insert(user);
    }

}
