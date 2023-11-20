package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lims.manage.erp.vo.ReportPreserveVo;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("test_report_record")
public class ReportRecordEntity {
    private Long id;
    /**
     * 委托单id
     */
    private Long entrustmentId;
    /**
     * 委托单ID用于中间报告数据查询
     */
    private Long entrustId;
    /**
     * 委托单实际价格
     */
    @TableField(exist = false)
    private String actualPrice;
    /**
     * 检测人
     */
    @TableField(exist = false)
    private String inspector;
    /**
     * 记录人
     */
    @TableField(exist = false)
    private String recorder;
    /**
     * 报告编号
     */
    private String reportCode;
    /**
     * 备注描述报告参与的检测到签发人员
     */
    private String note;
    /**
     * 样品名称
     */
    private String sampleName;
    @TableField(exist = false)
    private String aliasName;
    /**
     * 本单价钱
     */
    private String price;
    /**
     * 要求完成时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date requiredCompletionTime;
    /**
     * 任务编号
     */
    private String taskCode;
    /**
     * 状态报告状态，0报告被驳回 1指标填写已完成，2指标填写未完成，3.审批已抢单，
     * 4.签发待抢单，5.签发已抢单，6已签发，7盖章，8已邮寄
     */
    private String state;
    /**
     * 报告份数
     */
    private Integer number;
    /**
     * 取报告方式
     */
    private String reportType;
    /**
     * 审批时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date verifyerTime;
    /**
     * 签发时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date issuerTime;
    /**
     * 盖章类型
     */
    private String sealType;
    /**
     * 审批驳回原因
     */
    private String applyReason;
    /**
     * 签发驳回原因
     */
    private String issuReason;
    /**
     * 审核人姓名
     */
    private String verifyer;
    /**
     * 审批人id
     */
    private Long verifyerId;

    /**
     * 签发人
     */
    private String issuer;
    /**
     * 签发人id
     */
    private Long issuerId;
    /**
     * 报告提交申请人
     */
    private String applicant;
    /**
     * 盖章人
     */
    private String sealer;
    /**
     * 盖章时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date sealTime;
    /**
     * 报告发出人
     */
    private String reportManager;
    /**
     * 报告发出时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date reportTime;
    /**
     * 领取报告人员
     */
    private String addressee;
    /**
     * 运单编号
     */
    private String waybill;
    /**
     * 操作时间，报告发出后录入数据时的时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date operateTime;
    /**
     * 报告模板url
     */
    private String reportUrl;
    /**
     * 印章url
     */
    private String sealUrl;
    /**
     * 电子邮箱
     */
    private String email;
    /**
     * 收件电话
     */
    private String reportPhone;

    /**
     *邮寄地址
     */
    private String reportMailingAddress;
    /**
     * 报告生成时间（state=1时）
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date reportCompleteTime;
    /**
     * 报告模板名称
     */
    private String templateName;
    @TableField(exist = false)
    private Integer productId;
    /**
     * 契约锁文档标识
     */
    private String qysDocmentId;
    /**
     * 契约锁报告状态1合同待发起,2合同待创建，3合同待签署，4合同待盖章，5合同待下载，6已下载
     */
    private String qysState;
    /**
     * 契约锁合同id
     */
    private String contractId;
    /**
     * 契约锁合同盖章url地址
     */
    private String signUrl;

    private Long taskId;
    @TableField(exist = false)
    private String entrustmentNo;
    @TableField(exist = false)
    private String entrustCompany;
    /**
     * 报告合并时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date combineTime;
    /**
     * 委托编号——String
     */
    @TableField(exist = false)
    private String entrustmentNostr;


    public ReportRecordEntity() {
    }

