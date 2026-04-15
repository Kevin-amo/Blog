package blog.service.impl;

import blog.mapper.CommentMapper;
import blog.pojo.po.Comment;
import blog.service.CommentService;
import blog.service.SensitiveWordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private SensitiveWordService sensitiveWordService;

    @Override
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("评论不能为空！");
        }
        if (comment.getArticleId() == null) {
            throw new IllegalArgumentException("文章id不能为空！");
        }
        String content = comment.getContent() == null ? "" : comment.getContent().trim();
        if (content.isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空！");
        }

        String nickname = comment.getNickname();
        if (nickname == null || nickname.trim().isEmpty()) {
            nickname = "游客";
        } else {
            nickname = nickname.trim();
        }

        // 评论内容校验，替换成*
        if (sensitiveWordService.contains(content)) {
            String replaced = sensitiveWordService.replace(content);
            comment.setContent(replaced);
        // 没有违禁词
        } else {
            comment.setContent(content);
        }
        comment.setNickname(nickname);

        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }

        comment.setStatus(1);
        comment.setIsDeleted(0);
        comment.setCreateTime(LocalDateTime.now());

        return commentMapper.insertComment(comment);
    }

    @Override
    public List<Comment> listByArticleId(Long articleId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        return commentMapper.selectByArticleId(articleId);
    }
}
