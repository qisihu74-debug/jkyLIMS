package com.lims.manage.erp.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;

/**
 * 团队检测项目(TestCheckItemTeamRel)表实体类
 *
 * @author makejava
 * @since 2022-03-18 10:01:57
 */
@SuppressWarnings("serial")
public class TestCheckItemTeamRel extends Model<TestCheckItemTeamRel> {
    //主键
    @TableId(type = IdType.AUTO)
    private Integer id;
    //产品product_id下检验项id
    private Integer checkItemId;
    //团队id
    private Integer teamId;
    //产品ID
    private Integer productId;


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

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
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