    public ReportRecordEntity(ReportPreserveVo vo) {
        this.id = vo.getId() == null ? null : vo.getId();
        this.entrustmentId = vo.getEntrustmentId() == null ? null : vo.getEntrustmentId();
        this.reportCode = vo.getReportCode() == null ? null : vo.getReportCode();
        this.sampleName = vo.getSampleName() == null ? null : vo.getSampleName();
        this.aliasName = vo.getAliasName() == null ? null : vo.getAliasName();
        this.price = vo.getPrice() == null ? null : vo.getPrice();
        this.requiredCompletionTime = vo.getRequiredCompletionTime() == null ? null : vo.getRequiredCompletionTime();
        this.taskCode = vo.getTaskCode() == null ? null : vo.getTaskCode();
        this.state = vo.getState() == null ? null : vo.getState();
        this.number = vo.getNumber() == null ? null : vo.getNumber();
        this.reportType = vo.getReportType() == null ? null : vo.getReportType();
        this.verifyerTime = vo.getVerifyerTime() == null ? null : vo.getVerifyerTime();
        this.issuerTime = vo.getIssuerTime() == null ? null : vo.getIssuerTime();
        this.sealType = vo.getSealType() == null ? null : vo.getSealType();
        this.applyReason = vo.getApplyReason() == null ? null : vo.getApplyReason();
        this.issuReason = vo.getIssuReason() == null ? null : vo.getIssuReason();
        this.verifyer = vo.getVerifyer() == null ? null : vo.getVerifyer();
        this.issuer = vo.getIssuer() == null ? null : vo.getIssuer();
        this.applicant = vo.getApplicant() == null ? null : vo.getApplicant();
        this.sealer = vo.getSealer() == null ? null : vo.getSealer();
        this.sealTime = vo.getSealTime() == null ? null : vo.getSealTime();
        this.reportManager = vo.getReportManager() == null ? null : vo.getReportManager();
        this.reportTime = vo.getReportTime() == null ? null : vo.getReportTime();
        this.addressee = vo.getAddressee() == null ? null : vo.getAddressee();
        this.waybill = vo.getWaybill() == null ? null : vo.getWaybill();
        this.operateTime = vo.getOperateTime() == null ? null : vo.getOperateTime();
        this.taskId = vo.getTaskId() == null ? null : vo.getTaskId();
    }
    public ReportRecordEntity(ReportPreserveVo vo,Long entrustId) {
        this.reportCode = vo.getReportCode() == null ? null : vo.getReportCode();
        this.sampleName = vo.getSampleName() == null ? null : vo.getSampleName();
        this.price = vo.getPrice() == null ? null : vo.getPrice();
        this.requiredCompletionTime = vo.getRequiredCompletionTime() == null ? null : vo.getRequiredCompletionTime();
        this.taskCode = vo.getTaskCode() == null ? null : vo.getTaskCode();
        this.state = vo.getState() == null ? null : vo.getState();
        this.number = vo.getNumber() == null ? null : vo.getNumber();
        this.reportType = vo.getReportType() == null ? null : vo.getReportType();
        this.verifyerTime = vo.getVerifyerTime() == null ? null : vo.getVerifyerTime();
        this.issuerTime = vo.getIssuerTime() == null ? null : vo.getIssuerTime();
        this.sealType = vo.getSealType() == null ? null : vo.getSealType();
        this.applyReason = vo.getApplyReason() == null ? null : vo.getApplyReason();
        this.issuReason = vo.getIssuReason() == null ? null : vo.getIssuReason();
        this.verifyer = vo.getVerifyer() == null ? null : vo.getVerifyer();
        this.issuer = vo.getIssuer() == null ? null : vo.getIssuer();
        this.applicant = vo.getApplicant() == null ? null : vo.getApplicant();
        this.sealer = vo.getSealer() == null ? null : vo.getSealer();
        this.sealTime = vo.getSealTime() == null ? null : vo.getSealTime();
        this.reportManager = vo.getReportManager() == null ? null : vo.getReportManager();
        this.reportTime = vo.getReportTime() == null ? null : vo.getReportTime();
        this.addressee = vo.getAddressee() == null ? null : vo.getAddressee();
        this.waybill = vo.getWaybill() == null ? null : vo.getWaybill();
        this.operateTime = vo.getOperateTime() == null ? null : vo.getOperateTime();
        this.taskId = vo.getTaskId() == null ? null : vo.getTaskId();
        this.entrustId = entrustId;
    }

    /**
     * 报告类型 （0最终报告,1中间报告）
     */
    private String type;

    /**
     * 印章分类，PHYSICS(物理章),ELECTRONIC(电子章)
     */
    private String category;
    /**
     * 操作类型0线上编辑的报告，1线下编辑的报告
     */
    private Integer operateType;
}
