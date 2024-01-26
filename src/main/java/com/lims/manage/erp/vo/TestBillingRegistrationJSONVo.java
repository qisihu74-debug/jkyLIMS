package com.lims.manage.erp.vo;

import com.lims.manage.erp.entity.TestBillingRegistrationEntity;
import lombok.Data;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2024/1/25 17:51
 */
@Data
public class TestBillingRegistrationJSONVo {

    private List<TestBillingRegistrationEntity> list;
}
