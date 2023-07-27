package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lims.manage.erp.entity.SampleCirculationRecord;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @Author: DLC
 * @Date: 2023/3/1 15:25
 *
 * 样品留样、与样品出入库
 */
@Data
public class SampleOutPutVo {
    /**
     * 样品id
     */
    private Integer sampleId;
    /**
     * 任务单号
     */
    private String taskCode;
    /**
     * 样品编号
     */
    private String sampleCode;
    /**
     * 样品名称
     */
    private String sampleName;
    /**
     * 留样天数
     */
    private Integer sampleRetentionPeriod;
    /**
     * 样品处置方式
     */
    private String sampleProcessMode;
    /**
     * 样品留样备注
     */
    private String sampleReservedRemrk;
    /**
     * 样品出入库备注
     */
    private String sampleOutPutRemrk;
    /**
     * 受理日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date acceptanceDate;
    /**
     * 要求完成日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date requestDate;
    /**
     * 留样人
     */
    private String sampleHolder;
    /**
     * 处理人
     */
    private String handler;
    /**
     * 处理日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date sellOffDate;
    /**
     * 开始日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date startTime;
    /**
     * 结束日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date endTime;
    /**
     *样品流转记录信息单
     */
    List<SampleCirculationRecord> sampleCirculationRecords;
    /**
     * 页码
     */
    private Integer pageNum;
    /**
     * 每页展示数量
     */
    private Integer pageSize;

    /**
     * 样品接收人（任务发布人）
     */
    private String taskPublisher;

    /**
     * 领样人
     */
    private String sampleTaker;

    /**
     *  出库日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date outboundDeliveryDate;
    /**
     * 技术负责人
     */
    private String approver;

    private String entrustPeople;
    /**
     * 状态，1待检，2在检，3已检，4留样，5处置
     */
    private String status;
}
