package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class LabelValueVo {
    private Long entrustId;
    private String label;
    private Long value;
    /**
     * 描述性信息
     */
    private String text;

    public LabelValueVo() {
    }

    public LabelValueVo(String label, Long value) {
        this.label = label;
        this.value = value;
    }
}
