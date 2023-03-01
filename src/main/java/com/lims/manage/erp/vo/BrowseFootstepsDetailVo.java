package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * @Description 浏览足迹详细信息
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
public class BrowseFootstepsDetailVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 浏览足迹id
     */
    private String id;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 浏览时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "HH:mm:ss")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private java.util.Date createTime;

    /**
     * 资料id
     */
    private String dataId;

    /**
     * 资料名称
     */
    private String dataTitle;

    /**
     * 资料类型
     */
    private String dataType;

    /**
     * 资料后缀
     */
    private String fileSuffix;
}
