package blog.service.impl;

import blog.common.Result.PageResult;
import static blog.common.constant.ArticleConstants.*;
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
    public Long add(ArticleAddDTO addDTO) {
        Long currentUserId = UserContext.getUser().getUserId();
        validateStatus(addDTO.getStatus());
        if (STATUS_PUBLISHED == addDTO.getStatus()) {
            validatePublishFields(addDTO.getTitle(), addDTO.getContent(), addDTO.getCategoryId());
        }

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
        return article.getId();
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
        validateStatus(updateDTO.getStatus());
        if (STATUS_PUBLISHED == updateDTO.getStatus()) {
            validatePublishFields(updateDTO.getTitle(), updateDTO.getContent(), updateDTO.getCategoryId());
        }

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

    private void validateStatus(Integer status) {
        if (status == null) {
            throw new RuntimeException("文章状态不能为空");
        }
        if (status != STATUS_DRAFT && status != STATUS_PUBLISHED) {
            throw new RuntimeException("文章状态非法");
        }
    }

    private void validatePublishFields(String title, String content, Long categoryId) {
        if (title == null || title.trim().isEmpty()) {
            throw new RuntimeException("请输入文章标题");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("请输入文章内容");
        }
        if (categoryId == null) {
            throw new RuntimeException("请选择分类");
        }
    }
}
