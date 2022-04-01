package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2022/4/1 15:45
 * 检测项 与仪器的关系
 */
@Data
public class InstrumentEntity {

    /**
     * 检测项主键
     */
    private Integer itemId;
    /**
     * 仪器列表主键
     */
    private List<Integer> ids;

}
