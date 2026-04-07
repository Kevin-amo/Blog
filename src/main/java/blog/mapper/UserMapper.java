package blog.mapper;

import blog.pojo.dto.UserAdminPageQueryDTO;
import blog.pojo.po.User;
import blog.pojo.vo.UserAdminPageVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户 mapper
 */
public interface UserMapper {

    User selectByUsername(@Param("username") String username);

    User selectById(@Param("id") Long id);

    int insert(User user);

    int updateAvatarById(@Param("id") Long id, @Param("avatar") String avatar);

    int updateNicknameById(@Param("id") Long id, @Param("nickname") String nickname);

    int updatePasswordById(@Param("id") Long id, @Param("password") String password);

    List<UserAdminPageVO> selectUserPage(@Param("queryDTO") UserAdminPageQueryDTO queryDTO);

    int updateStatusByIdAndRole(@Param("id") Long id, @Param("role") Integer role, @Param("status") Integer status);

    int resetPasswordByIdAndRole(@Param("id") Long id, @Param("role") Integer role, @Param("password") String password);

    int deleteByIdAndRole(@Param("id") Long id, @Param("role") Integer role);
}
