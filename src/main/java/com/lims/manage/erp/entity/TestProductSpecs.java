package com.lims.manage.erp.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;

/**
 * 产品规格等级(TestProductSpecs)表实体类
 *
 * @author makejava
 * @since 2022-03-08 17:12:49
 */
@SuppressWarnings("serial")
public class TestProductSpecs extends Model<TestProductSpecs> {
    @TableId(type = IdType.AUTO)
    private Integer id;
    //产品
    private Integer productId;
    //规格等级
    private String specs;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProduceId() {
        return productId;
    }

    public void setProduceId(Integer produceId) {
        this.productId = produceId;
    }

    public String getSpecs() {
        return specs;
    }

    public void setSpecs(String specs) {
        this.specs = specs;
    }

    public TestProductSpecs(Integer produceId, String specs) {
        this.productId = produceId;
        this.specs = specs;
    }

    /**
     * 获取主键值
     *
     * @return 主键值
     */
    @Override
    protected Serializable pkVal() {
        return this.id;
    }
    }

