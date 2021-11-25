package com.lims.manage.erp.result;

/**
 * @author gjl
 */

public enum ResultEnum {
    //可自行定义，与前端交互
    UNKNOWN_ERROR(-1,"未知错误"),
    SUCCESS(200,"成功"),
    STUDENT_NOT_EXIST(1,"用户不存在"),
    STUDENT_IS_EXISTS(2,"条形码不存在"),
    DATA_IS_NULL(3,"数据为空"),
    DELETE_FAIL(5,"删除失败"),
    UPDATE_FAIL(6,"更新失败"),
    VERIFY_FAIL(7,"验证失败"),
    VERIFY_FAIL_NINE(8,"缺少必要参数"),
    VERIFY_FAIL_ONE(9,"通过钉钉接口获取企业内部token失败"),
    VERIFY_FAIL_TWO(10,"调用钉钉接口获取用户信息失败"),
    VERIFY_FAIL_THRERE(11,"调用钉钉接口获取用户详情信息失败"),
    VERIFY_FAIL_FIVE(12,"调用钉钉接口获取部门信息失败"),
    VERIFY_FAIL_SiX(13,"token无效"),
    VERIFY_FAIL_NICK(14,"请输入账号！"),
    VERIFY_FAIL_PASS_WORD(15,"请输入密码！"),
    CREATE_USER_FAILD(16,"创建用户失败！"),
    CHANGE_USER_STATE(17,"改变用户状态失败！"),
    RESET_PASSWORD(18,"重置密码失败！"),
    UPDATE_PASSWORD(19,"修改密码失败！"),
    UPDATE_USERINFO(20,"修改用户信息失败！");
    private Integer code;
    private String msg;

    ResultEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}