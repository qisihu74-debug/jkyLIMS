package com.lims.manage.erp.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * @Description 优质分享者信息
 * @Author zhq
 * @CreateTime 2023/01/03 10:18
 */
@Data
public class ShareUserInfoVo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 排名
     */
    private Integer rowNum=0;

    /**
     * 积分数量
     */
    private Integer integralNum=0;

    /**
     * 积分称号
     */
    private String integralTitle="默默无闻";

    /**
     * 用户名称
     */
    private String name;

    /**
     * 分享数量
     */
    private Integer shareNum;

    /**
     * 用户注册时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private java.util.Date createTime;
}
