package blog.controller;

import blog.common.Result.PageResult;
import blog.common.Result.Result;
import blog.pojo.dto.ArticleAuditUpdateDTO;
import blog.pojo.dto.ArticleReviewPageQueryDTO;
import blog.pojo.dto.UserAdminPageQueryDTO;
import blog.pojo.dto.UserStatusUpdateDTO;
import blog.pojo.vo.ArticleReviewPageVO;
import blog.pojo.vo.ArticleVO;
import blog.pojo.vo.UserAdminPageVO;
import blog.service.ArticleService;
import blog.service.UserService;
import blog.util.PermissionUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "管理员接口")
public class AdminController {

    private final UserService userService;
    private final ArticleService articleService;

    @GetMapping("/users/page")
    @Operation(summary = "分页查询用户列表")
    public Result<PageResult<UserAdminPageVO>> pageUsers(UserAdminPageQueryDTO queryDTO) {
        PermissionUtil.requireAdmin();
        return Result.success(userService.pageUsers(queryDTO));
    }

    @PutMapping("/users/{id}/status")
    @Operation(summary = "更新用户状态")
    public Result<Void> updateUserStatus(@PathVariable("id") Long userId,
                                         @RequestBody @Valid UserStatusUpdateDTO dto) {
        PermissionUtil.requireAdmin();
        userService.updateUserStatus(userId, dto.getStatus());
        return Result.success();
    }

    @PutMapping("/users/{id}/reset-password")
    @Operation(summary = "重置用户密码")
    public Result<Void> resetUserPassword(@PathVariable("id") Long userId) {
        PermissionUtil.requireAdmin();
        userService.resetUserPassword(userId);
        return Result.success();
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "删除用户")
    public Result<Void> deleteUser(@PathVariable("id") Long userId) {
        PermissionUtil.requireAdmin();
        userService.deleteUser(userId);
        return Result.success();
    }

    @GetMapping("/articles/review/page")
    @Operation(summary = "分页查询待审核文章")
    public Result<PageResult<ArticleReviewPageVO>> reviewPage(ArticleReviewPageQueryDTO queryDTO) {
        PermissionUtil.requireAdmin();
        return Result.success(articleService.reviewPage(queryDTO));
    }

    @GetMapping("/articles/{id}")
    @Operation(summary = "查询文章详情（管理员）")
    public Result<ArticleVO> articleDetail(@PathVariable("id") Long articleId) {
        PermissionUtil.requireAdmin();
        return Result.success(articleService.detailForAdmin(articleId));
    }

    @PutMapping("/articles/{id}/audit")
    @Operation(summary = "审核文章")
    public Result<Void> auditArticle(@PathVariable("id") Long articleId,
                                     @RequestBody @Valid ArticleAuditUpdateDTO dto) {
        PermissionUtil.requireAdmin();
        articleService.audit(articleId, dto.getAuditStatus());
        return Result.success();
    }
}
