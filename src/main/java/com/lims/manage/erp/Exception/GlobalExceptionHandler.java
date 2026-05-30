package com.lims.manage.erp.Exception;

import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.config
 * @desc
 * @date 2021/8/30 10:31
 * @Copyright © 河南交科院
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理自定义的业务异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = JkyException.class)
    @ResponseBody
    public  ResultBody bizExceptionHandler(HttpServletRequest req,JkyException e){
        logger.error("发生业务异常！原因是：{}",e.getCode());
        return ResultBody.error(e.getCode(),e.getMsg());
    }

    /**
     * 处理空指针的异常
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value =NullPointerException.class)
    @ResponseBody
    public ResultBody exceptionHandler(HttpServletRequest req, NullPointerException e){
        logger.error("发生空指针异常！原因是:",e);
        return ResultBody.error(CommonEnum.BODY_NOT_MATCH);
    }


    /**
     * 处理其他异常
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value =Exception.class)
    @ResponseBody
    public ResultBody exceptionHandler(HttpServletRequest req, Exception e){
        logger.error("未知异常！原因是:",e);
        return ResultBody.error(CommonEnum.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理Shiro权限拦截异常
     * 如果返回JSON数据格式请加上 @ResponseBody注解
     * @Author gjl
     * @CreateTime 2021/11/09 13:35
     * @Return Map<Object> 返回结果集
     */
    @ResponseBody
    @ExceptionHandler(value = AuthorizationException.class)
    public Result defaultErrorHandler(){
        Map<String,Object> map = new HashMap<>();
        map.put("403","权限不足");
        return ResultUtil.success(map);
    }
}
