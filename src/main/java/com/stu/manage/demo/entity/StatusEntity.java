package com.stu.manage.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.entity
 * @desc
 * @date 2021/9/23 15:48
 * @Copyright © 河南交科院
 */
@Data
public class StatusEntity {
    private int id;
    /**
     * 委托单状态0 未分任务 1已分任务
     */
    private String status;
    /**
     * 样品状态
     */
    private List<SampleStatus> list;
}
