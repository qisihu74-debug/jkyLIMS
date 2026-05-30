package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-01-02 11:23
 * @Copyright © 河南交科院
 */
@Data
@TableName("test_internal_audit_info")
public class InternalAuditInfo {
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 内审计划id
     */
    private Integer auditId;
    /**
     * 审核员id
     */
    private Long auditorId;
    /**
     * 审核员姓名
     */
    private String auditorName;
    /**
     * 受审部门
     */
    private String deptName;
    /**
     * 材料
     */
    private String fileUrl;

}
