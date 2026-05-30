package com.lims.manage.erp.vo;

import lombok.Data;

import java.util.List;

@Data
public class CheckItemDetailVo {
    private Integer productId;
    private Integer itemPid;
    private Integer checkItemId;
    private String checkItemName;
    private List<LabelValueVo> checkBasisList;
}
