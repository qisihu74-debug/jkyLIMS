package com.lims.manage.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lims.manage.erp.vo.EntrustAddVo;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.text.DecimalFormat;
import java.util.Date;
@Data
public class EntrustEntity {
    /**
     * 主键
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
     * 业务受理人
     */
    private String businessAcceptor;
    /**
     * 委托单位名称
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
     * 委托人ID
     */
    private Integer entrustPeopleId;
    /**
     * 见证人ID
     */
    private Integer witnessPersonId;
    /**
     * 总计应收款
     */
    private String countPrice;
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
    /**
     * 任务编号
     */
    private String taskCode;
    /**
     * 任务来源
     */
    private String taskSource;
    /**
     * 折扣率
     */
    private String discount;
    /**
     * 实收价格
     */
    private String actualPrice;
    /**
     * 应收价格
     */
    private String systemPrice;

    /**
    * 所属部门id
    */
    private Long department;

    DecimalFormat df = new DecimalFormat("#.00");


    public EntrustEntity(Long id, Integer entrustmentNo, String entrustType, String entrustPeople, String entrustPhone,
                         String witnessUint, String witnessPerson, String witnessPhone, String projectName,
                         String projectPart, String samplingMethod, String checkPurpose, Integer reportCount,
                         String reportType, Date requestDate, String paymentMethod, String paymentRecord,
                         String paymentCount, String presentInformation, Date acceptanceDate, Integer team,
                         Integer state, String remark, String invalidReason, Long operateUser, Date operateDate,
                         String fileUrl, String sealType, String isSave, Integer entrustPeopleId,
                         Integer witnessPersonId) {
        this.id = id;
        this.entrustmentNo = entrustmentNo;
        this.entrustType = entrustType;
        this.entrustPeople = entrustPeople;
        this.entrustPhone = entrustPhone;
        this.witnessUint = witnessUint;
        this.witnessPerson = witnessPerson;
        this.witnessPhone = witnessPhone;
        this.projectName = projectName;
        this.projectPart = projectPart;
        this.samplingMethod = samplingMethod;
        this.checkPurpose = checkPurpose;
        this.reportCount = reportCount;
        this.reportType = reportType;
        this.requestDate = requestDate;
        this.paymentMethod = paymentMethod;
        this.paymentRecord = paymentRecord;
        this.paymentCount = paymentCount;
        this.presentInformation = presentInformation;
        this.acceptanceDate = acceptanceDate;
        this.team = team;
        this.state = state;
        this.remark = remark;
        this.invalidReason = invalidReason;
        this.operateUser = operateUser;
        this.operateDate = operateDate;
        this.fileUrl = fileUrl;
        this.sealType = sealType;
        this.isSave = isSave;
        this.entrustPeopleId = entrustPeopleId;
        this.witnessPersonId = witnessPersonId;
    }

    public EntrustEntity() {
        super();
    }

