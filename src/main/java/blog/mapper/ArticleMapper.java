package blog.mapper;

import blog.entity.dto.ArticlePageQueryDTO;
import blog.entity.dto.ArticleQueryDTO;
import blog.entity.dto.ArticleReviewPageQueryDTO;
import blog.entity.po.Article;
import blog.entity.vo.ArticlePageVO;
import blog.entity.vo.ArticleReviewPageVO;
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
}
