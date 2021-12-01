package com.lims.manage.erp.entity;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2021/12/1 10:05
 * 历史委托
 */
@Data
public class EntrustHistoryEntity {
    /**
     * 委托编号
     */
    private String entrustmentNo;
    /**
     * 样品
     */
    private String sampleNname;
    /**
     * 委托人
     */
    private String entrustPeople;
    /**
     * 工程名称
     */
    private String projectName;

    public EntrustHistoryEntity(String entrustmentNo, String sampleNname, String entrustPeople, String projectName) {
        this.entrustmentNo = entrustmentNo;
        this.sampleNname = sampleNname;
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
        return sampleNname;
    }

    public void setSampleNname(String sampleNname) {
        this.sampleNname = sampleNname;
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
