package blog.mapper;

import blog.pojo.po.Category;
import blog.pojo.dto.CategoryPageQueryDTO;
import blog.pojo.vo.CategoryOptionVO;
import blog.pojo.vo.CategoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author admin
 */
@Mapper
public interface CategoryMapper {

    /**
     * 新增分类
     *
     * @param category 分类实体
     */
    void insert(Category category);

    /**
     * 根据ID查询分类
     *
     * @param id 分类ID
     * @return 分类信息
     */
    CategoryVO selectById(Long id);

    /**
     * 分页查询分类列表
     *
     * @param queryDTO 查询条件
     * @return 分类列表
     */
    List<CategoryVO> selectPage(CategoryPageQueryDTO queryDTO);

    /**
     * 修改分类
     *
     * @param category 分类实体
     */
    int updateById(Category category);

    /**
     * 逻辑删除分类
     *
     * @param id 分类ID
     */
    int deleteById(Long id);

    /**
     * 根据名称查询分类数量
     *
     * @param name 分类名称
     * @return 数量
     */
    Integer countByName(String name);

    /**
     * 修改时校验重名（排除自己）
     *
     * @param name 分类名称
     * @param id   分类ID
     * @return 数量
     */
    Integer countByNameAndNotId(@Param("name") String name, @Param("id") Long id);

    /**
     * 查询启用状态的分类下拉选项
     *
     * @return 下拉列表
     */
    List<CategoryOptionVO> selectOptions();

}
