package com.lims.manage.erp.entity;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2021/12/1 10:05
 * 历史委托
 */
@Data
public class EntrustHistoryEntity {
    private Long id;
    /**
     * 委托编号
     */
    private String entrustmentNo;
    /**
     * 样品
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

    public EntrustHistoryEntity(String entrustmentNo, String sampleName, String entrustPeople, String projectName) {
        this.entrustmentNo = entrustmentNo;
        this.sampleName = sampleName;
        this.entrustPeople = entrustPeople;
        this.projectName = projectName;
    }

    public String getEntrustmentNo() {
        return entrustmentNo;
    }

    public void setEntrustmentNo(String entrustmentNo) {
        this.entrustmentNo = entrustmentNo;
    }

    public String getSampleNname() {
        return sampleName;
    }

    public void setSampleNname(String sampleNname) {
        this.sampleName = sampleNname;
    }

    public String getEntrustPeople() {
        return entrustPeople;
    }

    public void setEntrustPeople(String entrustPeople) {
        this.entrustPeople = entrustPeople;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
