package com.lims.manage.erp.entity;

import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2021/12/15 15:24
 * 仪器表
 * 来自 test_instrument
 */
@Data
public class TestInstrumentEntity {
    /**
     * 仪器id
     */
    private Integer id;
    /**
     * 仪器类型id
     */
    private Integer typeId;
    /**
     * 仪器名称
     */
    private String name;
    /**
     * 编号
     */
    private String code;
    /**
     * 型号
     */
    private String model;
}
