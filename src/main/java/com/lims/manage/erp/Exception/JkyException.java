package com.lims.manage.erp.Exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.Exception
 * @desc
 * @date 2021/8/30 10:45
 * @Copyright © 河南交科院
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class JkyException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    protected String code;
    /**
     * 错误信息
     */
    protected String msg;

    public JkyException(String code,String msg) {
        this.code = code;
        this.msg = msg;
    }

}
