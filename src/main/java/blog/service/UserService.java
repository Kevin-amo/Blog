package blog.service;

import blog.common.Result.PageResult;
import blog.entity.dto.UserAdminPageQueryDTO;
import blog.entity.dto.UserPasswordUpdateDTO;
import blog.entity.dto.UserProfileUpdateDTO;
import blog.entity.dto.UserRegisterDTO;
import blog.entity.vo.UserAdminPageVO;
import blog.entity.vo.UserProfileVO;
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
