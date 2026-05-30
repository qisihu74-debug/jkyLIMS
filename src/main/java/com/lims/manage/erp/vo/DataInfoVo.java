package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * @Description 我分享的学习资料、视频列表
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
public class DataInfoVo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 资料id
     */
    private String dataId;

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
     * 上传时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date upTime;

    /**
     * 资料审核状态
     */
    private Integer auditStatus;

    /**
     * 资料审核内容
     */
    private String auditContent;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 上传人
     */
    private String upUser;

    /**
     * 资料地址
     */
    private String dataUrl;

    /**
     * 资料图片地址
     */
    private String dataImgUrl;

    /**
     * 资料后缀
     */
    private String fileSuffix;
}
