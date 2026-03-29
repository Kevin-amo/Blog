package blog.mapper;


import blog.entity.po.User;
import org.apache.ibatis.annotations.Param;

/**
 * @author admin
 */
public interface UserMapper {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 根据用户ID查询用户。
     *
     * @param id 用户ID
     * @return 用户对象
     */
    User selectById(@Param("id") Long id);

    /**
     * 新增用户
     *
     * @param user 用户对象
     * @return 受影响行数
     */
    int insert(User user);

    /**
     * 根据用户ID更新头像URL。
     *
     * @param id     用户ID
     * @param avatar 头像URL
     * @return 受影响行数
     */
    int updateAvatarById(@Param("id") Long id, @Param("avatar") String avatar);
}
