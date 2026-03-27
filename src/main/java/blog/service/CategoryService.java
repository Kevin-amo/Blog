package blog.service;

import blog.common.Result.PageResult;
import blog.entity.dto.CategoryAddDTO;
import blog.entity.dto.CategoryUpdateDTO;
import blog.entity.vo.CategoryOptionVO;
import blog.entity.vo.CategoryVO;
import blog.entity.dto.CategoryPageQueryDTO;

import java.util.List;

/**
 * @author admin
 */
public interface CategoryService {

    /**
     * 新增分类
     *
     * @param dto 新增参数
     */
    void add(CategoryAddDTO dto);

    /**
     * 分类分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult<CategoryVO> page(CategoryPageQueryDTO queryDTO);

    /**
     * 根据ID查询分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     */
    CategoryVO detail(Long id);

    /**
     * 修改分类
     *
     * @param dto 修改参数
     */
    void update(CategoryUpdateDTO dto);

    /**
     * 删除分类
     *
     * @param id 分类ID
     */
    void delete(Long id);

    /**
     * 查询分类下拉选项
     *
     * @return 下拉列表
     */
    List<CategoryOptionVO> options();

}
