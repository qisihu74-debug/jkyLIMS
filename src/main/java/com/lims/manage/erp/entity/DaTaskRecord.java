package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-11-01 15:04
 * @Copyright © 河南交科院
 */
@Data
@TableName("da_task_record")
public class DaTaskRecord {
    @TableId(type = IdType.AUTO)
    private int id;
    private String taskCode;
    private String url;
    private String time;
}
