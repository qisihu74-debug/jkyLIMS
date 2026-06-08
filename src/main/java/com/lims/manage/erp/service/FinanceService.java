package com.lims.manage.erp.service;

import java.util.Map;

public interface FinanceService {

    Map<String, Object> summary();

    Map<String, Object> billingList(Integer pageNum, Integer pageSize, String search);
}
