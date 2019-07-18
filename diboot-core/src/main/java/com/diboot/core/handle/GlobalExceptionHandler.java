package com.diboot.core.handle;

import com.diboot.core.exception.RestException;
import com.diboot.core.exception.WebException;
import com.diboot.core.properties.DibootProperties;
import com.diboot.core.util.S;
import com.diboot.core.util.V;
import com.diboot.core.vo.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 异常统一捕获类：只捕获{@link RestException} 和 {@link WebException}及其子类
 *
 * <p>
 * 如果没有特殊要求，系统中可以直接抛出{@link RestException} 和 {@link WebException}异常<br/>
 * 如果想对每个异常有个具体的描述，方便排查，可以继承上述两个类，进行异常细化描述
 * </p>
 * <p>
 * 如果上述两个异常不满足要求，可以自定义异常捕获
 * </p>
 *
 * @author : wee
 * @version : v2.0
 * @Date 2019-07-11  11:13
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private final static Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private DibootProperties dibootProperties;

    /**
     * 捕获{@link RestException}及其子类异常，返回{@link JsonResult}
     *
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(RestException.class)
    public JsonResult advice(RestException e) {
        String msg = V.notEmpty(e.getMsg()) ? S.join(e.getStatus().label(), ":", e.getMsg()) : e.getStatus().label();
        log.error("【rest错误】<== 错误码：{}，错误信息:{}", e.getStatus().code(), msg, e);
        return new JsonResult(e.getStatus(), e.getData(), (V.isEmpty(e.getMsg()) ? "" : e.getMsg()));
    }

    /**
     * 捕获{@link WebException}及其子类异常，
     * <p>如果配置了自定义错误页面，那么跳转到指定的错误页面，否则返回spring错误页面</p>
     * <p>错误页面提供页面类型{@link org.springframework.http.HttpStatus}</p>
     *
     * @param we
     * @return
     */
    @ExceptionHandler(WebException.class)
    public ModelAndView advice(WebException we) {
        log.error("【web错误】<==", we);
        //获取配置信息
        String redirectUrl = dibootProperties.getException().getPage().get(we.getHttpStatus());
        if (V.notEmpty(redirectUrl)) {
            //存在页面跳转至自定义页面
            return new ModelAndView("redirect:" + redirectUrl);
        }
        //默认提示信息
        Map<String, Object> model = new HashMap<>(16);
        model.put("exception", we.getClass().getName());
        model.put("status", we.getHttpStatus().value());
        model.put("message", StringUtils.isEmpty(we.getMsg()) ? "No message available" : we.getMsg());
        model.put("timestamp", new Date());
        return new ModelAndView("error", model);
    }
}
