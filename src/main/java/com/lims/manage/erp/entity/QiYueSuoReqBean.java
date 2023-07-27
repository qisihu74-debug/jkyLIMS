/**
  * Copyright 2022 bejson.com
  */
package com.lims.manage.erp.entity;
import lombok.Data;

import java.util.List;

/**
 * Auto-generated: 2022-03-07 15:11:23
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class QiYueSuoReqBean {
    private String reportType;
    /**
     * 合同创建人手机号码
     */
    private String creatorContact;
    /**
     * 合同创建人姓名
     */
    private String creatorName;
    /**
     * 委托单id
     */
    private long entrustId;
    /**
     *是否发起合同，默认true。（true：立即发起；false：保存为草稿）
     */
    private boolean send;
    /**
     * 合同名称
     */
    private String subject;
    /**
     * 指定位置外签署，默认为false
     */
    private Boolean ordinal;
    /**
     * 允许指定位置签署，默认为true，指定位置外签署和指定位置签署两者不可同时为false
     */
    private Boolean extraSign;
    private Boolean mustSign;
    /**
     * 是否自动生成会签节点（用于签署动作中传入多个印章并且未指定签署人）默认false
     */
    private Boolean autoCreateCounterSign;
    /**
     * 发起方名称
     */
    private String tenantName;
    /**
     *(契约锁提供)用印流程ID-> 2934717410113839636
     */
    private String categoryId;
    /**
     * 签署方，为空时在合同签署完成后需要调用接口“封存合同”主动结束合同
     */
    /**
     * 合同文档ID的集合（一个文档只能属于一个合同）接口qiYueSuoHnadler.creatFile()返回
     */
    private List<String> documents;

    private List<Signatories> signatories;

    private List<Long> list;
}
