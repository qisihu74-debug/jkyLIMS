package com.lims.manage.erp.entity;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2021/12/10 10:08
 * 任务委托信息
 */
@Data
public class EntrustHistoryTaskEntity extends EntrustHistoryEntity {

    /**
     * 样品状态
     */
    private String sampleStatus;


}
