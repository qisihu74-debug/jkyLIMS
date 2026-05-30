package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * @Description 用户动态操作记录
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
@TableName("t_user_operation_record")
public class UserOperationRecord implements Serializable {
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
     * 资料或问答类型
     */
    private String eventType;

    /**
     * 资料id、问答id、评论id
     */
    private String eventId;

    /**
     * 用户操作类型（浏览、赞、踩、收藏）
     */
    private String operationType;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;
}
