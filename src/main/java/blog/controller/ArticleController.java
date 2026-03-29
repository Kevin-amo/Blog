package blog.controller;

import blog.common.Result.PageResult;
import blog.common.Result.Result;
import blog.entity.dto.ArticleAddDTO;
import blog.entity.dto.ArticlePageQueryDTO;
import blog.entity.dto.ArticleQueryDTO;
import blog.entity.dto.ArticleUpdateDTO;
import blog.entity.vo.ArticlePageVO;
import blog.entity.vo.ArticleVO;
import blog.service.ArticleService;
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
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping
    public Result<Long> add(@RequestBody @Valid ArticleAddDTO addDTO) {
        return Result.success(articleService.add(addDTO));
    }

    @GetMapping("/list")
    public Result<List<ArticleVO>> list(ArticleQueryDTO queryDTO) {
        return Result.success(articleService.list(queryDTO));
    }

    @GetMapping("/public/list")
    public Result<List<ArticleVO>> publicList(ArticleQueryDTO queryDTO) {
        return Result.success(articleService.listPublished(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<ArticleVO> detail(@PathVariable Long id) {
        return Result.success(articleService.detail(id));
    }

    @GetMapping("/public/{id}")
    public Result<ArticleVO> publicDetail(@PathVariable Long id) {
        return Result.success(articleService.detailPublished(id));
    }

    @PutMapping
    public Result<Void> update(@RequestBody @Valid ArticleUpdateDTO updateDTO) {
        articleService.update(updateDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult<ArticlePageVO>> page(ArticlePageQueryDTO queryDTO) {
        return Result.success(articleService.page(queryDTO));
    }
}
