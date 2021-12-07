package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.Date;


/**
 * @Author: DLC
 * @Date: 2021/12/1 10:05
 * 历史委托
 */
@Data
public class EntrustHistoryEntity {
    /**
     * 委托主键
     */
    private Long id;
    /**
     * 委托编号
     */
    private String entrustmentNo;
    /**
     * 样品名称
     */
    private String sampleName;
    /**
     * 委托人
     */
    private String entrustPeople;
    /**
     * 工程名称
     */
    private String projectName;
    /**
     * 受理日期
     */
    private Date requestDate;
    /**
     * 委托公司
     */
    private String entrustCompany;
    /**
     * 业务受理人
     */
    private String businessAcceptor;
    /**
     * 任务状态
     */
    private Integer state;

}
