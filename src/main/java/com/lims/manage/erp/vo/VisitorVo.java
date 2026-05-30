package com.lims.manage.erp.vo;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2024/12/17 11:40
 * 访客模式 实体类
 */
@Data
public class VisitorVo {

    /**
     * 被访人唯一标识,
     */
    private String receptionistId;
    /**
     * 预计来访时间
     */
    private String visitStartTime;
    /**
     * 预计离开时间
     */
    private String visitEndTime;
    /**
     * 访客姓名:
     */
    private String visitorName;
    /**
     * 性别
     */
    private String gender;
    /**
     * 联系电话
     */
    private String phoneNo;
}
