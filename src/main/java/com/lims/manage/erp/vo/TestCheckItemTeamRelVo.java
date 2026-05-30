package com.lims.manage.erp.vo;

import lombok.Data;

@Data
public class TestCheckItemTeamRelVo {
    //主键
    private Integer id;
    //产品product_id下检验项id
    private Integer checkItemId;
    //科室id
    private Integer teamId;
    //产品ID
    private Integer productId;
    //产品名称
    private String productName;
    //科室名称
    private String teamName;
    //检验项名称
    private String checkItemName;
    /**
     * 优先级
     */
    private String priority;
}
