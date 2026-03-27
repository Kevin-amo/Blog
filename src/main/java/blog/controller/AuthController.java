package blog.controller;

import blog.common.Result.Result;
import blog.entity.dto.LoginDTO;
import blog.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author admin
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success("登录成功！", authService.login(loginDTO));
    }

    /**
     * 登出接口
     */
    @PostMapping("/logout")
    public Result<?> logout() {
        // 让前端删除token, 后端不需要处理
        return Result.success("登出成功！");
    }
}
