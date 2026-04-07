package blog.service;

import blog.common.Result.PageResult;
import blog.pojo.dto.ArticleAddDTO;
import blog.pojo.dto.ArticlePageQueryDTO;
import blog.pojo.dto.ArticleQueryDTO;
import blog.pojo.dto.ArticleReviewPageQueryDTO;
import blog.pojo.dto.ArticleUpdateDTO;
import blog.pojo.vo.ArticlePageVO;
import blog.pojo.vo.ArticleReviewPageVO;
import blog.pojo.vo.ArticleVO;

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
