package com.lims.manage.erp.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * @Description 资料信息详情
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
public class DataInfoDetailVo implements Serializable {
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
     * 上传人用户id
     */
    private String userId;

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

    /**
     * 点赞数量
     */
    private Integer likeNum=0;

    /**
     * 点踩数量
     */
    private Integer tapNum=0;

    /**
     * 收藏数量
     */
    private Integer collectNum=0;

    /**
     * 完成数量
     */
    private Integer completeNum=0;

    /**
     * 用户资料点赞状态
     */
    private Boolean likeStatus;

    /**
     * 用户资料点踩状态
     */
    private Boolean tapStatus;

    /**
     * 用户资料收藏状态
     */
    private Boolean collectStatus;

    /**
     * 用户资料完成状态
     */
    private Boolean completeStatus;
}
