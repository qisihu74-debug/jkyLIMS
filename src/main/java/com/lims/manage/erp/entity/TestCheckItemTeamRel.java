package com.lims.manage.erp.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.io.Serializable;

/**
 * 团队检测项目(TestCheckItemTeamRel)表实体类
 *
 * @author makejava
 * @since 2022-03-18 10:01:57
 */
@Data
@TableName("test_check_item_team_rel")
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

    /**
     * 团队名称
     */
    private String teamName;
    /**
     * 优先级
     */
    private String priority;

    public TestCheckItemTeamRel() {
    }

    public TestCheckItemTeamRel(Integer checkItemId, Integer teamId, Integer productId) {
        this.checkItemId = checkItemId;
        this.teamId = teamId;
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

