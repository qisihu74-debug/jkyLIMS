package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * @Description 知识广场-学习资料信息
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
public class KsDataInfoVo implements Serializable {
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
     * 视频封面图
     */
    private String dataVideoUrl;

    /**
     * 资料后缀
     */
    private String fileSuffix;

    /**
     * 上传人
     */
    private String upUser;

    /**
     * 上传人id
     */
    private String userId;

    /**
     * 上传时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private java.util.Date upTime;
}
