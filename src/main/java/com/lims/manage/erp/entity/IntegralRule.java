package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;


/**
 * @Description 积分规则实体
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
@TableName("t_integral_rule")
public class IntegralRule implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 规则id
     */
    @TableId(type = IdType.ID_WORKER_STR)
    private String ruleId;

    /**
     * 积分类型
     */
    private String integralType;

    /**
     * 完成类型(点赞、上传、学习、签到等)
     */
    private String completeType;

    /**
     * 每日完成频次
     */
    private Integer frequency;

    /**
     * 积分数量
     */
    private Integer integralNum;
}
