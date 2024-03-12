package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: DLC
 * @Date: 2021/11/29 16:47
 * 单位下 人员信息
 */
@Data
@TableName("test_customer")
public class TestCustomerEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    @TableId
    private Integer customerId;
    /**
     * 公司id
     */
    private Integer companyId;
    /**
     * 联系人
     */
    private String contacts;
    /**
     * 联系方式
     */
    private String phone;
    @TableField(exist = false)
    private Integer pageNum;
    @TableField(exist = false)
    private Integer pageSize;

    public TestCustomerEntity() {
    }

    public TestCustomerEntity(Integer companyId, String contacts) {
        this.companyId = companyId;
        this.contacts = contacts;
    }
}
