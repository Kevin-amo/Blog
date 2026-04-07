package blog.service.impl;

import blog.common.Result.PageResult;
import blog.common.constant.UserConstants;
import blog.pojo.dto.UserAdminPageQueryDTO;
import blog.pojo.dto.UserPasswordUpdateDTO;
import blog.pojo.dto.UserProfileUpdateDTO;
import blog.pojo.dto.UserRegisterDTO;
import blog.pojo.po.User;
import blog.pojo.vo.UserAdminPageVO;
import blog.pojo.vo.UserProfileVO;
import blog.mapper.ArticleMapper;
import blog.mapper.UserMapper;
import blog.model.LoginUser;
import blog.service.OssService;
import blog.service.UserService;
import blog.util.BCryptUtil;
import blog.util.PermissionUtil;
import blog.util.UserContext;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author admin
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final OssService ossService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterDTO registerDTO) {
        if (!registerDTO.getPassword().equals(registerDTO.getRePassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        User dbUser = userMapper.selectByUsername(registerDTO.getUsername());
        if (dbUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(BCryptUtil.encode(registerDTO.getPassword()));
        user.setNickname(StringUtils.hasText(registerDTO.getNickname()) ? registerDTO.getNickname() : registerDTO.getUsername());
        userMapper.insert(user);
    }

    @Override
    public UserProfileVO me() {
        User user = loadCurrentUser();
        UserProfileVO profileVO = new UserProfileVO();
        profileVO.setUserId(user.getId());
        profileVO.setUsername(user.getUsername());
        profileVO.setNickname(user.getNickname());
        profileVO.setAvatar(user.getAvatar());
        profileVO.setRole(user.getRole() == null ? UserConstants.ROLE_USER : user.getRole());
        return profileVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(MultipartFile file) {
        User currentUser = loadCurrentUser();
        String oldAvatar = currentUser.getAvatar();

        OssService.UploadResult uploadResult = ossService.uploadAvatar(file, currentUser.getId());
        try {
            int rows = userMapper.updateAvatarById(currentUser.getId(), uploadResult.fileUrl());
            if (rows == 0) {
                throw new RuntimeException("头像更新失败");
            }
        } catch (Exception e) {
            try {
                ossService.deleteObject(uploadResult.objectKey());
            } catch (Exception cleanupError) {
                log.warn("头像回滚失败: {}", uploadResult.objectKey(), cleanupError);
            }
            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException("头像更新失败", e);
        }

        if (StringUtils.hasText(oldAvatar)) {
            try {
                ossService.deleteByUrl(oldAvatar);
            } catch (Exception e) {
                log.warn("旧头像删除失败: {}", oldAvatar, e);
            }
        }
        return uploadResult.fileUrl();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(UserProfileUpdateDTO dto) {
        PermissionUtil.requireLogin();
        LoginUser loginUser = UserContext.getUser();
        int rows = userMapper.updateNicknameById(loginUser.getUserId(), dto.getNickname().trim());
        if (rows == 0) {
            throw new RuntimeException("更新资料失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UserPasswordUpdateDTO dto) {
        PermissionUtil.requireLogin();
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("两次输入的新密码不一致");
        }
        LoginUser loginUser = UserContext.getUser();
        User user = userMapper.selectById(loginUser.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!BCryptUtil.match(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }
        int rows = userMapper.updatePasswordById(user.getId(), BCryptUtil.encode(dto.getNewPassword()));
        if (rows == 0) {
            throw new RuntimeException("修改密码失败");
        }
    }

    @Override
    public PageResult<UserAdminPageVO> pageUsers(UserAdminPageQueryDTO queryDTO) {
        PermissionUtil.requireAdmin();
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        List<UserAdminPageVO> list = userMapper.selectUserPage(queryDTO);
        PageInfo<UserAdminPageVO> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer status) {
        PermissionUtil.requireAdmin();
        if (!Integer.valueOf(UserConstants.STATUS_ENABLED).equals(status)
                && !Integer.valueOf(UserConstants.STATUS_DISABLED).equals(status)) {
            throw new RuntimeException("状态非法");
        }
        int rows = userMapper.updateStatusByIdAndRole(userId, UserConstants.ROLE_USER, status);
        if (rows == 0) {
            throw new RuntimeException("用户不存在或不可修改");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetUserPassword(Long userId) {
        PermissionUtil.requireAdmin();
        String encodedPassword = BCryptUtil.encode(UserConstants.DEFAULT_RESET_PASSWORD);
        int rows = userMapper.resetPasswordByIdAndRole(userId, UserConstants.ROLE_USER, encodedPassword);
        if (rows == 0) {
            throw new RuntimeException("用户不存在或不可重置密码");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        PermissionUtil.requireAdmin();
        LoginUser admin = UserContext.getUser();
        articleMapper.deleteByCreateBy(userId, admin.getUserId());
        int rows = userMapper.deleteByIdAndRole(userId, UserConstants.ROLE_USER);
        if (rows == 0) {
            throw new RuntimeException("用户不存在或不可删除");
        }
    }

    private User loadCurrentUser() {
        LoginUser loginUser = UserContext.getUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new RuntimeException("请先登录");
        }
        User user = userMapper.selectById(loginUser.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }
}
