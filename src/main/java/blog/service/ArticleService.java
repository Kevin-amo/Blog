package blog.service;

import blog.common.Result.PageResult;
import blog.entity.dto.ArticleAddDTO;
import blog.entity.dto.ArticlePageQueryDTO;
import blog.entity.dto.ArticleQueryDTO;
import blog.entity.dto.ArticleReviewPageQueryDTO;
import blog.entity.dto.ArticleUpdateDTO;
import blog.entity.vo.ArticlePageVO;
import blog.entity.vo.ArticleReviewPageVO;
import blog.entity.vo.ArticleVO;

import java.util.List;

/**
 * 文章服务
 */
public interface ArticleService {

    Long add(ArticleAddDTO addDTO);

    List<ArticleVO> list(ArticleQueryDTO queryDTO);

    List<ArticleVO> listPublished(ArticleQueryDTO queryDTO);

    ArticleVO detail(Long id);

    ArticleVO detailPublished(Long id);

    ArticleVO detailForAdmin(Long id);

    void update(ArticleUpdateDTO updateDTO);

    void delete(Long id);

    PageResult<ArticlePageVO> page(ArticlePageQueryDTO queryDTO);

    PageResult<ArticleReviewPageVO> reviewPage(ArticleReviewPageQueryDTO queryDTO);

    void audit(Long id, Integer auditStatus);
}
