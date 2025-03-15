package com.pansophicmind.server.aidog.common.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pansophicmind.server.web.exception.BadSqlGrammarException;
import com.pansophicmind.server.web.exception.BaseException;
import com.pansophicmind.server.web.exception.NoAccessException;
import com.pansophicmind.server.web.exception.NoLoginException;
import com.pansophicmind.server.web.feign.FeignResponseResultVO;
import com.pansophicmind.server.web.feign.enums.FeignResponseCodeEnum;
import com.pansophicmind.server.web.feign.util.FeignResponseResultUtil;
import com.pansophicmind.server.web.utils.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

/**
 * 全局异常处理类
 * <p>
 * 建言
 *
 * @author Sun
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 登录过期
     */
    public FeignResponseResultVO handleNoLoginException() {
        return FeignResponseResultUtil.fail(FeignResponseCodeEnum.LOGIN_EXPIRED);
    }

    /**
     * 无操作权限
     */
    public FeignResponseResultVO handleNoAccessException(NoAccessException e) {
        return FeignResponseResultUtil.fail(e.getMessage());
    }

    /**
     * 公共异常
     */
    public FeignResponseResultVO handleException(BaseException e) {
        return FeignResponseResultUtil.fail(e.getMessage());
    }

    /**
     * 实体校验报错处理
     */
    public FeignResponseResultVO handleException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        StringBuilder sb = new StringBuilder("参数异常：");
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            // 有错误提示信息，取提示信息，没有则取字段名
            String defaultMessage = fieldError.getDefaultMessage(); // 默认值：不能为空/不能为null
            if (StringUtils.isEmpty(defaultMessage)) {
                sb.append(fieldError.getField()).append("；");
            } else {
                defaultMessage = defaultMessage.replace("null", "空");
                sb.append("不能为空".equals(defaultMessage) ? fieldError.getField() + defaultMessage : defaultMessage);
            }
        }
        return FeignResponseResultUtil.fail(sb.toString());
    }

    /**
     * 实体校验报错处理
     */
    public FeignResponseResultVO handleException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        StringBuilder sb = new StringBuilder("参数异常：");
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            // 有错误提示信息，取提示信息，没有则取字段名
            String defaultMessage = fieldError.getDefaultMessage(); // 默认值：不能为空/不能为null
            if (StringUtils.isEmpty(defaultMessage)) {
                sb.append(fieldError.getField()).append("；");
            } else {
                defaultMessage = defaultMessage.replace("null", "空");
                sb.append("不能为空".equals(defaultMessage) ? fieldError.getField() + defaultMessage : defaultMessage);
            }
        }
        return FeignResponseResultUtil.fail(sb.toString());
    }

    /**
     * 实体校验报错处理
     */
    public FeignResponseResultVO handleException(MethodArgumentTypeMismatchException e) {
        return FeignResponseResultUtil.fail("参数异常：" + e.getName());
    }

    /**
     * 非法参数异常
     */
    public FeignResponseResultVO handleException(IllegalArgumentException e) {
        return FeignResponseResultUtil.fail(e.getMessage());
    }

    /**
     * 单个数据检验报错处理
     */
    public FeignResponseResultVO handleException(ConstraintViolationException e) {
        return FeignResponseResultUtil.fail(e.getMessage());
    }

    /**
     * sql异常处理
     */
    public FeignResponseResultVO handleException(BadSqlGrammarException e) {
        return FeignResponseResultUtil.fail(e.getMessage());
    }

    /**
     * 全局异常处理
     */
    @ExceptionHandler(value = Exception.class)
    public FeignResponseResultVO defaultErrorHandler(HttpServletRequest request, HttpServletResponse response, Exception e) {
        String ip = RequestUtil.getRealIpAddr(request);
        String url = request.getRequestURL().toString();
        log.error(ip + "-" + url);
        log.error(e.getClass().getName());
        log.error(e.getMessage());
        response.setStatus(HttpStatus.OK.value());
        if (e instanceof NoLoginException) {
            return handleNoLoginException();
        } else if (e instanceof NoAccessException) {
            return handleNoAccessException((NoAccessException) e);
        } else if (e instanceof BaseException) {
            return handleException((BaseException) e);
        } else if (e instanceof BindException) {
            return handleException((BindException) e);
        } else if (e instanceof MethodArgumentNotValidException) {
            return handleException((MethodArgumentNotValidException) e);
        } else if (e instanceof MethodArgumentTypeMismatchException) {
            return handleException((MethodArgumentTypeMismatchException) e);
        } else if (e instanceof JsonProcessingException) {
            return FeignResponseResultUtil.fail("参数解析异常！");
        } else if (e instanceof HttpMessageConversionException) {
            return FeignResponseResultUtil.fail("参数转换异常！");
        } else if (e instanceof IllegalArgumentException) {
            return handleException((IllegalArgumentException) e);
        } else if (e instanceof ConstraintViolationException) {
            return handleException((ConstraintViolationException) e);
        } else if (e instanceof BadSqlGrammarException) {
            return handleException((BadSqlGrammarException) e);
        } else if (e instanceof DataAccessException || e instanceof PersistenceException) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return FeignResponseResultUtil.fail("数据访问异常，请稍后重试！");
        } else {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return FeignResponseResultUtil.fail(e.getMessage());
        }
    }

    /**
     * 增加返回参数
     */
    @ModelAttribute
    public void modelAttribute(Model model) {
        // model.addAttribute("", "");
    }

    /**
     * 重新对数据进行定义
     */
    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        // webDataBinder.setDisallowedFields("");
    }

}
