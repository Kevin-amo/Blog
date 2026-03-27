package blog.service.impl;

import blog.common.Result.PageResult;
import blog.entity.po.Category;
import blog.entity.dto.CategoryAddDTO;
import blog.entity.dto.CategoryPageQueryDTO;
import blog.entity.dto.CategoryUpdateDTO;
import blog.entity.vo.CategoryOptionVO;
import blog.entity.vo.CategoryVO;
import blog.mapper.ArticleMapper;
import blog.mapper.CategoryMapper;
import blog.service.CategoryService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author admin
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    private final ArticleMapper articleMapper;

    /**
     * 新增分类
     *
     * @param dto 新增参数
     */
    @Override
    public void add(CategoryAddDTO dto) {
        // 校验分类名称是否重复
        Integer count = categoryMapper.countByName(dto.getName());
        if (count != null && count > 0) {
            throw new RuntimeException("分类名称已存在！");
        }
        // DTO转实体类
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        category.setIsDeleted(0);
        // 新增到数据
        categoryMapper.insert(category);
    }

    /**
     * 分类分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @Override
    public PageResult<CategoryVO> page(CategoryPageQueryDTO queryDTO) {
        // 开启分页
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        // 查询
        List<CategoryVO> list = categoryMapper.selectPage(queryDTO);
        // PageInfo中会自动带出总记录数
        PageInfo<CategoryVO> pageInfo = new PageInfo<>(list);
        // 封装分页结果返回
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 查询详情
     *
     * @param id 分类ID
     * @return 分类详情
     */
    @Override
    public CategoryVO detail(Long id) {
        CategoryVO vo = categoryMapper.selectById(id);
        if (vo == null) {
            throw new RuntimeException("分类不存在！");
        }
        return vo;
    }

    /**
     * 修改分类
     *
     * @param dto 修改参数
     */
    @Override
    public void update(CategoryUpdateDTO dto) {
        // 校验分类是否存在
        CategoryVO oldVO = categoryMapper.selectById(dto.getId());
        if (oldVO == null) {
            throw new RuntimeException("分类不存在！");
        }
        // 校验修改后的名称是否和其它分类冲突
        Integer count = categoryMapper.countByNameAndNotId(dto.getName(), dto.getId());
        if (count != null && count > 0) {
            throw new RuntimeException("分类名称已存在！");
        }
        // DTO转实体类
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        // 修改分类, 更新数据库
        categoryMapper.updateById(category);
    }

    /**
     * 删除分类
     *
     * @param id 分类ID
     */
    @Override
    public void delete(Long id) {
        // 校验分类是否存在
        CategoryVO oldVO = categoryMapper.selectById(id);
        if (oldVO == null) {
            throw new RuntimeException("分类不存在！");
        }
        // 校验该分类下是否还有文章
        Integer articleCount = articleMapper.countByCategoryId(id);
        if (articleCount != null && articleCount > 0) {
            throw new RuntimeException("分类下还有一篇文章，不能删除！");
        }
        // 执行删除（逻辑）
        categoryMapper.deleteById(id);
    }

    /**
     * 查询分类下拉选项
     *
     * @return 下拉列表
     */
    @Override
    public List<CategoryOptionVO> options() {
        return categoryMapper.selectOptions();
    }

}
