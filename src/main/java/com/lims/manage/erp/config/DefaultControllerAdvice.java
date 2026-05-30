package com.lims.manage.erp.config;

import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @author gjl
 */


@ControllerAdvice
@ResponseBody
public class DefaultControllerAdvice {

    @ExceptionHandler(Exception.class)
    public Result exceptionHandler() {

        return ResultUtil.error(ResultEnum.UNKNOWN_ERROR.getCode(),ResultEnum.UNKNOWN_ERROR.getMsg());
    }
}
