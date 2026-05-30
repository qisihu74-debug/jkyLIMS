package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * @Description 问答列表
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
public class ProblemInfoVo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 问题id
     */
    private String problemId;

    /**
     * 问答名称
     */
    private String problemTitle;

    /**
     * 资料标签
     */
    private String dataLabel;

    /**
     * 提问人
     */
    private String userName;

    /**
     * 用户Id
     */
    private String userId;

    /**
     * 提问时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;

    /**
     * 问答类型
     */
    private String problemType;

    /**
     * 问答类型id
     */
    private String problemTypeId;

    /**
     * 查看数量
     */
    private Integer viewNum;

    /**
     * 回复数量
     */
    private Integer replyNum;

    /**
     * 问题内容
     */
    private String problemContent;

    /**
     * 是否置顶
     */
    private Integer isTop;
}
