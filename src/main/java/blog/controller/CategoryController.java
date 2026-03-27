package blog.controller;

import blog.common.Result.PageResult;
import blog.common.Result.Result;
import blog.entity.dto.CategoryAddDTO;
import blog.entity.dto.CategoryPageQueryDTO;
import blog.entity.dto.CategoryUpdateDTO;
import blog.entity.vo.CategoryOptionVO;
import blog.entity.vo.CategoryVO;
import blog.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author admin
 */
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 新增分类
     *
     * @param addDTO 新增参数
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> add(@RequestBody @Valid CategoryAddDTO addDTO) {
        categoryService.add(addDTO);
        return Result.success();
    }

    /**
     * 分类分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页数据
     */
    @GetMapping
    public Result<PageResult<CategoryVO>> page(CategoryPageQueryDTO queryDTO) {
        return Result.success(categoryService.page(queryDTO));
    }

    /**
     * 分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     */
    @GetMapping("/{id}")
    public Result<CategoryVO> detail(@PathVariable Long id) {
        return Result.success(categoryService.detail(id));
    }

    /**
     * 修改分类
     *
     * @param updateDTO 修改参数
     * @return 操作结果
     */
    @PutMapping
    public Result<Void> update(@RequestBody @Valid CategoryUpdateDTO updateDTO) {
        categoryService.update(updateDTO);
        return Result.success();
    }

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }

    /**
     * 分类下拉列表
     *
     * @return 下拉选项
     */
    @GetMapping("/options")
    public Result<List<CategoryOptionVO>> options() {
        return Result.success(categoryService.options());
    }

}
