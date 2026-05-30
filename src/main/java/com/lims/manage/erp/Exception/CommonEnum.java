package com.lims.manage.erp.Exception;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.Exception
 * @desc
 * @date 2021/8/30 10:43
 * @Copyright © 河南交科院
 */
public enum CommonEnum implements BaseErrorInfoInterface {
    // 数据操作错误定义
    SUCCESS("200", "成功!"),
    BODY_NOT_MATCH("400","请求的数据格式不符!"),
    SIGNATURE_NOT_PASS("401","token无效或过期!"),
    SIGNATURE_NO_TOKEN("402","token为空!"),
    NOT_FOUND("404", "未找到该资源!"),
    INTERNAL_SERVER_ERROR("500", "服务器内部错误!"),
    SERVER_BUSY("503","服务器正忙，请稍后再试!")
    ;

    /** 错误码 */
    private String resultCode;

    /** 错误描述 */
    private String resultMsg;

    CommonEnum(String resultCode, String resultMsg) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
    }

    @Override
    public String getResultCode() {
        return resultCode;
    }

    @Override
    public String getResultMsg() {
        return resultMsg;
    }
}
