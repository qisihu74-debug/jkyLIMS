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
public class Actions {
    /**
     * 签署动作类型：CORPORATE（企业签章），PERSONAL（个人签字），LP（法定代表人签字），AUDIT（审批）
     */
    private String type;
    /**
     * 签署动作名称
     */
    private String name;
    /**
     * 签署顺序（从1开始)；如果想按顺序签署，
     * 则分别设置签署动作的serialNo为1,2,3；
     * 如果想无序签署，则设置签署动作的serialNo为1,1,1；
     * 设置签署动作顺序为1,2,2时，表示第一个先签署，后两个同时签署
     */
    private String serialNo;
    /**
     * 指定印章，格式：[123123123213,123213213213]
     */
    private String sealIds;
    /**
     *操作人信息
     */
    private List<ActionOperators> actionOperators;

}