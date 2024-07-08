package com.lims.manage.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-07-05 11:11
 * @Copyright © 河南交科院
 */
@Data
public class InternalAuditorActive {
    /**
     * 活动id
     */
    private int activeId;
    /**
     * 分工id
     */
    private int divideId;
    /**
     * 活动名称
     */
    private String name;
    /**
     * 审核开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date startTime;
    /**
     * 审核结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date endTime;
    /**
     * 活动编制时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date editorDate;
    /**
     * 状态：检查待开始，检查中，检查完成，措施验证，已完成
     */
    private String state;
}
