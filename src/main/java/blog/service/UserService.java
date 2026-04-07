package blog.service;

import blog.common.Result.PageResult;
import blog.pojo.dto.UserAdminPageQueryDTO;
import blog.pojo.dto.UserPasswordUpdateDTO;
import blog.pojo.dto.UserProfileUpdateDTO;
import blog.pojo.dto.UserRegisterDTO;
import blog.pojo.vo.UserAdminPageVO;
import blog.pojo.vo.UserProfileVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务
 */
public interface UserService {

    void register(UserRegisterDTO registerDTO);

    UserProfileVO me();

    String uploadAvatar(MultipartFile file);

    void updateProfile(UserProfileUpdateDTO dto);

    void updatePassword(UserPasswordUpdateDTO dto);

    PageResult<UserAdminPageVO> pageUsers(UserAdminPageQueryDTO queryDTO);

    void updateUserStatus(Long userId, Integer status);

    void resetUserPassword(Long userId);

    void deleteUser(Long userId);
}
