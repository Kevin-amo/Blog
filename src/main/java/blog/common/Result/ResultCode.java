package blog.common.Result;

/**
 * @author admin
 */
public interface ResultCode {
    Integer SUCCESS = 200;
    Integer ERROR = 500;
    Integer UNAUTHORIZED = 401;
    Integer FORBIDDEN = 403;
    Integer BAD_REQUEST = 400;
}