    public EntrustEntity(EntrustAddVo vo){
        this.id=vo.getId();
        this.entrustmentNo=vo.getEntrustmentNo();
        this.entrustType=vo.getEntrustType();
        this.samplingMethod=vo.getSamplingMethod();
        this.reportType=vo.getReportType();
        this.entrustCompany=vo.getEntrustCompany();
        this.entrustCompanyId=vo.getEntrustCompanyId();
        this.entrustPeople=vo.getEntrustPeople();
        this.entrustPhone=vo.getEntrustPhone();
        this.witnessUint=vo.getWitnessUint();
        this.witnessPerson=vo.getWitnessPerson();
        this.witnessPhone=vo.getWitnessPhone();
        this.projectName=vo.getProjectName();
        this.projectPart=vo.getProjectPart();
        this.requestDate=vo.getRequestDate();
        this.isSave=vo.getIsSave();
        this.checkPurpose=vo.getCheckPurpose();
        this.reportCount=vo.getReportCount();
        this.paymentMethod=vo.getPaymentMethod();
        this.paymentRecord=vo.getPaymentRecord();
        this.sealType=vo.getSealType();
        this.businessAcceptor=vo.getBusinessAcceptor();
        this.acceptanceDate=vo.getAcceptanceDate();
        this.team=vo.getTeam();
        this.fileUrl=vo.getFileUrl();
        this.remark=vo.getRemark();
        this.address = vo.getAddress();
        this.addressee = vo.getAddressee();
        this.mobile=vo.getMobile();
        this.mailbox=vo.getMailbox();
        this.entrustTestType=vo.getEntrustTestType();
        this.designStrength=vo.getDesignStrength();
        this.intensityOfConfiguration=vo.getIntensityOfConfiguration();
        this.antifreezeLevel=vo.getAntifreezeLevel();
        this.waterBinderRatio=vo.getWaterBinderRatio();
        this.unitWaterUse =vo.getUnitWaterUse();
        this.sandRatio=vo.getSandRatio();
        this.designSlump=vo.getDesignSlump();
        this.mixingWay=vo.getMixingWay();
        this.taskSource=vo.getTaskSource();
        this.discount=vo.getDiscount();
        this.actualPrice= vo.getActualPrice() == null ? null : df.format(Double.parseDouble(vo.getActualPrice()));
        this.systemPrice=vo.getSystemPrice();
        this.presentInformation=vo.getPresentInformation();
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getEntrustmentNo() {
        return entrustmentNo;
    }

    public void setEntrustmentNo(Integer entrustmentNo) {
        this.entrustmentNo = entrustmentNo;
    }

    public String getEntrustType() {
        return entrustType;
    }

    public void setEntrustType(String entrustType) {
        this.entrustType = entrustType;
    }

    public String getEntrustPeople() {
        return entrustPeople;
    }

    public void setEntrustPeople(String entrustPeople) {
        this.entrustPeople = entrustPeople == null ? null : entrustPeople.trim();
    }

    public String getEntrustPhone() {
        return entrustPhone;
    }

    public void setEntrustPhone(String entrustPhone) {
        this.entrustPhone = entrustPhone == null ? null : entrustPhone.trim();
    }

    public String getWitnessUint() {
        return witnessUint;
    }

    public void setWitnessUint(String witnessUint) {
        this.witnessUint = witnessUint == null ? null : witnessUint.trim();
    }

    public String getWitnessPerson() {
        return witnessPerson;
    }

    public void setWitnessPerson(String witnessPerson) {
        this.witnessPerson = witnessPerson == null ? null : witnessPerson.trim();
    }

    public String getWitnessPhone() {
        return witnessPhone;
    }

    public void setWitnessPhone(String witnessPhone) {
        this.witnessPhone = witnessPhone == null ? null : witnessPhone.trim();
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName == null ? null : projectName.trim();
    }

    public String getProjectPart() {
        return projectPart;
    }

    public void setProjectPart(String projectPart) {
        this.projectPart = projectPart == null ? null : projectPart.trim();
    }

    public String getSamplingMethod() {
        return samplingMethod;
    }

    public void setSamplingMethod(String samplingMethod) {
        this.samplingMethod = samplingMethod;
    }

    public String getCheckPurpose() {
        return checkPurpose;
    }

    public void setCheckPurpose(String checkPurpose) {
        this.checkPurpose = checkPurpose;
    }

    public Integer getReportCount() {
        return reportCount;
    }

    public void setReportCount(Integer reportCount) {
        this.reportCount = reportCount;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentRecord() {
        return paymentRecord;
    }

    public void setPaymentRecord(String paymentRecord) {
        this.paymentRecord = paymentRecord == null ? null : paymentRecord.trim();
    }

    public String getPaymentCount() {
        return paymentCount;
    }

    public void setPaymentCount(String paymentCount) {
        this.paymentCount = paymentCount == null ? null : paymentCount.trim();
    }

    public String getPresentInformation() {
        return presentInformation;
    }

    public void setPresentInformation(String presentInformation) {
        this.presentInformation = presentInformation == null ? null : presentInformation.trim();
    }

    public Date getAcceptanceDate() {
        return acceptanceDate;
    }

    public void setAcceptanceDate(Date acceptanceDate) {
        this.acceptanceDate = acceptanceDate;
    }

    public Integer getTeam() {
        return team;
    }

    public void setTeam(Integer team) {
        this.team = team;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {
        this.invalidReason = invalidReason == null ? null : invalidReason.trim();
    }

    public Long getOperateUser() {
        return operateUser;
    }

    public void setOperateUser(Long operateUser) {
        this.operateUser = operateUser;
    }

    public Date getOperateDate() {
        return operateDate;
    }

    public void setOperateDate(Date operateDate) {
        this.operateDate = operateDate;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl == null ? null : fileUrl.trim();
    }

    public String getSealTypeId() {
        return sealType;
    }

    public void setSealTypeId(String sealType) {
        this.sealType = sealType;
    }

    public String getIsSave() {
        return isSave;
    }

    public void setIsSave(String isSave) {
        this.isSave = isSave == null ? null : isSave.trim();
    }

    public Integer getEntrustPeopleId() {
        return entrustPeopleId;
    }

    public void setEntrustPeopleId(Integer entrustPeopleId) {
        this.entrustPeopleId = entrustPeopleId;
    }

    public Integer getWitnessPersonId() {
        return witnessPersonId;
    }

    public void setWitnessPersonId(Integer witnessPersonId) {
        this.witnessPersonId = witnessPersonId;
    }
}