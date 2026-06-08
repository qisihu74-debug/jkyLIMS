package com.lims.manage.erp.service;

import java.util.List;
import java.util.Map;

public interface FinanceService {

    Map<String, Object> summary();

    Map<String, Object> billingList(Integer pageNum, Integer pageSize, String search);

    void calculateBilling(Map<String, Object> payload);

    void addRemittance(Map<String, Object> payload);

    List<Map<String, Object>> remittanceList(Long entrustId);

    void updateRemittance(Map<String, Object> payload);

    void deleteRemittance(Map<String, Object> payload);

    void addInvoice(Map<String, Object> payload);

    Map<String, Object> invoiceLedger(Integer pageNum, Integer pageSize, String search, String status);

    void updateInvoiceStatus(Map<String, Object> payload);

    Map<String, Object> profitAnalysis(Integer pageNum, Integer pageSize, String search);

    Map<String, Object> statement(Long entrustId);
}
