package diamond.cms.server.mvc.advice;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;

import diamond.cms.server.core.Result;
import diamond.cms.server.core.exceptions.AppException;
import diamond.cms.server.core.exceptions.AuthorizationException;
import diamond.cms.server.core.exceptions.Error;
import diamond.cms.server.core.exceptions.ParamValidException;
import diamond.cms.server.core.exceptions.UserNotInitException;

@ControllerAdvice
@ResponseBody
public class ExceptionControllerAdvice{

    Logger log = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(AppException.class)
    public Result appException(AppException ex) {
        return appExceptionToResult(ex);
    }

    @ExceptionHandler(AuthorizationException.class)
    public Result authException(HttpServletResponse response) {
        response.setStatus(401);
        return appExceptionToResult(new AppException(Error.UN_AUTHORIZATION));
    }

    @ExceptionHandler(UserNotInitException.class)
    public Result usernotinit(HttpServletResponse response) {
        response.setStatus(403);
        return appExceptionToResult(new AppException(Error.USER_ACCESS_NOT_INIT));
    }

    @ExceptionHandler(ParamValidException.class)
    public Result paramValidExceptionHandler(ParamValidException ex, HttpServletResponse response) {
        response.setStatus(400);
        Result result = appException(new AppException(Error.INVALID_PARAMS, ex.getMessage()));
        result.setData(ex.getFieldErrors());
        return result;
    }

    @ExceptionHandler(BindException.class)
    public Result bindExceptionHandler(BindException ex, HttpServletResponse response){
        return paramValidExceptionHandler(new ParamValidException(ex), response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result constraintViolationExceptionHandler(ConstraintViolationException ex, HttpServletResponse response, HandlerMethod handlerMethod) {
        return paramValidExceptionHandler(new ParamValidException(ex, handlerMethod.getMethodParameters()), response);
    }

    @ExceptionHandler(Exception.class)
    public Result exception(Exception ex, HttpServletResponse response) {
        response.setStatus(500);
        log.error(ex.getMessage(), ex);
        return appExceptionToResult(new AppException(Error.UNKNOW_EXCEPTION));
    }

    private Result appExceptionToResult(AppException ex) {
        Result result = new Result();
        result.setSuccess(false);
        result.setCode(ex.getCode());
        result.setMsg(ex.getMessage());
        return result;
    }

}
