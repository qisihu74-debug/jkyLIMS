package com.lims.manage.erp.entity;

import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-02-05 13:44
 * @Copyright © 河南交科院
 */
@Data
public class ReportTools {
    /**
     * 操作类型 1中间报告改为最终报告，2最终报告改为中间报告
     * 3.报告重新上传进行电子盖章，4线上审批改为线下，5线下审批改为线上
     */
    private String type;

    private String reportCode;

}
