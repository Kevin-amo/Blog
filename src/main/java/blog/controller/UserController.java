package blog.controller;

import blog.common.Result.Result;
import blog.pojo.dto.LoginDTO;
import blog.pojo.dto.UserPasswordUpdateDTO;
import blog.pojo.dto.UserProfileUpdateDTO;
import blog.pojo.dto.UserRegisterDTO;
import blog.pojo.vo.UserProfileVO;
import blog.service.LoginService;
import blog.service.UserService;
import blog.util.PermissionUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "用户接口")
public class UserController {

    private final LoginService loginService;
    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success("login success", loginService.login(loginDTO));
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<?> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success("register success", null);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public Result<?> logout() {
        return Result.success("logout success", null);
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    public Result<UserProfileVO> me() {
        return Result.success(userService.me());
    }

    @PostMapping("/avatar")
    @Operation(summary = "上传用户头像")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String avatarUrl = userService.uploadAvatar(file);
        Map<String, String> data = new HashMap<>();
        data.put("avatarUrl", avatarUrl);
        return Result.success(data);
    }

    @PutMapping("/profile")
    @Operation(summary = "更新用户资料")
    public Result<Void> updateProfile(@Valid @RequestBody UserProfileUpdateDTO dto) {
        PermissionUtil.requireLogin();
        userService.updateProfile(dto);
        return Result.success();
    }

    @PutMapping("/password")
    @Operation(summary = "修改用户密码")
    public Result<Void> updatePassword(@Valid @RequestBody UserPasswordUpdateDTO dto) {
        PermissionUtil.requireLogin();
        userService.updatePassword(dto);
        return Result.success();
    }
}
