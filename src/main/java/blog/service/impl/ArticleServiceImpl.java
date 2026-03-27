package blog.service.impl;

import blog.common.Result.PageResult;
import blog.entity.dto.ArticlePageQueryDTO;
import blog.entity.po.Article;
import blog.entity.dto.ArticleAddDTO;
import blog.entity.dto.ArticleQueryDTO;
import blog.entity.dto.ArticleUpdateDTO;
import blog.entity.vo.ArticlePageVO;
import blog.entity.vo.ArticleVO;
import blog.mapper.ArticleMapper;
import blog.service.ArticleService;
import blog.util.UserContext;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author admin
 * 文章服务实现类
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public void add(ArticleAddDTO addDTO) {
        // 1.获取当前登录用户ID
        Long currentUserId = UserContext.getUser().getUserId();
        // 2.将DTO转为实体类
        Article article = new Article();
        BeanUtils.copyProperties(addDTO, article);
        // 3.补充系统字段
        article.setIsTop(article.getIsTop() == null ? 0 : article.getIsTop());
        article.setViewCount(0);
        article.setCommentCount(0);
        article.setCreateBy(currentUserId);
        article.setUpdateBy(currentUserId);
        article.setIsDeleted(0);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        // 4.存数据库
        articleMapper.insert(article);
    }

    @Override
    public List<ArticleVO> list(ArticleQueryDTO queryDTO) {
        // 1.获取当前登录用户ID
        Long currentUserId = UserContext.getUser().getUserId();
        // 2.查询当前用户自己的文章
        List<Article> articleList = articleMapper.selectList(queryDTO, currentUserId);
        // 3.转换为VO -->给前端
        List<ArticleVO> voList = new ArrayList<>();
        for (Article article : articleList) {
            ArticleVO vo = new ArticleVO();
            BeanUtils.copyProperties(article, vo);
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public ArticleVO detail(Long id) {
        // 1.获取当前登录用户ID
        Long currentUserId = UserContext.getUser().getUserId();
        // 2.查询当前用户自己的文章
        Article article = articleMapper.selectByIdAndCreateBy(id, currentUserId);
        // 3.判空
        if (article == null) {
            throw new RuntimeException("文章不存在或无权查看");
        }
        // 4.转vo
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);
        return vo;
    }

    @Override
    public void update(ArticleUpdateDTO updateDTO) {
        // 1. 获取当前登录用户ID
        Long currentUserId = UserContext.getUser().getUserId();
        // 2.DTO转实体类
        Article article = new Article();
        BeanUtils.copyProperties(updateDTO, article);
        // 3.补充更新信息
        article.setIsTop(article.getIsTop() == null ? 0 : article.getIsTop());
        article.setUpdateBy(currentUserId);
        article.setUpdateTime(LocalDateTime.now());
        // 4.更新
        int rows = articleMapper.updateByIdAndCreateBy(article, currentUserId);
        if (rows == 0) {
            throw new RuntimeException("文章不存在或无权限修改");
        }

    }

    /**
     * 逻辑删除
     * @param id 文章ID
     */
    @Override
    public void delete(Long id) {
        // 1. 获取当前登录用户ID
        Long currentUserId = UserContext.getUser().getUserId();
        // 2. 执行逻辑删除
        int rows = articleMapper.deleteByIdAndCreateBy(id, currentUserId, currentUserId);
        if (rows == 0) {
            throw new RuntimeException("文章不存在或无权删除");
        }
    }

    /**
     * 文章分页列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult<ArticlePageVO> page(ArticlePageQueryDTO queryDTO) {
        // 仅查询当前登录用户自己的文章
        Long currentUserId = UserContext.getUser().getUserId();
        // 开启分页
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        // 查询文章列表
        List<ArticlePageVO> list = articleMapper.selectPage(queryDTO, currentUserId);
        // 封装分页信息
        PageInfo<ArticlePageVO> pageInfo = new PageInfo<>(list);
        // 统一返回结果
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }
}
