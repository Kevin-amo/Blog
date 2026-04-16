package blog.mapper;

import blog.pojo.po.Comment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CommentMapper {

    /**
     * 新增评论
     * @param comment 评论内容
     */
    @Insert("INSERT INTO comment(article_id, parent_id, nickname, content, status, is_deleted, create_by, create_time) VALUES " +
            "(#{articleId}, #{parentId}, #{nickname}, #{content}, #{status}, #{isDeleted}, #{createBy}, #{createTime})")
    int insertComment(Comment comment);

    /**
     * 通过文章id查找
     * @param articleId 文章id
     */
    @Select("""
            SELECT
                c.id,
                c.article_id AS articleId,
                c.parent_id AS parentId,
                c.nickname,
                CASE WHEN c.is_deleted = 1 THEN '该评论已删除' ELSE c.content END AS content,
                c.status,
                c.is_deleted AS isDeleted,
                c.create_by AS createBy,
                c.create_time AS createTime
            FROM comment c
            WHERE c.article_id = #{articleId}
              AND c.status = 1
              AND (
                  c.is_deleted = 0
                  OR (
                      c.parent_id = 0
                      AND c.is_deleted = 1
                      AND EXISTS (
                          SELECT 1
                          FROM comment child
                          WHERE child.parent_id = c.id
                            AND child.status = 1
                            AND child.is_deleted = 0
                      )
                  )
              )
            ORDER BY c.create_time DESC
            """)
    List<Comment> selectByArticleId(@Param("articleId") Long articleId);

    /**
     * 按作者逻辑删除评论
     * @param id 评论id
     * @param currentUserId 当前用户id
     */
    @Update("UPDATE comment SET is_deleted = 1 WHERE id = #{id} AND create_by = #{currentUserId} AND is_deleted = 0")
    int deleteByIdAndCreateBy(@Param("id") Long id, @Param("currentUserId") Long currentUserId);
    
}
