package com.lims.manage.erp.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;

/**
 * 检测项使用的设备(TestProductItemInstrumentTypeRel)表实体类
 *
 * @author makejava
 * @since 2022-03-08 17:13:36
 */
@SuppressWarnings("serial")
public class TestProductItemInstrumentTypeRel extends Model<TestProductItemInstrumentTypeRel> {
    @TableId(type = IdType.AUTO)
    private Integer id;
    //检测项id
    private Integer checkItemId;
    //仪器类型id
    private Integer intrusmentTypeId;

    public TestProductItemInstrumentTypeRel(Integer checkItemId, Integer intrusmentTypeId) {
        this.checkItemId = checkItemId;
        this.intrusmentTypeId = intrusmentTypeId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCheckItemId() {
        return checkItemId;
    }

    public void setCheckItemId(Integer checkItemId) {
        this.checkItemId = checkItemId;
    }

    public Integer getIntrusmentTypeId() {
        return intrusmentTypeId;
    }

    public void setIntrusmentTypeId(Integer intrusmentTypeId) {
        this.intrusmentTypeId = intrusmentTypeId;
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

