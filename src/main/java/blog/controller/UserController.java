package blog.controller;

import blog.common.Result.Result;
import blog.entity.dto.LoginDTO;
import blog.entity.dto.UserRegisterDTO;
import blog.model.LoginUser;
import blog.service.LoginService;
import blog.service.UserService;
import blog.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author admin
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
    public Result<?> me() {
        LoginUser loginUser = UserContext.getUser();
        return Result.success(loginUser);
    }
}
