package com.lims.manage.erp.service;

import java.util.Map;

public interface FinanceService {

    Map<String, Object> summary();

    Map<String, Object> billingList(Integer pageNum, Integer pageSize, String search);

    void calculateBilling(Map<String, Object> payload);

    void addRemittance(Map<String, Object> payload);

    void addInvoice(Map<String, Object> payload);

    Map<String, Object> statement(Long entrustId);
}
