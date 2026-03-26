package blog.service;

import blog.entity.dto.LoginDTO;

import java.util.Map;

/**
 * @author admin
 * 认证业务接口
 */
public interface AuthService {

    Map<String, Object> login(LoginDTO loginDTO);

}
