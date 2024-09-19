package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2024-09-13 14:53
 * @Copyright © 河南交科院
 */
@Data
@TableName("hk_person")
public class HkPerson {
    @TableId
    private String personId;

    private String personName;

    private int gender; // 假设1代表男性，2代表女性或其他

    private int certificateType; // 证件类型，具体含义需要查阅文档

    private String phoneNo;

    private int age;

    private int married; // 婚姻状态，0可能代表未婚或其他

    private String roomNum;

    private String dormitory;

    private int lodge; // 住宿状态，0可能代表不住宿

    private int syncFlag; // 同步标志，具体用途未知

    private String orgIndexCode; // 组织索引码

    private String orgName; // 组织名称

    private String orgPath; // 组织路径

    private String orgPathName; // 组织路径名称
}
