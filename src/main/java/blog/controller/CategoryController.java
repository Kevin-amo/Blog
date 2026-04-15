package blog.controller;

import blog.common.Result.PageResult;
import blog.common.Result.Result;
import blog.pojo.dto.CategoryAddDTO;
import blog.pojo.dto.CategoryPageQueryDTO;
import blog.pojo.dto.CategoryUpdateDTO;
import blog.pojo.vo.CategoryOptionVO;
import blog.pojo.vo.CategoryVO;
import blog.service.CategoryService;
import blog.util.PermissionUtil;
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
 * 分类接口
 */
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@Tag(name = "分类接口")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "新增分类")
    public Result<Void> add(@RequestBody @Valid CategoryAddDTO addDTO) {
        PermissionUtil.requireAdmin();
        categoryService.add(addDTO);
        return Result.success();
    }

    @GetMapping
    @Operation(summary = "分页查询分类")
    public Result<PageResult<CategoryVO>> page(CategoryPageQueryDTO queryDTO) {
        PermissionUtil.requireAdmin();
        return Result.success(categoryService.page(queryDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询分类详情")
    public Result<CategoryVO> detail(@PathVariable Long id) {
        PermissionUtil.requireAdmin();
        return Result.success(categoryService.detail(id));
    }

    @PutMapping
    @Operation(summary = "更新分类")
    public Result<Void> update(@RequestBody @Valid CategoryUpdateDTO updateDTO) {
        PermissionUtil.requireAdmin();
        categoryService.update(updateDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    public Result<Void> delete(@PathVariable Long id) {
        PermissionUtil.requireAdmin();
        categoryService.delete(id);
        return Result.success();
    }

    @GetMapping("/options")
    @Operation(summary = "获取分类选项列表")
    public Result<List<CategoryOptionVO>> options() {
        return Result.success(categoryService.options());
    }
}
