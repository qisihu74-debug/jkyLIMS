package com.lims.manage.erp.vo;

import com.aspose.slides.internal.oe.va;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.api.client.util.Value;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.Serializable;
import java.util.List;


/**
 * @Description 问答评论列表
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
public class ProblemCommentVo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 评论id
     */
    private String commentId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户Id
     */
    private String userId;

    /**
     * 评论内容
     */
    private String commentContent;

    /**
     * 用户称号
     */
    private String integralTitle;

    /**
     * 评论时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;

    /**
     * 评论点赞数量
     */
    private Integer likeNum=0;

    /**
     * 评论点踩数量
     */
    private Integer tapNum=0;

    /**
     * 用户当前的点赞状态
     */
    private Integer likeStatus=0;

    /**
     * 用户当前的点踩状态
     */
    private Integer tapStatus=0;

    /**
     * 子集评论数量
     */
    private Integer commentNum=0;
}
