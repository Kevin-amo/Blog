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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author admin
 */
@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * 新增文章
     *
     * @param addDTO 新增参数
     * @return 操作结果
     */
    @PostMapping
    public Result<Long> add(@RequestBody @Valid ArticleAddDTO addDTO) {
        return Result.success(articleService.add(addDTO));
    }

    /**
     * 文章列表
     *
     * @param queryDTO 查询参数
     * @return 操作结果
     */
    @GetMapping("/list")
    public Result<List<ArticleVO>> list(ArticleQueryDTO queryDTO) {
        return Result.success(articleService.list(queryDTO));
    }

    /**
     * 公开文章列表（游客可访问）
     *
     * @param queryDTO 查询参数
     * @return 已发布文章
     */
    @GetMapping("/public/list")
    public Result<List<ArticleVO>> publicList(ArticleQueryDTO queryDTO) {
        return Result.success(articleService.listPublished(queryDTO));
    }

    /**
     * 文章详情
     *
     * @param id 文章id
     * @return 文章详情
     */
    @GetMapping("/{id}")
    public Result<ArticleVO> detail(@PathVariable Long id) {
        return Result.success(articleService.detail(id));
    }

    /**
     * 公开文章详情（游客可访问）
     *
     * @param id 文章id
     * @return 已发布文章详情
     */
    @GetMapping("/public/{id}")
    public Result<ArticleVO> publicDetail(@PathVariable Long id) {
        return Result.success(articleService.detailPublished(id));
    }

    /**
     * 编辑文章
     *
     * @param updateDTO 修改参数
     * @return 操作结果
     */
    @PutMapping
    public Result<Void> update(@RequestBody @Valid ArticleUpdateDTO updateDTO) {
        articleService.update(updateDTO);
        return Result.success();
    }

    /**
     * 删除文章
     *
     * @param id 文章id
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }

    /**
     * 文章分页列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<PageResult<ArticlePageVO>> page(ArticlePageQueryDTO queryDTO) {
        return Result.success(articleService.page(queryDTO));
    }

}
