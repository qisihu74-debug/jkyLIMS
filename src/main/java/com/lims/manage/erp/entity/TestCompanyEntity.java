package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

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

    public TestCompanyEntity() {
    }
}
