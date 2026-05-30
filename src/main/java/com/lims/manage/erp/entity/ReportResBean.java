package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.Map;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2022/5/30 11:32
 * @Copyright © 河南交科院
 */
@Data
public class ReportResBean {
    private String url;
    private Map<String,String> map;

}
