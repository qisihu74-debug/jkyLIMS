package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class TaskProgressVo {
    /**
     * 任务编号
     */
    private String taskCode;
    /**
     * 任务状态
     * 0：任务发布
     * 1：任务领取
     * 3：试验开始
     * 4：实验完成
     * 6：复核完成
     */
    private Integer state;
}
