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
public class Signatories {
    /**
     * 签署顺序（从1开始)；如果想按顺序签署，
     * 则分别设置签署方的serialNo为1,2,3；如果想无序签署，
     * 则设置签署方的serialNo为1,1,1；设置签署方顺序为1,2,2时，
     * 表示第一个先签署，后两个同时签署。
     */
    private String serialNo;
    /**
     * 发起方名称
     */
    private String tenantName;
    /**
     * 签约主体类型：COMPANY（外部企业），PERSONAL（个人）
     */
    private String tenantType;
    /**
     * 签署动作,用印流程非预设且签署方为发起方时，使用用户传入的签署动作，其余情况使用用印流程的配置
     */
    private List<Actions> actions;

}