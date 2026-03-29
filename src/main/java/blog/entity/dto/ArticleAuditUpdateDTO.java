package blog.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author admin
 *
 * 管理员审核文章 DTO
 */
@Data
public class ArticleAuditUpdateDTO {

    /**
     * 1 审核通过，2 审核驳回
     */
    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;
}
