package blog.service;

import blog.pojo.dto.LoginDTO;

import java.util.Map;

/**
 * 登录认证服务接口。
 */
public interface LoginService {

    /**
     * 用户登录并返回认证信息。
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    Map<String, Object> login(LoginDTO loginDTO);

}
