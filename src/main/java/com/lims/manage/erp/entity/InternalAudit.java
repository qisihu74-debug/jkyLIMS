package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-01-02 11:12
 * @Copyright © 河南交科院
 */
@Data
@TableName("test_internal_audit")
public class InternalAudit {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 计划名称
     */
    private String name;
    /**
     * 创建人ID
     */
    private Long operateId;
    /**
     * 创建人姓名
     */
    private String operateName;
    /**
     * 计划创建日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date operateDate;
    /**
     * 内审日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date auditDate;
    /**
     * 审核组长ID
     */
    private Long auditLeaderId;
    /**
     * 审核组长姓名
     */
    private String auditLeaderName;
    /**
     * 审核状态，进行中，已完成
     */
    private String state;
    /**
     * 内审附件
     */
    private String fileUrl;
    /**
     * 技术质量部最终上传的报告附件url
     */
    private String reportUrl;
    /**
     * 内审详情信息列表
     */
    @TableField(exist = false)
    private List<InternalAuditInfo> list;

}
