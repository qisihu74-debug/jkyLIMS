package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class TaskPriceVo {
    private Long taskId;
    private Double price;

    public TaskPriceVo(Long taskId, Double price) {
        this.taskId = taskId;
        this.price = price;
    }
}
