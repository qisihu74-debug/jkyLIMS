package com.lims.manage.erp.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;

/**
 * 产品的判定依据(TestProductStandardFileRel)表实体类
 *
 * @author makejava
 * @since 2022-03-08 17:12:21
 */
@SuppressWarnings("serial")
public class TestProductStandardFileRel extends Model<TestProductStandardFileRel> {
    //产品id
    private Integer productId;
    //规范id
    private Integer standardFileId;


    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getStandardFileId() {
        return standardFileId;
    }

    public void setStandardFileId(Integer standardFileId) {
        this.standardFileId = standardFileId;
    }

    public TestProductStandardFileRel(Integer productId, Integer standardFileId) {
        this.productId = productId;
        this.standardFileId = standardFileId;
    }

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

