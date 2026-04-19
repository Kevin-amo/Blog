package blog.controller;

import blog.common.Result.PageResult;
import blog.common.Result.Result;
import blog.pojo.dto.ArticleAddDTO;
import blog.pojo.dto.ArticlePageQueryDTO;
import blog.pojo.dto.ArticleQueryDTO;
import blog.pojo.dto.ArticleUpdateDTO;
import blog.pojo.vo.ArticlePageVO;
import blog.pojo.vo.ArticleVO;
import blog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author admin
 *
 * 文章接口
 */
@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
@Tag(name = "文章接口")
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping
    @Operation(summary = "新增文章")
    public Result<Long> add(@RequestBody @Valid ArticleAddDTO addDTO) {
        return Result.success(articleService.add(addDTO));
    }

    @GetMapping("/list")
    @Operation(summary = "查询文章列表")
    public Result<List<ArticleVO>> list(ArticleQueryDTO queryDTO) {
        return Result.success(articleService.list(queryDTO));
    }

    @GetMapping("/public/list")
    @Operation(summary = "查询已发布文章列表（公开）")
    public Result<List<ArticleVO>> publicList(ArticleQueryDTO queryDTO) {
        return Result.success(articleService.listPublished(queryDTO));
    }

    @GetMapping("/public/page")
    @Operation(summary = "分页查询已发布文章列表（公开）")
    public Result<PageResult<ArticleVO>> publicPage(ArticlePageQueryDTO queryDTO) {
        return Result.success(articleService.pagePublished(queryDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询文章详情")
    public Result<ArticleVO> detail(@PathVariable Long id) {
        return Result.success(articleService.detail(id));
    }

    @GetMapping("/public/{id}")
    @Operation(summary = "查询已发布文章详情（公开）")
    public Result<ArticleVO> publicDetail(@PathVariable Long id) {
        return Result.success(articleService.detailPublished(id));
    }

    @PutMapping
    @Operation(summary = "更新文章")
    public Result<Void> update(@RequestBody @Valid ArticleUpdateDTO updateDTO) {
        articleService.update(updateDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文章")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询文章")
    public Result<PageResult<ArticlePageVO>> page(ArticlePageQueryDTO queryDTO) {
        return Result.success(articleService.page(queryDTO));
    }
}
