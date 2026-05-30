package com.lims.manage.erp.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

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

    /**
     * 来自 test_product_item_instrument_middle_rel 下 主键
     */
    private Integer itemInstrumentMiddleId;
    /**
     * 来自 test_product_item_instrument_middle_rel 下 id_item
     */
    private Integer idItem;

    private String temperature;
    private String humidity;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
    private String user;
}
