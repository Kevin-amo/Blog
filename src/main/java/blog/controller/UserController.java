package blog.controller;

import blog.common.Result.Result;
import blog.model.LoginUser;
import blog.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author admin
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/me")
    public Result<?> me() {
        LoginUser loginUser = UserContext.getUser();
        return Result.success(loginUser);
    }

}
