package com.lims.manage.erp.entity;

import lombok.Data;

@Data
public class PreSampleCode {
    private String year;
    private String month;
    private String preSampleCode;

    public PreSampleCode() {
    }

    public PreSampleCode(String year, String month, String preSampleCode) {
        this.year = year;
        this.month = month;
        this.preSampleCode = preSampleCode;
    }
}
