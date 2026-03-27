package blog.service.impl;

import blog.common.Result.PageResult;
import blog.entity.dto.ArticleAddDTO;
import blog.entity.dto.ArticlePageQueryDTO;
import blog.entity.dto.ArticleQueryDTO;
import blog.entity.dto.ArticleUpdateDTO;
import blog.entity.po.Article;
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
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public void add(ArticleAddDTO addDTO) {
        Long currentUserId = UserContext.getUser().getUserId();

        Article article = new Article();
        BeanUtils.copyProperties(addDTO, article);

        article.setIsTop(article.getIsTop() == null ? 0 : article.getIsTop());
        article.setViewCount(0);
        article.setCommentCount(0);
        article.setCreateBy(currentUserId);
        article.setUpdateBy(currentUserId);
        article.setIsDeleted(0);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());

        articleMapper.insert(article);
    }

    @Override
    public List<ArticleVO> list(ArticleQueryDTO queryDTO) {
        Long currentUserId = UserContext.getUser().getUserId();
        List<Article> articleList = articleMapper.selectList(queryDTO, currentUserId);

        List<ArticleVO> voList = new ArrayList<>();
        for (Article article : articleList) {
            ArticleVO vo = new ArticleVO();
            BeanUtils.copyProperties(article, vo);
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public List<ArticleVO> listPublished(ArticleQueryDTO queryDTO) {
        List<Article> articleList = articleMapper.selectPublishedList(queryDTO);

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
        Long currentUserId = UserContext.getUser().getUserId();
        Article article = articleMapper.selectByIdAndCreateBy(id, currentUserId);
        if (article == null) {
            throw new RuntimeException("文章不存在或无权查看");
        }

        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);
        return vo;
    }

    @Override
    public ArticleVO detailPublished(Long id) {
        Article article = articleMapper.selectPublishedById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在或未发布");
        }

        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);
        return vo;
    }

    @Override
    public void update(ArticleUpdateDTO updateDTO) {
        Long currentUserId = UserContext.getUser().getUserId();

        Article article = new Article();
        BeanUtils.copyProperties(updateDTO, article);
        article.setIsTop(article.getIsTop() == null ? 0 : article.getIsTop());
        article.setUpdateBy(currentUserId);
        article.setUpdateTime(LocalDateTime.now());

        int rows = articleMapper.updateByIdAndCreateBy(article, currentUserId);
        if (rows == 0) {
            throw new RuntimeException("文章不存在或无权限修改");
        }
    }

    @Override
    public void delete(Long id) {
        Long currentUserId = UserContext.getUser().getUserId();
        int rows = articleMapper.deleteByIdAndCreateBy(id, currentUserId, currentUserId);
        if (rows == 0) {
            throw new RuntimeException("文章不存在或无权限删除");
        }
    }

    @Override
    public PageResult<ArticlePageVO> page(ArticlePageQueryDTO queryDTO) {
        Long currentUserId = UserContext.getUser().getUserId();
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        List<ArticlePageVO> list = articleMapper.selectPage(queryDTO, currentUserId);

        PageInfo<ArticlePageVO> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }
}
