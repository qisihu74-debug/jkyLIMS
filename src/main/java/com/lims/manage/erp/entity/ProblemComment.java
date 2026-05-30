package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * @Description 问答评论
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
@TableName("t_problem_comment")
public class ProblemComment implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 评论id
     */
    @TableId(type = IdType.ID_WORKER_STR)
    private String commentId;

    /**
     * 问题id
     */
    private String problemId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 父级评论id
     */
    private String parentCommentId;

    /**
     * 评论内容
     */
    private String commentContent;
    
    /**
     * 删除标记
     */
    private Integer delFlag;

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

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date updateTime;
}
