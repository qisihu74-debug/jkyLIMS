package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * @Description 资料学习心得
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
public class DataExperienceVo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 学习心得id
     */
    private String id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名称
     */
    private String name;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private java.util.Date createTime;

    /**
     * 心得内容
     */
    private String experienceContent;
}
