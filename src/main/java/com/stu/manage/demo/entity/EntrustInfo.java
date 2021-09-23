package com.stu.manage.demo.entity;

/**
 * @Author: DLC
 * @Date: 2021/9/22 14:06
 * 委托基本信息录入
 */
public class EntrustInfo {

    /**
     * 委托id
     */
    private Integer entrustId;

    /**
     * 委托编号
     */
    private String entrustNumber;

    /**
     * 委托类型
     */
    private Integer entrustType;

    /**
     * 委托公司名称
     */
    private Integer entrustCompanyId;

    /**
     * 委托人
     */
    private String entrustPeople;

    /**
     * 委托人手机号
     */
    private String entrustPeoplePhone;

    /**
     * 见证单位
     */
    private String witnessCompanyName;

    /**
     * 见证人
     */
    private String witnessPeople;

    /**
     * 工程名称
     */
    private String projectId;

    /**
     * 工程部位
     */
    private String projectPart;

    /**
     * 取样方式
     */
    private String sampleReceiveWay;

    /**
     * 检验目的
     */
    private String checkPurpose;

    /**
     * 邮寄方式
     */
    private String reportGetWay;

    /**
     * 报告份数
     */
    private Integer reportNum;

    /**
     * 提供资料
     */
    private String entrustData;

    /**
     * 备注信息
     */
    private String entrustNote;

    public Integer getEntrustId() {
        return entrustId;
    }

    public void setEntrustId(Integer entrustId) {
        this.entrustId = entrustId;
    }

    public String getEntrustNumber() {
        return entrustNumber;
    }

    public void setEntrustNumber(String entrustNumber) {
        this.entrustNumber = entrustNumber;
    }

    public Integer getEntrustType() {
        return entrustType;
    }

    public void setEntrustType(Integer entrustType) {
        this.entrustType = entrustType;
    }

    public Integer getEntrustCompanyId() {
        return entrustCompanyId;
    }

    public void setEntrustCompanyId(Integer entrustCompanyId) {
        this.entrustCompanyId = entrustCompanyId;
    }

    public String getEntrustPeople() {
        return entrustPeople;
    }

    public void setEntrustPeople(String entrustPeople) {
        this.entrustPeople = entrustPeople;
    }

    public String getEntrustPeoplePhone() {
        return entrustPeoplePhone;
    }

    public void setEntrustPeoplePhone(String entrustPeoplePhone) {
        this.entrustPeoplePhone = entrustPeoplePhone;
    }

    public String getWitnessCompanyName() {
        return witnessCompanyName;
    }

    public void setWitnessCompanyName(String witnessCompanyName) {
        this.witnessCompanyName = witnessCompanyName;
    }

    public String getWitnessPeople() {
        return witnessPeople;
    }

    public void setWitnessPeople(String witnessPeople) {
        this.witnessPeople = witnessPeople;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectPart() {
        return projectPart;
    }

    public void setProjectPart(String projectPart) {
        this.projectPart = projectPart;
    }

    public String getSampleReceiveWay() {
        return sampleReceiveWay;
    }

    public void setSampleReceiveWay(String sampleReceiveWay) {
        this.sampleReceiveWay = sampleReceiveWay;
    }

    public String getCheckPurpose() {
        return checkPurpose;
    }

    public void setCheckPurpose(String checkPurpose) {
        this.checkPurpose = checkPurpose;
    }

    public String getReportGetWay() {
        return reportGetWay;
    }

    public void setReportGetWay(String reportGetWay) {
        this.reportGetWay = reportGetWay;
    }

    public Integer getReportNum() {
        return reportNum;
    }

    public void setReportNum(Integer reportNum) {
        this.reportNum = reportNum;
    }

    public String getEntrustData() {
        return entrustData;
    }

    public void setEntrustData(String entrustData) {
        this.entrustData = entrustData;
    }

    public String getEntrustNote() {
        return entrustNote;
    }

    public void setEntrustNote(String entrustNote) {
        this.entrustNote = entrustNote;
    }
}
