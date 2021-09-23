package com.stu.manage.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.entity
 * @desc
 * @date 2021/9/23 16:58
 * @Copyright © 河南交科院
 */
@Data
@TableName("jt_entrust_info")
public class EntrustStat {
    @TableId(value="entrust_id",type = IdType.ID_WORKER_STR)
    private int id;
    /**
     * 委托单状态0 未分任务 1已分任务
     */
    @TableField(value = "entrust_status")
    private String status;
}
