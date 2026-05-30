package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * @Description 知识审核资料列表
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
public class DataAuditRecordVo implements Serializable {
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
     * 上传人
     */
    private String upUser;

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
}
