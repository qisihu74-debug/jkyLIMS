package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2022-08-18 14:51
 * @Copyright © 河南交科院
 */
@Data
@TableName("sys_code")
public class CodeEntity {
    @TableId
    private String id;
    /**
     * 客户代表id
     */
    private String userId;
    /**
     * 客户代表姓名
     */
    private String name;
    /**
     * 客户代表联系方式
     */
    private String mobile;
    /**
     * 0待使用，1已使用
     */
    private String state;
    /**
     * 验证码
     */
    private String code;
    /**
     * 生成个数
     */
    private Integer number;
    /**
     * 客户注册时绑定的客户单位名称
     */
    private String usedCompany;
    /**
     * 验证码创建时间
     */
    private String createTime;
    /**
     * 验证码使用时间
     */
    private String useTime;
}
