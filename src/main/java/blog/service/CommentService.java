package blog.service;

import blog.pojo.po.Comment;

import java.util.List;

public interface CommentService {

    /**
     * 添加评论
     * @param comment 评论
     * @return int
     */
    int addComment(Comment comment);

    /**
     * 文章 id 查评论
     * @param articleId 文章id
     * @return 评论列表
     */
    List<Comment> listByArticleId(Long articleId);

    /**
     * 删除当前用户自己的评论
     * @param id 评论id
     */
    void delete(Long id);

}
