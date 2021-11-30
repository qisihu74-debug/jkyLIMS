package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.SampleEntity;

import java.util.Date;
import java.util.List;

public class EntrustAddVo {
    /**
     * 编号
     */
    private String entrustmentNo;
    /**
     * 委托方式
     */
    private Integer entrustType;
    /**
     * 委托单位ID
     */
    private Integer companyId;
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
    private Integer samplingMethod;
    /**
     * 检测目的
     */
    private Integer checkPurpose;
    /**
     * 报告份数
     */
    private Integer reportCount;
    /**
     * 取报告方式
     */
    private Integer reportType;
    /**
     * 要求完成时间
     */
    private Date requestDate;
    /**
     * 支付方式
     */
    private Integer paymentMethod;
    /**
     * 缴费记录(实际已收费）
     */
    private String paymentRecord;
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
    private Integer operateUser;
    /**
     * 作废操作日期
     */
    private Date operateDate;
    /**
     * 附件url
     */
    private String fileUrl;
    /**
     * 盖章类型25甲级，26，27
     */
    private Integer sealTypeId;
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
     * 样品信息
     */
    private List<SampleEntity> samples;


    public String getEntrustmentNo() {
        return entrustmentNo;
    }

    public void setEntrustmentNo(String entrustmentNo) {
        this.entrustmentNo = entrustmentNo;
    }

    public Integer getEntrustType() {
        return entrustType;
    }

    public void setEntrustType(Integer entrustType) {
        this.entrustType = entrustType;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public String getEntrustPeople() {
        return entrustPeople;
    }

    public void setEntrustPeople(String entrustPeople) {
        this.entrustPeople = entrustPeople;
    }

    public String getEntrustPhone() {
        return entrustPhone;
    }

    public void setEntrustPhone(String entrustPhone) {
        this.entrustPhone = entrustPhone;
    }

    public String getWitnessUint() {
        return witnessUint;
    }

    public void setWitnessUint(String witnessUint) {
        this.witnessUint = witnessUint;
    }

    public String getWitnessPerson() {
        return witnessPerson;
    }

    public void setWitnessPerson(String witnessPerson) {
        this.witnessPerson = witnessPerson;
    }

    public String getWitnessPhone() {
        return witnessPhone;
    }

    public void setWitnessPhone(String witnessPhone) {
        this.witnessPhone = witnessPhone;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectPart() {
        return projectPart;
    }

    public void setProjectPart(String projectPart) {
        this.projectPart = projectPart;
    }

    public Integer getSamplingMethod() {
        return samplingMethod;
    }

    public void setSamplingMethod(Integer samplingMethod) {
        this.samplingMethod = samplingMethod;
    }

    public Integer getCheckPurpose() {
        return checkPurpose;
    }

    public void setCheckPurpose(Integer checkPurpose) {
        this.checkPurpose = checkPurpose;
    }

    public Integer getReportCount() {
        return reportCount;
    }

    public void setReportCount(Integer reportCount) {
        this.reportCount = reportCount;
    }

    public Integer getReportType() {
        return reportType;
    }

    public void setReportType(Integer reportType) {
        this.reportType = reportType;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public Integer getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(Integer paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentRecord() {
        return paymentRecord;
    }

    public void setPaymentRecord(String paymentRecord) {
        this.paymentRecord = paymentRecord;
    }

    public String getPaymentCount() {
        return paymentCount;
    }

    public void setPaymentCount(String paymentCount) {
        this.paymentCount = paymentCount;
    }

    public String getPresentInformation() {
        return presentInformation;
    }

    public void setPresentInformation(String presentInformation) {
        this.presentInformation = presentInformation;
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
        this.remark = remark;
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {
        this.invalidReason = invalidReason;
    }

    public Integer getOperateUser() {
        return operateUser;
    }

    public void setOperateUser(Integer operateUser) {
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
        this.fileUrl = fileUrl;
    }

    public Integer getSealTypeId() {
        return sealTypeId;
    }

    public void setSealTypeId(Integer sealTypeId) {
        this.sealTypeId = sealTypeId;
    }

    public String getIsSave() {
        return isSave;
    }

    public void setIsSave(String isSave) {
        this.isSave = isSave;
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
