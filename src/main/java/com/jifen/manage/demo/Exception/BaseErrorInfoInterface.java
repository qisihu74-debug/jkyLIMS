package com.jifen.manage.demo.Exception;

/**
 * @author gjl
 * @version V1.0
 * @Package com.jifen.manage.demo.config
 * @desc
 * @date 2021/8/30 10:42
 * @Copyright © 河南交科院
 */
public interface BaseErrorInfoInterface {
    /** 错误码*/
    String getResultCode();

    /** 错误描述*/
    String getResultMsg();
}
