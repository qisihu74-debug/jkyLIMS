package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.service.FinanceService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinanceServiceImpl implements FinanceService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    private static final String RECEIVABLE_AMOUNT =
            "COALESCE(CAST(NULLIF(REPLACE(REPLACE(e.actual_price, ',', ''), '￥', ''), '') AS DECIMAL(16,2)), " +
                    "CAST(NULLIF(REPLACE(REPLACE(e.system_price, ',', ''), '￥', ''), '') AS DECIMAL(16,2)), " +
                    "CAST(NULLIF(REPLACE(REPLACE(e.count_price, ',', ''), '￥', ''), '') AS DECIMAL(16,2)), 0)";

    private static final String PAID_SUBQUERY =
            "LEFT JOIN (SELECT entrusted_id, " +
                    "SUM(CAST(NULLIF(REPLACE(REPLACE(amount, ',', ''), '￥', ''), '') AS DECIMAL(16,2))) paid_amount " +
                    "FROM test_entrust_remittance_registration GROUP BY entrusted_id) pay ON pay.entrusted_id = e.id ";

    private static final String INVOICE_SUBQUERY =
            "LEFT JOIN (SELECT entrustment_id, COUNT(*) invoice_count, MAX(registration_time) last_invoice_time " +
                    "FROM test_billing_registration GROUP BY entrustment_id) inv ON inv.entrustment_id = e.id ";

    @Override
    public Map<String, Object> summary() {
        String sql = "SELECT COUNT(*) entrustCount, " +
                "SUM(CASE WHEN x.receivable_amount <= 0 THEN 1 ELSE 0 END) pendingBillingCount, " +
                "COALESCE(SUM(x.receivable_amount), 0) receivableAmount, " +
                "COALESCE(SUM(x.paid_amount), 0) paidAmount, " +
                "COALESCE(SUM(GREATEST(x.receivable_amount - x.paid_amount, 0)), 0) receivableBalance, " +
                "SUM(CASE WHEN x.receivable_amount > 0 AND x.invoice_count = 0 AND x.is_invoice <> '是' THEN 1 ELSE 0 END) pendingInvoiceCount " +
                "FROM (" + baseFinanceSql(null, false) + ") x";
        Map<String, Object> row = jdbcTemplate.queryForMap(sql);
        Map<String, Object> result = new HashMap<>();
        result.put("entrustCount", number(row.get("entrustCount")));
        result.put("pendingBillingCount", number(row.get("pendingBillingCount")));
        result.put("receivableAmount", money(row.get("receivableAmount")));
        result.put("paidAmount", money(row.get("paidAmount")));
        result.put("receivableBalance", money(row.get("receivableBalance")));
        result.put("pendingInvoiceCount", number(row.get("pendingInvoiceCount")));
        return result;
    }

    @Override
    public Map<String, Object> billingList(Integer pageNum, Integer pageSize, String search) {
        int offset = Math.max(pageNum - 1, 0) * pageSize;
        List<Object> params = new ArrayList<>();
        String where = buildWhere(search, params);
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_entrusted_info e " + where, params.toArray(), Long.class);

        List<Object> listParams = new ArrayList<>(params);
        listParams.add(pageSize);
        listParams.add(offset);
        String sql = "SELECT x.*, " +
                "GREATEST(x.receivable_amount - x.paid_amount, 0) balance_amount, " +
                "CASE " +
                "WHEN x.receivable_amount <= 0 THEN '待核算' " +
                "WHEN x.paid_amount >= x.receivable_amount THEN '已结清' " +
                "WHEN x.paid_amount > 0 THEN '部分回款' " +
                "ELSE '待回款' END billing_status, " +
                "CASE WHEN x.invoice_count > 0 OR x.is_invoice = '是' THEN '已登记' ELSE '未登记' END invoice_status, " +
                "CASE " +
                "WHEN x.receivable_amount <= x.paid_amount THEN '正常' " +
                "WHEN x.acceptance_date IS NULL THEN '正常' " +
                "WHEN DATEDIFF(CURDATE(), x.acceptance_date) > 60 THEN '逾期' " +
                "WHEN DATEDIFF(CURDATE(), x.acceptance_date) > 45 THEN '临期' " +
                "ELSE '正常' END risk " +
                "FROM (" + baseFinanceSql(where, true) + ") x " +
                "ORDER BY x.acceptance_date DESC, x.id DESC LIMIT ? OFFSET ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, listParams.toArray());

        Map<String, Object> result = new HashMap<>();
        result.put("total", total == null ? 0 : total);
        result.put("list", rows);
        return result;
    }

    private String baseFinanceSql(String where, boolean includeSearchWhere) {
        String whereSql = includeSearchWhere ? where : "WHERE e.id > 0 AND (e.state IS NULL OR e.state <> 144)";
        return "SELECT e.id, " +
                "IFNULL(CONCAT(e.entrust_category_type, e.entrustment_no), e.entrustment_no) entrust_no, " +
                "e.entrust_company customer, e.project_name project_name, e.entrust_test_type category, " +
                "e.acceptance_date, " +
                RECEIVABLE_AMOUNT + " receivable_amount, " +
                "COALESCE(pay.paid_amount, 0) paid_amount, " +
                "COALESCE(inv.invoice_count, 0) invoice_count, inv.last_invoice_time, COALESCE(e.is_invoice, '') is_invoice " +
                "FROM test_entrusted_info e " +
                PAID_SUBQUERY +
                INVOICE_SUBQUERY +
                whereSql;
    }

    private String buildWhere(String search, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE e.id > 0 AND (e.state IS NULL OR e.state <> 144) ");
        if (search != null && !search.trim().isEmpty()) {
            String value = "%" + search.trim() + "%";
            where.append("AND (CAST(e.entrustment_no AS CHAR) LIKE ? OR e.entrust_company LIKE ? OR e.project_name LIKE ?) ");
            params.add(value);
            params.add(value);
            params.add(value);
        }
        return where.toString();
    }

    private BigDecimal money(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return BigDecimal.ZERO;
    }

    private long number(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }
}
