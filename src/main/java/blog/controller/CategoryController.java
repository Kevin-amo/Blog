package blog.controller;

import blog.common.Result.PageResult;
import blog.common.Result.Result;
import blog.entity.dto.CategoryAddDTO;
import blog.entity.dto.CategoryPageQueryDTO;
import blog.entity.dto.CategoryUpdateDTO;
import blog.entity.vo.CategoryOptionVO;
import blog.entity.vo.CategoryVO;
import blog.service.CategoryService;
import blog.util.PermissionUtil;
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
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public Result<Void> add(@RequestBody @Valid CategoryAddDTO addDTO) {
        PermissionUtil.requireAdmin();
        categoryService.add(addDTO);
        return Result.success();
    }

    @GetMapping
    public Result<PageResult<CategoryVO>> page(CategoryPageQueryDTO queryDTO) {
        PermissionUtil.requireAdmin();
        return Result.success(categoryService.page(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<CategoryVO> detail(@PathVariable Long id) {
        PermissionUtil.requireAdmin();
        return Result.success(categoryService.detail(id));
    }

    @PutMapping
    public Result<Void> update(@RequestBody @Valid CategoryUpdateDTO updateDTO) {
        PermissionUtil.requireAdmin();
        categoryService.update(updateDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        PermissionUtil.requireAdmin();
        categoryService.delete(id);
        return Result.success();
    }

    @GetMapping("/options")
    public Result<List<CategoryOptionVO>> options() {
        return Result.success(categoryService.options());
    }
}
