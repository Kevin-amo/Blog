package blog.mapper;

import blog.pojo.dto.ArticlePageQueryDTO;
import blog.pojo.dto.ArticleQueryDTO;
import blog.pojo.dto.ArticleReviewPageQueryDTO;
import blog.pojo.po.Article;
import blog.pojo.vo.ArticlePageVO;
import blog.pojo.vo.ArticleReviewPageVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author admin
 * 文章 mapper
 */
public interface ArticleMapper {

    void insert(Article article);

    List<Article> selectList(@Param("queryDTO") ArticleQueryDTO queryDTO, @Param("currentUserId") Long currentUserId);

    List<Article> selectPublishedList(@Param("queryDTO") ArticleQueryDTO queryDTO);

    Article selectByIdAndCreateBy(@Param("id") Long id, @Param("currentUserId") Long currentUserId);

    Article selectPublishedById(@Param("id") Long id);

    Article selectById(@Param("id") Long id);

    int updateByIdAndCreateBy(@Param("article") Article article, @Param("currentUserId") Long currentUserId);

    int deleteByIdAndCreateBy(@Param("id") Long id, @Param("currentUserId") Long currentUserId, @Param("updateBy") Long updateBy);

    Integer countByCategoryId(Long categoryId);

    List<ArticlePageVO> selectPage(@Param("queryDTO") ArticlePageQueryDTO queryDTO, @Param("currentUserId") Long currentUserId);

    List<ArticleReviewPageVO> selectReviewPage(@Param("queryDTO") ArticleReviewPageQueryDTO queryDTO);

    int updateAuditStatusById(@Param("id") Long id, @Param("auditStatus") Integer auditStatus, @Param("updateBy") Long updateBy);

    int deleteByCreateBy(@Param("createBy") Long createBy, @Param("updateBy") Long updateBy);

    List<Long> selectIdsByCreateBy(@Param("createBy") Long createBy);
}
