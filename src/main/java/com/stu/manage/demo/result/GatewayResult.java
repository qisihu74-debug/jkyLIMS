package com.stu.manage.demo.result;

/**
 * 宜搭网关服务请求的返回结果类
 *
 * @Author: weimeng(shanyu)
 * @Date: 2019/2/28 下午2:52
 */

public class GatewayResult {

    private boolean success;

    private String result;

    private String errorCode;

    private String errorMsg;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "GatewayResult{" +
                "success=" + success +
                ", result='" + result + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                '}';
    }

}