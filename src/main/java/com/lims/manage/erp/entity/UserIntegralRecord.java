package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * @Description 用户获取积分记录
 * @Author zhq
 * @CreateTime 2023/01/04 10:18
 */
@Data
@TableName("t_user_integral_record")
public class UserIntegralRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    @TableId(type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 积分类型
     */
    private String integralType;

    /**
     * 积分数量
     */
    private Integer integralNum;

    /**
     * 规则id
     */
    private String ruleId;

    /**
     * 资料、评论类型
     */
    private String eventType;

    /**
     * 资料、评论id
     */
    private String eventId;

    /**
     * 积分获取时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date gainTime;
}
