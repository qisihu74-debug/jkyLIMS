package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;


/**
 * @Description 问答列表信息
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
@TableName("t_problem_info")
public class ProblemInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 问题id
     */
    @TableId(type = IdType.ID_WORKER_STR)
    private String problemId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 问题名称
     */
    private String problemTitle;

    /**
     * 资料标签
     */
    private String dataLabel;

    /**
     * 问题类型
     */
    private String problemType;

    /**
     * 问题内容
     */
    private String problemContent;

    /**
     * 是否置顶
     */
    private Integer isTop;

    /**
     * 浏览数量
     */
    private Integer viewNum;

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

    /**
     * 开始创建时间-查询条件
     */
    @TableField(exist = false)
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private java.util.Date createTimeBegin;

    /**
     * 结束创建时间-查询条件
     */
    @TableField(exist = false)
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private java.util.Date createTimeEnd;

    /**
     * 标签列表
     */
    @TableField(exist = false)
    private List<LabelInfo> labelIdList;
}
