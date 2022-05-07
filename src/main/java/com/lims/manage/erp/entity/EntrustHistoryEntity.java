package com.lims.manage.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lims.manage.erp.vo.TaskCodeVo;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;


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
     * 委托人
     */
    private String entrustPeople;
    /**
     * 委托单位
     */
    private String entrustCompany;
    /**
     * 工程名称
     */
    private String projectName;
    /**
     * 受理日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date acceptanceDate;
    /**
     * 业务受理人
     */
    private String businessAcceptor;
    /**
     * 任务状态
     */
    private Integer state;
    /**
     * 范围 开始受理日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;
    /**
     * 结束受理日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endingDate;
    /**
     * 日期区间
     */
    private String dateInterval;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 当前展示页数量
     */
    private Integer pageSize;
    /**
     * 任务编号
     */
    private List<TaskCodeVo> taskCodes;
    /**
     * 任务编号参数
     */
    private String taskCodeParam;
}
