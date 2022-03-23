package com.lims.manage.erp.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 检测项的检测方法(TestProductItemMethodRel)表实体类
 *
 * @author makejava
 * @since 2022-03-02 15:15:27
 */
@SuppressWarnings("serial")
public class TestProductItemMethodRel extends Model<TestProductItemMethodRel> {
    @TableId(type = IdType.AUTO)
    private Integer id;
    //检测项id
    private Integer checkItemId;
    //检测方法id
    private Integer methodId;
    //检测项方法检测价格
    private Integer methodItemPrice;
    //检测项应用印章
    private Integer methodItemSignet;


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

    public Integer getMethodId() {
        return methodId;
    }

    public void setMethodId(Integer methodId) {
        this.methodId = methodId;
    }

    public TestProductItemMethodRel() {
    }

    public Integer getMethodItemPrice() {
        return methodItemPrice;
    }

    public void setMethodItemPrice(Integer methodItemPrice) {
        this.methodItemPrice = methodItemPrice;
    }

    public TestProductItemMethodRel(Integer checkItemId, Integer methodId) {
        this.checkItemId = checkItemId;
        this.methodId = methodId;
    }

    public Integer getMethodItemSignet() {
        return methodItemSignet;
    }

    public void setMethodItemSignet(Integer methodItemSignet) {
        this.methodItemSignet = methodItemSignet;
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

