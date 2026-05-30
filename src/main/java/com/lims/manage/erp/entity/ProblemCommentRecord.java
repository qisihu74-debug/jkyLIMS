package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;


/**
 * @Description 评论动态(点赞、点踩)记录
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
@TableName("t_problem_comment_record")
public class ProblemCommentRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 评论id
     */
    @TableId
    private String commentId;

    /**
     * 点赞数量
     */
    private Integer likeNum;

    /**
     * 点踩数量
     */
    private Integer tapNum;

    /**
     * 用户点赞状态
     */
    @TableField(exist = false)
    private Boolean likeStatus;

    /**
     * 用户点踩状态
     */
    @TableField(exist = false)
    private Boolean tapStatus;
}
