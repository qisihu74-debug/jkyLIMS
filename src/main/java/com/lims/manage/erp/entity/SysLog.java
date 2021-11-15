package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Date;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2021/11/15 16:33
 * @Copyright © 河南交科院
 */
@Data
@TableName("sys_log")
public class SysLog implements Serializable {
    private Long id;
    /**
     * 日志类型
     */
    private String type;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 用户名称
     */
    private String userName;
    /**
     * 所属组织
     */
    private String userDept;
    /**
     * 操作描述
     */
    private String operateDesc;
    /**
     * 操作时间
     */
    private Date operate_time;


}
