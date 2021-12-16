package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * @Author: DLC
 * @Date: 2021/12/15 16:58
 * 检测项 与仪器 中间表
 */
@Data
public class TestChItemInstrumentMiddleEntity {
    private Integer id;
    private Integer sidItem;
    private Integer intrusmentId;
}
