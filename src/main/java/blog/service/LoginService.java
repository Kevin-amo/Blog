package blog.service;

import blog.pojo.dto.LoginDTO;

import java.util.Map;

/**
 * @author admin
 * 认证业务接口
 */
public interface LoginService {

    Map<String, Object> login(LoginDTO loginDTO);

}
