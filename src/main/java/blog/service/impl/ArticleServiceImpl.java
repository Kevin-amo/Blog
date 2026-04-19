package blog.service.impl;

import blog.common.Result.PageResult;
import blog.mapper.ArticleMapper;
import blog.pojo.dto.ArticleAddDTO;
import blog.pojo.dto.ArticlePageQueryDTO;
import blog.pojo.dto.ArticleQueryDTO;
import blog.pojo.dto.ArticleReviewPageQueryDTO;
import blog.pojo.dto.ArticleUpdateDTO;
import blog.pojo.po.Article;
import blog.pojo.vo.ArticlePageVO;
import blog.pojo.vo.ArticleReviewPageVO;
import blog.pojo.vo.ArticleVO;
import blog.service.ArticleService;
import blog.util.PermissionUtil;
import blog.util.UserContext;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static blog.common.constant.ArticleConstants.AUDIT_APPROVED;
import static blog.common.constant.ArticleConstants.AUDIT_PENDING;
import static blog.common.constant.ArticleConstants.AUDIT_REJECTED;
import static blog.common.constant.ArticleConstants.STATUS_DRAFT;
import static blog.common.constant.ArticleConstants.STATUS_PUBLISHED;

/**
 * 文章服务实现类。
 */
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;

    /**
     * 新增文章。
     *
     * @param addDTO 新增参数
     * @return 文章ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(ArticleAddDTO addDTO) {
        PermissionUtil.requireUser();
        Long currentUserId = UserContext.getUser().getUserId();
        validateStatus(addDTO.getStatus());
        if (STATUS_PUBLISHED == addDTO.getStatus()) {
            validatePublishFields(addDTO.getTitle(), addDTO.getContent(), addDTO.getCategoryId());
        }

        Article article = new Article();
        BeanUtils.copyProperties(addDTO, article);
        article.setAuditStatus(AUDIT_PENDING);
        article.setIsTop(article.getIsTop() == null ? 0 : article.getIsTop());
        article.setViewCount(0);
        article.setCreateBy(currentUserId);
        article.setUpdateBy(currentUserId);
        article.setIsDeleted(0);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());

        articleMapper.insert(article);
        return article.getId();
    }

    /**
     * 查询当前用户文章列表。
     *
     * @param queryDTO 查询参数
     * @return 文章列表
     */
    @Override
    public List<ArticleVO> list(ArticleQueryDTO queryDTO) {
        PermissionUtil.requireUser();
        Long currentUserId = UserContext.getUser().getUserId();
        List<Article> articleList = articleMapper.selectList(queryDTO, currentUserId);
        List<ArticleVO> voList = new ArrayList<>(articleList.size());
        for (Article article : articleList) {
            ArticleVO vo = new ArticleVO();
            BeanUtils.copyProperties(article, vo);
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 查询公开已发布文章列表。
     *
     * @param queryDTO 查询参数
     * @return 文章列表
     */
    @Override
    public List<ArticleVO> listPublished(ArticleQueryDTO queryDTO) {
        List<Article> articleList = articleMapper.selectPublishedList(queryDTO);
        List<ArticleVO> voList = new ArrayList<>(articleList.size());
        for (Article article : articleList) {
            ArticleVO vo = new ArticleVO();
            BeanUtils.copyProperties(article, vo);
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 分页查询公开已发布文章列表。
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @Override
    public PageResult<ArticleVO> pagePublished(ArticlePageQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        ArticleQueryDTO publicQuery = new ArticleQueryDTO();
        publicQuery.setTitle(queryDTO.getTitle());
        publicQuery.setCategoryId(queryDTO.getCategoryId());
        List<Article> articleList = articleMapper.selectPublishedList(publicQuery);
        PageInfo<Article> pageInfo = new PageInfo<>(articleList);
        List<ArticleVO> records = new ArrayList<>(pageInfo.getList().size());
        for (Article article : pageInfo.getList()) {
            ArticleVO vo = new ArticleVO();
            BeanUtils.copyProperties(article, vo);
            records.add(vo);
        }
        return new PageResult<>(pageInfo.getTotal(), records);
    }

    /**
     * 查询当前用户文章详情。
     *
     * @param id 文章ID
     * @return 文章详情
     */
    @Override
    public ArticleVO detail(Long id) {
        PermissionUtil.requireUser();
        Long currentUserId = UserContext.getUser().getUserId();
        Article article = articleMapper.selectByIdAndCreateBy(id, currentUserId);
        if (article == null) {
            throw new RuntimeException("文章不存在或无权查看");
        }
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);
        return vo;
    }

    /**
     * 查询已发布文章详情。
     *
     * @param id 文章ID
     * @return 文章详情
     */
    @Override
    public ArticleVO detailPublished(Long id) {
        Article article = articleMapper.selectPublishedById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在或暂未发布");
        }
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);
        return vo;
    }

    /**
     * 后台查询文章详情。
     *
     * @param id 文章ID
     * @return 文章详情
     */
    @Override
    public ArticleVO detailForAdmin(Long id) {
        PermissionUtil.requireAdmin();
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);
        return vo;
    }

    /**
     * 修改文章。
     *
     * @param updateDTO 修改参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ArticleUpdateDTO updateDTO) {
        PermissionUtil.requireUser();
        Long currentUserId = UserContext.getUser().getUserId();
        validateStatus(updateDTO.getStatus());
        if (STATUS_PUBLISHED == updateDTO.getStatus()) {
            validatePublishFields(updateDTO.getTitle(), updateDTO.getContent(), updateDTO.getCategoryId());
        }

        Article article = new Article();
        BeanUtils.copyProperties(updateDTO, article);
        article.setAuditStatus(AUDIT_PENDING);
        article.setIsTop(article.getIsTop() == null ? 0 : article.getIsTop());
        article.setUpdateBy(currentUserId);
        article.setUpdateTime(LocalDateTime.now());

        int rows = articleMapper.updateByIdAndCreateBy(article, currentUserId);
        if (rows == 0) {
            throw new RuntimeException("文章不存在或无权修改");
        }
    }

    /**
     * 删除文章。
     *
     * @param id 文章ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        PermissionUtil.requireUser();
        Long currentUserId = UserContext.getUser().getUserId();
        int rows = articleMapper.deleteByIdAndCreateBy(id, currentUserId, currentUserId);
        if (rows == 0) {
            throw new RuntimeException("文章不存在或无权删除");
        }
    }

    /**
     * 分页查询当前用户文章。
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @Override
    public PageResult<ArticlePageVO> page(ArticlePageQueryDTO queryDTO) {
        PermissionUtil.requireUser();
        Long currentUserId = UserContext.getUser().getUserId();
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        List<ArticlePageVO> list = articleMapper.selectPage(queryDTO, currentUserId);
        PageInfo<ArticlePageVO> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 后台分页查询文章审核列表。
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @Override
    public PageResult<ArticleReviewPageVO> reviewPage(ArticleReviewPageQueryDTO queryDTO) {
        PermissionUtil.requireAdmin();
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        List<ArticleReviewPageVO> list = articleMapper.selectReviewPage(queryDTO);
        PageInfo<ArticleReviewPageVO> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 审核文章状态。
     *
     * @param id 文章ID
     * @param auditStatus 审核状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, Integer auditStatus) {
        PermissionUtil.requireAdmin();
        validateAuditStatus(auditStatus);
        Long adminId = UserContext.getUser().getUserId();
        int rows = articleMapper.updateAuditStatusById(id, auditStatus, adminId);
        if (rows == 0) {
            throw new RuntimeException("文章不存在或不可审核");
        }
    }

    /**
     * 校验文章状态是否合法。
     *
     * @param status 文章状态
     */
    private void validateStatus(Integer status) {
        if (status == null) {
            throw new RuntimeException("文章状态不能为空");
        }
        if (status != STATUS_DRAFT && status != STATUS_PUBLISHED) {
            throw new RuntimeException("文章状态非法");
        }
    }

    /**
     * 校验审核状态是否合法。
     *
     * @param auditStatus 审核状态
     */
    private void validateAuditStatus(Integer auditStatus) {
        if (auditStatus == null) {
            throw new RuntimeException("审核状态不能为空");
        }
        if (auditStatus != AUDIT_APPROVED && auditStatus != AUDIT_REJECTED) {
            throw new RuntimeException("审核状态非法");
        }
    }

    /**
     * 校验文章发布时的必填字段。
     *
     * @param title 标题
     * @param content 内容
     * @param categoryId 分类ID
     */
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
