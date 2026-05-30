package com.lims.manage.erp.vo;

import lombok.Data;
import java.util.List;

@Data
public class AuthorizationSaveReq {
    private Integer technicistId;
    private List<Integer> excludedItemIds;
}
