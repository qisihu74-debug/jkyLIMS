package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;


/**
 * @Author: DLC
 * @Date: 2021/12/15 16:58
 * 检测项 与仪器 中间表
 */
@Data
public class TestChItemInstrumentMiddleEntity {
    /**
     *
     */
    private Integer id;
    /**
     * （表test_entrusted_sample_checkitem_rel）的id
     */
    private Integer sidItem;
    /**
     * 仪器主键id
     */
    private Integer intrusmentId;
    /**
     * 开始使用时间
     */
    private Date startTime;
    /**
     * 结束使用时间
     */
    private Date endTime;
}
