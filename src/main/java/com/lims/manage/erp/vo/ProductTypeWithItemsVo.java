package com.lims.manage.erp.vo;

import lombok.Data;
import java.util.List;

@Data
public class ProductTypeWithItemsVo {
    private Integer productTypeId;
    private String productTypeName;
    private List<ProductItemNodeVo> items;
}
