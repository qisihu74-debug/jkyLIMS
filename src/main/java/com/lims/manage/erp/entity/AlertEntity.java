package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2022/5/31 15:47
 * @Copyright © 河南交科院
 */
@Data
@TableName("test_alert")
public class AlertEntity {
    private Long id;
    private Long entrustId;
    private String checkItemName;
    private String describ;
}
