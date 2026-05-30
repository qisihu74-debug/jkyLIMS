package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;


/**
 * @Description 学习资料动态记录
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
@TableName("t_data_record")
public class DataRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 资料id
     */
    @TableId
    private String dataId;

    /**
     * 点赞数量
     */
    private Integer likeNum;

    /**
     * 点踩数量
     */
    private Integer tapNum;

    /**
     * 收藏数量
     */
    private Integer collectNum;

    /**
     * 完成人数
     */
    private Integer completeNum;

    /**
     * 用户资料点赞状态
     */
    @TableField(exist = false)
    private Boolean likeStatus;

    /**
     * 用户资料点踩状态
     */
    @TableField(exist = false)
    private Boolean tapStatus;

    /**
     * 用户资料收藏状态
     */
    @TableField(exist = false)
    private Boolean collectStatus;

    /**
     * 用户资料完成状态
     */
    @TableField(exist = false)
    private Boolean completeStatus;
}
