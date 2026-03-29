package blog.common.constant;

/**
 * @author admin
 * 文章常量
 */
public final class ArticleConstants {

    private ArticleConstants() {
    }

    /**
     * 草稿
     */
    public static final int STATUS_DRAFT = 0;

    /**
     * 已提交发布（需要审核）
     */
    public static final int STATUS_PUBLISHED = 1;

    /**
     * 待审核
     */
    public static final int AUDIT_PENDING = 0;

    /**
     * 审核通过
     */
    public static final int AUDIT_APPROVED = 1;

    /**
     * 审核驳回
     */
    public static final int AUDIT_REJECTED = 2;
}
