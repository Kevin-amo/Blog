package blog.controller;

import blog.common.Result.PageResult;
import blog.common.Result.Result;
import blog.entity.dto.ArticleAuditUpdateDTO;
import blog.entity.dto.ArticleReviewPageQueryDTO;
import blog.entity.dto.UserAdminPageQueryDTO;
import blog.entity.dto.UserStatusUpdateDTO;
import blog.entity.vo.ArticleReviewPageVO;
import blog.entity.vo.ArticleVO;
import blog.entity.vo.UserAdminPageVO;
import blog.service.ArticleService;
import blog.service.UserService;
import blog.util.PermissionUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author admin
 *
 * 管理员接口
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ArticleService articleService;

    @GetMapping("/users/page")
    public Result<PageResult<UserAdminPageVO>> pageUsers(UserAdminPageQueryDTO queryDTO) {
        PermissionUtil.requireAdmin();
        return Result.success(userService.pageUsers(queryDTO));
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable("id") Long userId,
                                         @RequestBody @Valid UserStatusUpdateDTO dto) {
        PermissionUtil.requireAdmin();
        userService.updateUserStatus(userId, dto.getStatus());
        return Result.success();
    }

    @PutMapping("/users/{id}/reset-password")
    public Result<Void> resetUserPassword(@PathVariable("id") Long userId) {
        PermissionUtil.requireAdmin();
        userService.resetUserPassword(userId);
        return Result.success();
    }

    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable("id") Long userId) {
        PermissionUtil.requireAdmin();
        userService.deleteUser(userId);
        return Result.success();
    }

    @GetMapping("/articles/review/page")
    public Result<PageResult<ArticleReviewPageVO>> reviewPage(ArticleReviewPageQueryDTO queryDTO) {
        PermissionUtil.requireAdmin();
        return Result.success(articleService.reviewPage(queryDTO));
    }

    @GetMapping("/articles/{id}")
    public Result<ArticleVO> articleDetail(@PathVariable("id") Long articleId) {
        PermissionUtil.requireAdmin();
        return Result.success(articleService.detailForAdmin(articleId));
    }

    @PutMapping("/articles/{id}/audit")
    public Result<Void> auditArticle(@PathVariable("id") Long articleId,
                                     @RequestBody @Valid ArticleAuditUpdateDTO dto) {
        PermissionUtil.requireAdmin();
        articleService.audit(articleId, dto.getAuditStatus());
        return Result.success();
    }
}
