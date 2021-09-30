package com.stu.manage.demo.entity;

import java.util.List;

public class EntrustBaseInfo {
    private String checkPurpose;
    private String comName;
    private String entrustNote;
    private String entrustPeople;
    private String entrustPeoplePhone;
    private String entrustType;
    private String projectId;
    private String projectPart;
    private String reportGetWay;
    private int reportNum;
    private String sampleReceiveWay;
    private String witnessCompanyName;
    private String witnessPeople;
    private List<SampleInfoVo> sampleList;

    public EntrustBaseInfo() {
    }

    public EntrustBaseInfo(EntrustBaseInfo info, List<SampleInfoVo> sampleList) {
        this.checkPurpose = info.getCheckPurpose();
        this.comName = info.getComName();
        this.entrustNote = info.getEntrustNote();
        this.entrustPeople = info.getEntrustPeople();
        this.entrustPeoplePhone = info.getEntrustPeoplePhone();
        this.entrustType = info.getEntrustType();
        this.projectId = info.getProjectId();
        this.projectPart = info.getProjectPart();
        this.reportGetWay = info.getReportGetWay();
        this.reportNum = info.getReportNum();
        this.sampleReceiveWay = info.getSampleReceiveWay();
        this.witnessCompanyName = info.getWitnessCompanyName();
        this.witnessPeople = info.getWitnessPeople();
        this.sampleList = sampleList;
    }

    public String getCheckPurpose() {
        return checkPurpose;
    }

    public void setCheckPurpose(String checkPurpose) {
        this.checkPurpose = checkPurpose;
    }

    public String getComName() {
        return comName;
    }

    public void setComName(String comName) {
        this.comName = comName;
    }

    public String getEntrustNote() {
        return entrustNote;
    }

    public void setEntrustNote(String entrustNote) {
        this.entrustNote = entrustNote;
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

    public String getEntrustType() {
        return entrustType;
    }

    public void setEntrustType(String entrustType) {
        this.entrustType = entrustType;
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

    public String getReportGetWay() {
        return reportGetWay;
    }

    public void setReportGetWay(String reportGetWay) {
        this.reportGetWay = reportGetWay;
    }

    public int getReportNum() {
        return reportNum;
    }

    public void setReportNum(int reportNum) {
        this.reportNum = reportNum;
    }

    public String getSampleReceiveWay() {
        return sampleReceiveWay;
    }

    public void setSampleReceiveWay(String sampleReceiveWay) {
        this.sampleReceiveWay = sampleReceiveWay;
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

    public List<SampleInfoVo> getSampleList() {
        return sampleList;
    }

    public void setSampleList(List<SampleInfoVo> sampleList) {
        this.sampleList = sampleList;
    }
}
