package blog.service.impl;

import blog.entity.dto.UserRegisterDTO;
import blog.entity.po.User;
import blog.entity.vo.UserProfileVO;
import blog.mapper.UserMapper;
import blog.service.OssService;
import blog.service.UserService;
import blog.util.BCryptUtil;
import blog.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户相关业务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final OssService ossService;

    /**
     * 用户注册：校验参数、加密密码并入库。
     */
    @Override
    public void register(UserRegisterDTO registerDTO) {
        String password = registerDTO.getPassword();
        String rePassword = registerDTO.getRePassword();
        if (!password.equals(rePassword)) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        User dbUser = userMapper.selectByUsername(registerDTO.getUsername());
        if (dbUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(BCryptUtil.encode(registerDTO.getPassword()));
        // 昵称为空时默认使用用户名。
        if (StringUtils.hasText(registerDTO.getNickname())) {
            user.setNickname(registerDTO.getNickname());
        } else {
            user.setNickname(registerDTO.getUsername());
        }
        userMapper.insert(user);
    }

    /**
     * 返回当前登录用户信息。
     */
    @Override
    public UserProfileVO me() {
        User user = loadCurrentUser();

        UserProfileVO profileVO = new UserProfileVO();
        profileVO.setUserId(user.getId());
        profileVO.setUsername(user.getUsername());
        profileVO.setNickname(user.getNickname());
        profileVO.setAvatar(user.getAvatar());
        return profileVO;
    }

    /**
     * 上传头像并更新数据库。
     * 1) 先上传新头像
     * 2) 再更新数据库
     * 3) 数据库失败则回滚删除新头像
     * 4) 成功后尝试删除旧头像（失败仅记录日志）
     */
    @Override
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
                log.warn("头像回滚失败，新对象未删除：{}", uploadResult.objectKey(), cleanupError);
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
                log.warn("旧头像删除失败，url：{}", oldAvatar, e);
            }
        }
        return uploadResult.fileUrl();
    }

    /**
     * 读取当前登录用户并做存在性校验。
     */
    private User loadCurrentUser() {
        if (UserContext.getUser() == null || UserContext.getUser().getUserId() == null) {
            throw new RuntimeException("请先登录");
        }

        Long userId = UserContext.getUser().getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }
}
