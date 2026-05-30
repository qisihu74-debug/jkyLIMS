package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.List;


/**
 * @Description 学习资料信息
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
@TableName("t_data_info")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 资料id
     */
    @TableId(type = IdType.ID_WORKER_STR)
    private String dataId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 资料名称
     */
    private String dataTitle;

    /**
     * 资料标签
     */
    private String dataLabel;

    /**
     * 资料类型
     */
    private String dataType;

    /**
     * 视频资料封面图
     */
    private String dataVideoUrl;

    /**
     * 资料地址
     */
    private String dataUrl;

    /**
     * 资料图片地址
     */
    private String dataImgUrl;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件后缀
     */
    private String fileSuffix;

    /**
     * 上传时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date upTime;

    /**
     * 开始上传时间-查询条件
     */
    @TableField(exist = false)
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private java.util.Date upTimeBegin;

    /**
     * 结束上传时间-查询条件
     */
    @TableField(exist = false)
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private java.util.Date upTimeEnd;

    /**
     * 是否原创
     */
    private Integer original;

    /**
     * 资料来源
     */
    private String dataSource;

    /**
     * 参与人员
     */
    private String partakeUser;

    /**
     * 上传人
     */
    @TableField(exist = false)
    private String upUser;

    /**
     * 审核状态
     */
    @TableField(exist = false)
    private Integer auditStatus;

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
     * 标签列表
     */
    @TableField(exist = false)
    private List<LabelInfo> labelIdList;
}
