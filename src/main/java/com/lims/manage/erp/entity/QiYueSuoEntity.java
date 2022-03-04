package com.lims.manage.erp.entity;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2022/2/23 10:17
 * @Copyright © 河南交科院
 */
@Data
@Component
public class QiYueSuoEntity {
    /**
     * 契约锁应用key
     */
    @Value("${qiyuesuo.AppToken}")
    private String appToken;
    /**
     * 密钥
     */
    @Value("${qiyuesuo.AppSecret}")
    private String appSecret;
    /**
     * 契约锁服务地址
     */
    @Value("${qiyuesuo.Url}")
    private String url;
    /**
     * 根据文件类型创建合同文档接口
     */
    @Value("${qiyuesuo.Create}")
    private String createInterface;
    /**
     * 创建合同接口
     */
    @Value("${qiyuesuo.Add}")
    private String addInterface;
    /**
     * 发起合同接口
     */
    @Value("${qiyuesuo.Send}")
    private String sendInterface;
    /**
     * 合同签署页面接口
     */
    @Value("${qiyuesuo.Sign}")
    private String signInterface;
    /**
     * 删除合同接口
     */
    @Value("${qiyuesuo.Delete}")
    private String deleteInterface;
    /**
     * 印章列表接口
     */
    @Value("${qiyuesuo.List}")
    private String listInterface;
    /**
     * 文件下载接口
     */
    @Value("${qiyuesuo.Download}")
    private String downloadInterface;
    /**
     * 部门列表
     */
    @Value("${qiyuesuo.dept}")
    private String deptInterface;

    @Value("${qiyuesuo.categoryId}")
    private String categoryId;

}
