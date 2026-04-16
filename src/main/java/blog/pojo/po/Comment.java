package blog.pojo.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {

    private Long id;

    private Long articleId;

    private Long parentId;

    private String nickname;

    private String content;

    private Integer status;

    private Integer isDeleted;

    private Long createBy;

    private LocalDateTime createTime;
}
