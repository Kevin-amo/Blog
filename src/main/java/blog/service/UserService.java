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
 * 用户服务接口。
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
     * @return 当前用户资料
     */
    UserProfileVO me();

    /**
     * 上传当前用户头像。
     *
     * @param file 头像文件
     * @return 头像访问地址
     */
    String uploadAvatar(MultipartFile file);

    /**
     * 修改当前用户资料。
     *
     * @param dto 资料修改参数
     */
    void updateProfile(UserProfileUpdateDTO dto);

    /**
     * 修改当前用户密码。
     *
     * @param dto 密码修改参数
     */
    void updatePassword(UserPasswordUpdateDTO dto);

    /**
     * 后台分页查询用户。
     *
     * @param queryDTO 查询参数
     * @return 用户分页结果
     */
    PageResult<UserAdminPageVO> pageUsers(UserAdminPageQueryDTO queryDTO);

    /**
     * 修改用户状态。
     *
     * @param userId 用户ID
     * @param status 用户状态
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 重置指定用户密码。
     *
     * @param userId 用户ID
     */
    void resetUserPassword(Long userId);

    /**
     * 删除指定用户。
     *
     * @param userId 用户ID
     */
    void deleteUser(Long userId);
}
