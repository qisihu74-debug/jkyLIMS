package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.jky.common.system.vo
 * @desc
 * @date 2023-06-28 17:26
 * @Copyright © 河南交科院
 */
@Data
@TableName("sys_serial_number_record")
public class SnRecord {
    private Long id;
    private String type;
    private String sn;
    private int status;
}
