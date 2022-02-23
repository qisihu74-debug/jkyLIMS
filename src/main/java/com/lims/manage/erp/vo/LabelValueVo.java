package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class LabelValueVo {
    private String label;
    private Long value;

    public LabelValueVo() {
    }

    public LabelValueVo(String label, Long value) {
        this.label = label;
        this.value = value;
    }
}
