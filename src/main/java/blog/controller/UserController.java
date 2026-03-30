package blog.controller;

import blog.common.Result.Result;
import blog.entity.dto.LoginDTO;
import blog.entity.dto.UserPasswordUpdateDTO;
import blog.entity.dto.UserProfileUpdateDTO;
import blog.entity.dto.UserRegisterDTO;
import blog.entity.vo.UserProfileVO;
import blog.service.LoginService;
import blog.service.UserService;
import blog.util.PermissionUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * @author admin
 *
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final LoginService loginService;
    private final UserService userService;

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success("login success", loginService.login(loginDTO));
    }

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success("register success", null);
    }

    @PostMapping("/logout")
    public Result<?> logout() {
        return Result.success("logout success", null);
    }

    @GetMapping("/me")
    public Result<UserProfileVO> me() {
        return Result.success(userService.me());
    }

    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String avatarUrl = userService.uploadAvatar(file);
        Map<String, String> data = new HashMap<>();
        data.put("avatarUrl", avatarUrl);
        return Result.success(data);
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UserProfileUpdateDTO dto) {
        PermissionUtil.requireLogin();
        userService.updateProfile(dto);
        return Result.success();
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody UserPasswordUpdateDTO dto) {
        PermissionUtil.requireLogin();
        userService.updatePassword(dto);
        return Result.success();
    }
}
