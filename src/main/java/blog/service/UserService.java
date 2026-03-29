package blog.service;

import blog.entity.dto.UserRegisterDTO;
import blog.entity.vo.UserProfileVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author admin
 */
public interface UserService {

    /**
     * 用户注册。
     *
     * @param registerDTO 注册参数
     */
    void register(UserRegisterDTO registerDTO);

    /**
     * 获取当前登录用户信息。
     *
     * @return 用户信息
     */
    UserProfileVO me();

    /**
     * 上传并更新当前用户头像。
     *
     * @param file 头像文件
     * @return 新头像URL
     */
    String uploadAvatar(MultipartFile file);

}
