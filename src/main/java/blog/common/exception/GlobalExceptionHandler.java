package blog.common.exception;

import blog.common.Result.Result;
import blog.common.Result.ResultCode;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author admin
 * 全局异常处理
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ForbiddenException.class)
    public Result<?> handleForbiddenException(ForbiddenException e) {
        return Result.fail(ResultCode.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        return Result.fail(ResultCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstrainViolationException(ConstraintViolationException e) {
        return Result.fail(ResultCode.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.fail("系统异常: " + e.getMessage());
    }
}
