package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * @Description 积分对应称号信息
 * @Author zhq
 * @CreateTime 2023/01/04 10:18
 */
@Data
@TableName("t_integral_info")
public class IntegralInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    @TableId(type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 积分类型
     */
    private String integralType;

    /**
     * 积分数量
     */
    private Integer integralNum;

    /**
     * 积分对应称号
     */
    private String integralTitle;

    /**
     * 积分对应徽章
     */
    private String integralBadge;
}
