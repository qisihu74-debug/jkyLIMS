package com.stu.manage.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.entity
 * @desc
 * @date 2021/9/18 9:34
 * @Copyright © 河南交科院
 */
@Data
@TableName(value = "jky_ding_lims_customer")
public class CrmEntity {
    /**
     * 主键
     */
    private int id;
    /**
     * 客户userID
     */
    private String userId;
    /**
     * 推荐人id
     */
    private String creatorUserId;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 用户名称
     */
    private String contactName;
    /**
     * 唯一标识
     */
    private String contactUnionId;
    /**
     * 实例id
     */
    private String instanceId;
    /**
     * 修改时间
     */
    private Date modifyTime;
    /**
     * crm数据当前位置
     */
    private String nextToken;

}
