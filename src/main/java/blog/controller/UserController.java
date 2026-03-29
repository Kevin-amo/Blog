package blog.controller;

import blog.common.Result.Result;
import blog.entity.dto.LoginDTO;
import blog.entity.dto.UserRegisterDTO;
import blog.entity.vo.UserProfileVO;
import blog.service.LoginService;
import blog.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * @author admin
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final LoginService loginService;
    private final UserService userService;

    /**
     * 用户登录。
     */
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success("login success", loginService.login(loginDTO));
    }

    /**
     * 用户注册。
     */
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success("register success", null);
    }

    /**
     * 退出登录（后端无状态，仅返回成功）。
     */
    @PostMapping("/logout")
    public Result<?> logout() {
        return Result.success("logout success", null);
    }

    /**
     * 获取当前登录用户信息。
     */
    @GetMapping("/me")
    public Result<UserProfileVO> me() {
        return Result.success(userService.me());
    }

    /**
     * 上传头像并更新用户资料。
     */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String avatarUrl = userService.uploadAvatar(file);
        Map<String, String> data = new HashMap<>();
        data.put("avatarUrl", avatarUrl);
        return Result.success(data);
    }
}
