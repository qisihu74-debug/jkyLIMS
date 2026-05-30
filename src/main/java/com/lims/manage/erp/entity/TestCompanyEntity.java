package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/11/29 10:55
 * 委托单位信息
 */
@Data
@TableName("test_company")
public class TestCompanyEntity implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "company_id", type = IdType.AUTO)
    private Integer companyId;
    /**
     * 委托单位名称
     */
    private String companyName;
    /**
     * 类型
     */
    private String type;

    /**
     * 地址
     */
    private String address;

    /**
     * 创建时间
     */
    private Date addTime;

    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 联系人信息
     */
    @TableField(exist = false)
    List<TestCustomerEntity> testCustomerEntityList;
    @TableField(exist = false)
    private Integer pageNum;
    @TableField(exist = false)
    private Integer pageSize;
    @TableField(exist = false)
    private String order;
    /**
     * 联系人
     */
    @TableField(exist = false)
    private String contacts;
    /**
     * 联系方式
     */
    @TableField(exist = false)
    private String phone;

    public TestCompanyEntity() {
    }
}
