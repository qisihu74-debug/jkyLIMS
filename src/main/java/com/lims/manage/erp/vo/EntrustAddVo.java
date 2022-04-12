package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lims.manage.erp.entity.SampleEntity;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
@Data
public class EntrustAddVo {
    /**
     * 委托单id
     */
    private Long id;
    /**
     * 编号
     */
    private Integer entrustmentNo;
    /**
     * 委托方式
     */
    private String entrustType;
    /**
     * 委托单位
     */
    private String entrustCompany;
    /**
     * 委托单位id
     */
    private Integer entrustCompanyId;
    /**
     * 委托人
     */
    private String entrustPeople;
    /**
     * 委托人联系方式
     */
    private String entrustPhone;
    /**
     * 见证单位
     */
    private String witnessUint;
    /**
     * 见证人
     */
    private String witnessPerson;
    /**
     * 见证人联系方式
     */
    private String witnessPhone;
    /**
     * 工程名称
     */
    private String projectName;
    /**
     * 工程部位
     */
    private String projectPart;
    /**
     * 取样方式
     */
    private String samplingMethod;
    /**
     * 检测目的
     */
    private String checkPurpose;
    /**
     * 报告份数
     */
    private Integer reportCount;
    /**
     * 取报告方式
     */
    private String reportType;
    /**
     * 要求完成时间
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date requestDate;
    /**
     * 支付方式
     */
    private String paymentMethod;
    /**
     * 缴费记录(实际已收费）
     */
    private String paymentRecord;
    /**
     * 缴费记录回显数据
     */
    private String paymentRecordShow;
    /**
     * 应收总计
     */
    private String paymentCount;
    /**
     * 提供资料
     */
    private String presentInformation;
    /**
     * 受理日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date acceptanceDate;
    /**
     * 指定检测团队
     */
    private Integer team;
    /**
     * 状态
     */
    private Integer state;
    /**
     * 备注
     */
    private String remark;
    /**
     * 作废原因
     */
    private String invalidReason;
    /**
     * 作废操作人
     */
    private Long operateUser;
    /**
     * 作废操作人String
     */
    private String operateUserStr;
    /**
     * 作废操作日期
     */
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern ="yyyy-MM-dd" , timezone ="GMT+8")
    private Date operateDate;
    /**
     * 附件url
     */
    private String fileUrl;
    /**
     * 盖章类型25甲级，26，27
     */
    private String sealType;
    /**
     * 是否留样1.保留2.废弃
     */
    private String isSave;
    /**
     * 业务受理人
     */
    private String businessAcceptor;
    /**
     * 样品信息
     */
    private List<SampleEntity> samples;
    /**
     * 邮寄地址
     */
    private String address;
    /**
     * 收件人
     */
    private String addressee;
    /**
     * 收件人电话
     */
    private String mobile;
    /**
     * 邮箱地址
     */
    private String mailbox;
    /**
     * 上传附件url原始名称
     */
    private String fileUrlStr;

    /**
     * 用章类型数组形式
     */
    private String[] sealTypes;

    /**
     * 委托检测类别（原材检测 配合比）
     */
    private String entrustTestType;

    /**
     *设计强度（MPa）
     */
    private String designStrength;

    /**
     * 配制强度（MPa）
     */
    private String  intensityOfConfiguration;

    /**
     * 抗（渗、冻）等级
     */
    private String antifreezeLevel;

    /**
     *水胶比
     */
    private String waterBinderRatio;

    /**
     * 单位用水量（kg）
     */
    private String unitWaterUse;

    /**
     * 砂率（%）
     */
    private String sandRatio;

    /**
     * 设计坍落度（mm）
     */
    private String designSlump;

    /**
     * 拌和方式
     */
    private String mixingWay;

}
