package com.lims.manage.erp.vo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 产品信息(TestProduct)表实体类
 *
 * @author makejava
 * @since 2022-03-02 15:00:10
 */
@SuppressWarnings("serial")
@Data
public class TestProductVo extends Model<TestProductVo> {
    //产品id
    @TableId(type = IdType.AUTO)
    private Integer productId;
    //产品编号
    private String productCode;
    //产品名称
    private String productName;
    //产品类型id
    private Integer productTypeId;
    //是否可用
    private Integer isValid;
    //样品描述
    private String remark;
    // 0,启用，1,冻结
    private String status;
    //0默认未删除,1删除
    private Integer delFlag;
    //注册时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    //产品类型名称
    private String productTypeName;
    //产品用途
    private String productPurpose;
    //外观描述
    private String outwardDescribe;
    //序号
    private Integer serialNumber;
    /**
     * 获取主键值
     *
     * @return 主键值
     */
    @Override
    protected Serializable pkVal() {
        return this.productId;
    }
    }

