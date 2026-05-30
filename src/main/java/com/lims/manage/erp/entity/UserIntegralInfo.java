package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * @Description 用户积分信息
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
@TableName("t_user_integral_info")
public class UserIntegralInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 记录id
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
}
