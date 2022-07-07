package com.lims.manage.erp.entity;

import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2022/4/8 11:10
 * @Copyright © 河南交科院
 */
@Data
public class ConclusionEntity {
    private int sampleId;
    /**
     * 报告模板地址
     */
    private String url;
    /**
     * 报告结论
     */
    private String conclusion;
    /**
     * 报告附加声明
     */
    private String additional;
}
