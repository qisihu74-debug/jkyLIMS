package com.lims.manage.erp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.entity
 * @desc
 * @date 2023-09-28 10:34
 * @Copyright © 河南交科院
 */
@Data
@TableName("test_technicist_capacity")
public class TechnicistCapacity {
    private Integer technicistId;
    private Integer productTypeId;
    private String productTypeName;
    private Integer productId;
    private String productName;

}
