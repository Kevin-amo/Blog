package blog.mapper;


import blog.entity.po.User;
import org.apache.ibatis.annotations.Param;

/**
 * @author admin
 */
public interface UserMapper {

    User selectByUsername(@Param("username") String username);

}
