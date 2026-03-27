package blog.service;

import blog.common.Result.PageResult;
import blog.entity.dto.ArticleAddDTO;
import blog.entity.dto.ArticlePageQueryDTO;
import blog.entity.dto.ArticleQueryDTO;
import blog.entity.dto.ArticleUpdateDTO;
import blog.entity.vo.ArticlePageVO;
import blog.entity.vo.ArticleVO;

import java.util.List;

/**
 * @author admin
 */
public interface ArticleService {

    /**
     * 添加文章
     *
     * @param addDTO 添加参数
     */
    void add(ArticleAddDTO addDTO);

    /**
     * 查询当前登录用户的文章列表
     *
     * @param queryDTO 查询条件
     * @return 文章列表
     */
    List<ArticleVO> list(ArticleQueryDTO queryDTO);

    /**
     * 查询文章详情
     *
     * @param id 文章ID
     * @return 文章详情
     */
    ArticleVO detail(Long id);

    /**
     * 修改文章
     *
     * @param updateDTO 修改参数
     */
    void update(ArticleUpdateDTO updateDTO);

    /**
     * 删除文章
     *
     * @param id 文章ID
     */
    void delete(Long id);

    /**
     * 文章分页列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<ArticlePageVO> page(ArticlePageQueryDTO queryDTO);

}
