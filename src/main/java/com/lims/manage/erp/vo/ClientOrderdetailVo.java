package com.lims.manage.erp.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lims.manage.erp.entity.ReportRecordEntity;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.TaskEntity;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @Author: DLC
 * @Date: 2022/7/19 10:59
 * 委托详情表 返回前端互动
 */
@Data
public class ClientOrderdetailVo {

    /**
     * 委托单id
     */
    private Long entrustmentId;
    /**
     * 委托单编号
     */
    private Integer entrustmentNo;
    /**
     * 委托日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date acceptanceDate;
    /**
     * 要求完成日期
     * */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date requestDate;
    /**
     * 委托人
     */
    private String entrustPeople;
    /**
     * 工程名称
     */
    private String projectName;
    /**
     * 工程部位
     */
    private String projectPart;
    /**
     * 委托单位名称
     */
    private String entrustCompany;
    /**
     * 委托单id
     */
    private Integer entrustCompanyId;
    /**
     * 应收价格变动 实收价格 9月7号
     */
    private double systemPrice;
    /**
     * 样品名称
     */
    @TableField(exist=false)
    private String sampleName;
    /**
     * 规格等级
     */
    private String specs;
    /**
     * 批号/编号
     */
    private String batchNumber;
    /**
     * 检测项名称
     */
    @TableField(exist = false)
    private String checkItemName;
    /**
     * 检测项价格
     */
    @TableField(exist = false)
    private String checkItemNameUnitPrice;
    /**
     * 检测项Id
     */
    @TableField(exist = false)
    private Integer checkItemId;
    /**
     * 任务编号
     */
    @TableField(exist = false)
    private String taskCode;
    /**
     * 报告编号
     */
    @TableField(exist=false)
    private String reportCode;
    /**
     * 报告发放日期
     */
    @TableField(exist=false)
    private String reportTime;

    /**
     * 样品信息
     */
    @TableField(exist=false)
    private List<SampleEntity> samples;

    /**
     * 任务信息
     */
    @TableField(exist=false)
    private List<TaskEntity> taskEntities;

    /**
     * 报告单信息
     */
    @TableField(exist=false)
    private List<ReportRecordEntity> reportRecordEntities;

    /**
     * 当前页码
     */
    @TableField(exist=false)
    private Integer pageNum;

    /**
     * 当前展示页数量
     */
    @TableField(exist=false)
    private Integer pageSize;

    /**
     * 委托开始日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date acceptancebeganDate;

    /**
     * 委托结束日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date acceptanceoverDate;

    /**
     * 委托单位id 数组
     */
    @TableField(exist=false)
    private Integer[] companyIds;

    /**
     * 委托单位id 数组
     */
    @TableField(exist = false)
    private String[] companyStrs;
    /**
     * 委托单编号
     */
    private String entrustmentNostr;
    /**
     * 经营人员
     */
    private String operatingPersonnel;

    /**
     * 开票管理是否开具发票（为空是 否，有值 为是）
     */
    @TableField(exist = false)
    private String isInvoice;

    /**
     * 备注
     */
    @TableField(exist = false)
    private String remark;

    /**
     * 备注
     */
    @TableField(exist = false)
    private List<Long> entrustIds;


}
