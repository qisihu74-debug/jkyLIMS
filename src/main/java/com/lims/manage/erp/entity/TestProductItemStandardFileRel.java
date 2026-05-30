package com.lims.manage.erp.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;

/**
 * 检测项的检测依据(TestProductItemStandardFileRel)表实体类
 *
 * @author makejava
 * @since 2022-03-08 17:13:05
 */
@SuppressWarnings("serial")
public class TestProductItemStandardFileRel extends Model<TestProductItemStandardFileRel> {
    //检测项
    private Integer checkItemId;
    //检测依据id
    private Integer standardFileId;


    public Integer getCheckItemId() {
        return checkItemId;
    }

    public void setCheckItemId(Integer checkItemId) {
        this.checkItemId = checkItemId;
    }

    public Integer getStandardFileId() {
        return standardFileId;
    }

    public void setStandardFileId(Integer standardFileId) {
        this.standardFileId = standardFileId;
    }

    public TestProductItemStandardFileRel(Integer checkItemId, Integer standardFileId) {
        this.checkItemId = checkItemId;
        this.standardFileId = standardFileId;
    }

    /**
     * 获取主键值
     *
     * @return 主键值
     */
    @Override
    protected Serializable pkVal() {
        return this.checkItemId;
    }
    }

