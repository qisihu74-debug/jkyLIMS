package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class TaskProgressStateVo {
    /**
     * 任务状态名称
     */
    private String title;
    /**
     * 状态时间
     */
    private String time;

    public TaskProgressStateVo(String title, String time) {
        this.title = title;
        this.time = time;
    }

    public TaskProgressStateVo() {
    }
}
