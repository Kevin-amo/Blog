package blog.service.impl;

import blog.mapper.CommentMapper;
import blog.model.LoginUser;
import blog.pojo.po.Comment;
import blog.service.SensitiveWordService;
import blog.util.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private SensitiveWordService sensitiveWordService;

    @InjectMocks
    private CommentServiceImpl commentService;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void addCommentShouldWriteCurrentUserAsCreateBy() {
        UserContext.setUser(new LoginUser(7L, "alice", 0));
        Comment comment = new Comment();
        comment.setArticleId(11L);
        comment.setParentId(0L);
        comment.setNickname("Alice");
        comment.setContent("正常内容");

        when(sensitiveWordService.contains("正常内容")).thenReturn(false);
        when(commentMapper.insertComment(any(Comment.class))).thenAnswer(invocation -> {
            Comment saved = invocation.getArgument(0);
            Assertions.assertEquals(7L, saved.getCreateBy());
            Assertions.assertEquals(0, saved.getIsDeleted());
            Assertions.assertEquals(1, saved.getStatus());
            return 1;
        });

        int rows = commentService.addComment(comment);

        Assertions.assertEquals(1, rows);
        verify(commentMapper).insertComment(comment);
    }

    @Test
    void deleteShouldRemoveOwnedComment() {
        UserContext.setUser(new LoginUser(9L, "bob", 0));
        when(commentMapper.deleteByIdAndCreateBy(5L, 9L)).thenReturn(1);

        commentService.delete(5L);

        verify(commentMapper).deleteByIdAndCreateBy(5L, 9L);
    }

    @Test
    void deleteShouldRejectNonOwnedComment() {
        UserContext.setUser(new LoginUser(9L, "bob", 0));
        when(commentMapper.deleteByIdAndCreateBy(5L, 9L)).thenReturn(0);

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> commentService.delete(5L));

        Assertions.assertEquals("评论不存在或无权删除", exception.getMessage());
    }
}
