package blog.mapper;

import blog.pojo.po.Comment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper {

    /**
     * 新增评论
     * @param comment 评论内容
     */
    @Insert("INSERT INTO comment(article_id, parent_id, nickname, content, status, is_deleted, create_time) VALUES " +
            "(#{articleId}, #{parentId}, #{nickname}, #{content}, #{status}, #{isDeleted}, #{createTime})")
    int insertComment(Comment comment);

    /**
     * 通过文章id查找
     * @param articleId 文章id
     */
    @Select("SELECT id, article_id AS articleId, parent_id AS parentId, nickname, content, status, is_deleted, create_time AS createTime" +
            " FROM comment WHERE article_id = #{articleId} AND is_deleted = 0 AND status = 1 ORDER BY create_time DESC")
    List<Comment> selectByArticleId(@Param("articleId") Long articleId);
    
}
