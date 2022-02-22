package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @Author: DLC
 * @Date: 2022/2/22 9:52
 * 公告表
 */
@Data
public class HomeAfficheEntity {

    /**
     * 主键
     */
    private Long id;
    /**
     * 大标题
     */
    private String spreadhead;
    /**
     * 副标题
     */
    private String subheading;
    /**
     * 文件链接
     */
    private String fileUrl;
    /**
     * 文本原始名
     */
    private String fileUrlName;
    /**
     * 文本内容
     */
    private String contentDetail;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 发布人姓名
     */
    private String issuerName;
    /**
     * 发布人id
     */
    private Long issuerUserId;
}
