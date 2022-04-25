package com.lims.manage.erp.entity;

import lombok.Data;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2022/4/14 9:56
 * @Copyright © 河南交科院
 */
@Data
public class ReqBean {
    private List<ConclusionEntity> list;
    private Long id;
    private String type;
    private TestSampleMixInfoEntity mixInfo;

}
