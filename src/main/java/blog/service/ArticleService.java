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
 * 文章服务接口。
 */
public interface ArticleService {

    /**
     * 新增文章。
     *
     * @param addDTO 新增参数
     * @return 新增后的文章ID
     */
    Long add(ArticleAddDTO addDTO);

    /**
     * 查询当前用户可管理的文章列表。
     *
     * @param queryDTO 查询参数
     * @return 文章列表
     */
    List<ArticleVO> list(ArticleQueryDTO queryDTO);

    /**
     * 查询已发布文章列表。
     *
     * @param queryDTO 查询参数
     * @return 已发布文章列表
     */
    List<ArticleVO> listPublished(ArticleQueryDTO queryDTO);

    /**
     * 分页查询已发布文章列表。
     *
     * @param queryDTO 查询参数
     * @return 已发布文章分页结果
     */
    PageResult<ArticleVO> pagePublished(ArticlePageQueryDTO queryDTO);

    /**
     * 查询当前用户的文章详情。
     *
     * @param id 文章ID
     * @return 文章详情
     */
    ArticleVO detail(Long id);

    /**
     * 查询已发布文章详情。
     *
     * @param id 文章ID
     * @return 文章详情
     */
    ArticleVO detailPublished(Long id);

    /**
     * 后台查询文章详情。
     *
     * @param id 文章ID
     * @return 文章详情
     */
    ArticleVO detailForAdmin(Long id);

    /**
     * 修改文章。
     *
     * @param updateDTO 修改参数
     */
    void update(ArticleUpdateDTO updateDTO);

    /**
     * 删除文章。
     *
     * @param id 文章ID
     */
    void delete(Long id);

    /**
     * 分页查询文章。
     *
     * @param queryDTO 查询参数
     * @return 文章分页结果
     */
    PageResult<ArticlePageVO> page(ArticlePageQueryDTO queryDTO);

    /**
     * 后台分页查询待审核文章。
     *
     * @param queryDTO 查询参数
     * @return 审核分页结果
     */
    PageResult<ArticleReviewPageVO> reviewPage(ArticleReviewPageQueryDTO queryDTO);

    /**
     * 审核文章。
     *
     * @param id 文章ID
     * @param auditStatus 审核状态
     */
    void audit(Long id, Integer auditStatus);
}
