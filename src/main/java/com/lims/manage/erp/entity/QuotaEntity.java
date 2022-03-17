package com.lims.manage.erp.entity;

import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2022/3/16 15:24
 * @Copyright © 河南交科院
 */
@Data
public class QuotaEntity {
    /**
     * 检测项id
     */
    private int checkItemId;
    /**
     * 指标内容
     */
    private String specsContent;
    private String conditionKey;
    private String conditionValue;
}
