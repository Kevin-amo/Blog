package blog.mapper;

import blog.entity.dto.ArticlePageQueryDTO;
import blog.entity.po.Article;
import blog.entity.dto.ArticleQueryDTO;
import blog.entity.vo.ArticlePageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author admin
 */
@Mapper
public interface ArticleMapper {

    /**
     * 添加文章
     */
    void insert(Article article);

    /**
     * 查询当前用户文章列表
     * @param queryDTO 查询条件
     * @param currentUserId 当前用户ID
     * @return 文章列表
     */
    List<Article> selectList(@Param("queryDTO")ArticleQueryDTO queryDTO,
                            @Param("currentUserId") Long currentUserId);

    /**
     * 根据文章ID和当前用户ID查询文章详情
     *
     * @param id 文章ID
     * @param currentUserId 当前登录用户ID
     * @return 文章详情
     */
    Article selectByIdAndCreateBy(@Param("id") Long id, @Param("currentUserId") Long currentUserId);

    /**
     * 根据文章ID和当前用户ID修改文章
     *
     * @param article 文章对象
     * @param currentUserId 当前登录用户ID
     * @return 修改数量
     */
    int updateByIdAndCreateBy(@Param("article") Article article, @Param("currentUserId") Long currentUserId);

    /**
     * 根据文章ID和当前用户ID删除文章
     *
     * @param id 文章ID
     * @param currentUserId 当前登录用户ID
     * @return 删除数量
     */
    int deleteByIdAndCreateBy(@Param("id") Long id, @Param("currentUserId") Long currentUserId, @Param("updateBy") Long updateBy);

    /**
     * 统计某个分类下有多少篇文章
     *
     * @param categoryId 分类ID
     * @return 文章数量
     */
    Integer countByCategoryId(Long categoryId);

    /**
     * 分页查询文章列表
     *
     * @param queryDTO 查询条件
     * @param currentUserId 当前登录用户ID
     * @return 文章列表
     */
    List<ArticlePageVO> selectPage(@Param("queryDTO") ArticlePageQueryDTO queryDTO,
                                   @Param("currentUserId") Long currentUserId);

}
