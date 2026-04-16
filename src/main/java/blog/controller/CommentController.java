package blog.controller;

import blog.common.Result.Result;
import blog.pojo.po.Comment;
import blog.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
@Tag(name = "评论接口")
public class CommentController {

    @Resource
    private CommentService commentService;

    /**
     * 新增评论
     */
    @PostMapping("/add")
    @Operation(summary = "新增评论")
    public Result<Void> addComment(@RequestBody Comment comment) {
        commentService.addComment(comment);
        return Result.success();
    }

    /**
     * 查询文章的评论列表
     */
    @GetMapping("/list/{articleId}")
    @Operation(summary = "根据文章ID查询评论列表")
    public Result<List<Comment>> listComments(@PathVariable Long articleId) {
        List<Comment> comments = commentService.listByArticleId(articleId);
        return Result.success(comments);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除当前用户自己的评论")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.delete(id);
        return Result.success();
    }

}
