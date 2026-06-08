package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.service.FinanceService;
import com.lims.manage.erp.util.ShiroUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinanceServiceImpl implements FinanceService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    private volatile boolean invoiceLifecycleColumnsChecked = false;

    private static final String RECEIVABLE_AMOUNT =
            "COALESCE(CAST(NULLIF(REPLACE(REPLACE(e.actual_price, ',', ''), '￥', ''), '') AS DECIMAL(16,2)), " +
                    "CAST(NULLIF(REPLACE(REPLACE(e.system_price, ',', ''), '￥', ''), '') AS DECIMAL(16,2)), " +
                    "CAST(NULLIF(REPLACE(REPLACE(e.count_price, ',', ''), '￥', ''), '') AS DECIMAL(16,2)), 0)";

    private static final String PAID_SUBQUERY =
            "LEFT JOIN (SELECT entrusted_id, " +
                    "SUM(CAST(NULLIF(REPLACE(REPLACE(amount, ',', ''), '￥', ''), '') AS DECIMAL(16,2))) paid_amount " +
                    "FROM test_entrust_remittance_registration GROUP BY entrusted_id) pay ON pay.entrusted_id = e.id ";

    private static final String TASK_COST_SUBQUERY =
            "LEFT JOIN (SELECT entrustment_id, COUNT(*) task_count, COALESCE(SUM(task_price), 0) task_cost " +
                    "FROM test_task GROUP BY entrustment_id) task ON task.entrustment_id = e.id ";

    private static final String INVOICE_SUBQUERY =
            "LEFT JOIN (SELECT entrustment_id, " +
                    "SUM(CASE WHEN COALESCE(invoice_status, 'NORMAL') = 'NORMAL' THEN 1 ELSE 0 END) invoice_count, " +
                    "MAX(CASE WHEN COALESCE(invoice_status, 'NORMAL') = 'NORMAL' THEN registration_time ELSE NULL END) last_invoice_time " +
                    "FROM test_billing_registration GROUP BY entrustment_id) inv ON inv.entrustment_id = e.id ";

    @Override
    public Map<String, Object> summary() {
        ensureInvoiceLifecycleColumns();
        String sql = "SELECT COUNT(*) entrustCount, " +
                "SUM(CASE WHEN x.receivable_amount <= 0 THEN 1 ELSE 0 END) pendingBillingCount, " +
                "COALESCE(SUM(x.receivable_amount), 0) receivableAmount, " +
                "COALESCE(SUM(x.paid_amount), 0) paidAmount, " +
                "COALESCE(SUM(GREATEST(x.receivable_amount - x.paid_amount, 0)), 0) receivableBalance, " +
                "SUM(CASE WHEN x.receivable_amount > 0 AND x.invoice_count = 0 THEN 1 ELSE 0 END) pendingInvoiceCount " +
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
        ensureInvoiceLifecycleColumns();
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
                "CASE WHEN x.invoice_count > 0 THEN '已登记' ELSE '未登记' END invoice_status, " +
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateBilling(Map<String, Object> payload) {
        Long entrustId = requiredLong(payload, "entrustId", "委托单不能为空");
        BigDecimal amount = requiredMoney(payload, "amount", "核算金额不能为空");
        findFinanceRow(entrustId);

        int updated = jdbcTemplate.update(
                "UPDATE test_entrusted_info SET actual_price = ? WHERE id = ?",
                amount.toPlainString(), entrustId);
        if (updated == 0) {
            throw new IllegalArgumentException("委托单不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRemittance(Map<String, Object> payload) {
        Long entrustId = requiredLong(payload, "entrustId", "委托单不能为空");
        BigDecimal amount = requiredMoney(payload, "amount", "回款金额不能为空");
        Map<String, Object> entrust = findFinanceRow(entrustId);
        Date registrationDate = optionalDate(payload.get("registrationDate"), new Date(), "登记日期格式不正确");
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        String registrant = displayName(userInfo) + "&" + (userInfo == null ? "" : userInfo.getUserId());

        jdbcTemplate.update(
                "INSERT INTO test_entrust_remittance_registration " +
                        "(entrusted_id, entrustment_no, amount, registration_date, payment_method, payment_source, note, create_time, update_time, registrant) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?)",
                entrustId,
                stringValue(entrust.get("entrust_no")),
                amount.toPlainString(),
                registrationDate,
                optionalString(payload.get("paymentMethod")),
                optionalString(payload.get("paymentSource")),
                optionalString(payload.get("note")),
                registrant);
    }

    @Override
    public List<Map<String, Object>> remittanceList(Long entrustId) {
        findFinanceRow(entrustId);
        return jdbcTemplate.queryForList(
                "SELECT CAST(id AS CHAR) id, CAST(entrusted_id AS CHAR) entrusted_id, entrustment_no, amount, " +
                        "registration_date, payment_method, payment_source, note, create_time, update_time, registrant " +
                        "FROM test_entrust_remittance_registration " +
                        "WHERE entrusted_id = ? ORDER BY registration_date DESC, id DESC",
                entrustId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRemittance(Map<String, Object> payload) {
        Long id = requiredLong(payload, "id", "回款记录不能为空");
        BigDecimal amount = requiredMoney(payload, "amount", "回款金额不能为空");
        findRemittance(id);
        Date registrationDate = optionalDate(payload.get("registrationDate"), new Date(), "登记日期格式不正确");

        int updated = jdbcTemplate.update(
                "UPDATE test_entrust_remittance_registration " +
                        "SET amount = ?, registration_date = ?, payment_method = ?, payment_source = ?, note = ?, update_time = NOW() " +
                        "WHERE id = ?",
                amount.toPlainString(),
                registrationDate,
                optionalString(payload.get("paymentMethod")),
                optionalString(payload.get("paymentSource")),
                optionalString(payload.get("note")),
                id);
        if (updated == 0) {
            throw new IllegalArgumentException("回款记录不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRemittance(Map<String, Object> payload) {
        Long id = requiredLong(payload, "id", "回款记录不能为空");
        findRemittance(id);
        int deleted = jdbcTemplate.update("DELETE FROM test_entrust_remittance_registration WHERE id = ?", id);
        if (deleted == 0) {
            throw new IllegalArgumentException("回款记录不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addInvoice(Map<String, Object> payload) {
        ensureInvoiceLifecycleColumns();
        Long entrustId = requiredLong(payload, "entrustId", "委托单不能为空");
        Map<String, Object> entrust = findFinanceRow(entrustId);
        Date registrationTime = optionalDate(payload.get("registrationTime"), new Date(), "开票日期格式不正确");
        Long exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM test_billing_registration WHERE entrustment_id = ? OR entrustment_no = ?",
                Long.class,
                entrustId,
                stringValue(entrust.get("entrust_no")));
        if (exists != null && exists > 0) {
            throw new IllegalArgumentException("该委托单已登记开票");
        }

        BigDecimal invoiceAmount = optionalPositiveMoney(payload.get("invoiceAmount"), money(entrust.get("receivable_amount")), "开票金额格式不正确");
        BigDecimal taxRate = optionalNonNegativeMoney(payload.get("taxRate"), BigDecimal.ZERO, "税率格式不正确").setScale(4, BigDecimal.ROUND_HALF_UP);
        BigDecimal taxAmount = invoiceTaxAmount(invoiceAmount, taxRate, payload.get("taxAmount"));
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        jdbcTemplate.update(
                "INSERT INTO test_billing_registration " +
                        "(entrustment_id, entrustment_no, entrust_company, sample_name, registration_time, registered_name, registered_userid, " +
                        "invoice_no, invoice_type, invoice_amount, tax_rate, tax_amount, invoice_file_url, remark, crate_time, update_time) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())",
                entrustId,
                stringValue(entrust.get("entrust_no")),
                stringValue(entrust.get("customer")),
                sampleName(entrustId, entrust),
                registrationTime,
                displayName(userInfo),
                userInfo == null ? null : userInfo.getUserId(),
                optionalString(payload.get("invoiceNo")),
                optionalString(payload.get("invoiceType")),
                invoiceAmount.toPlainString(),
                taxRate.toPlainString(),
                taxAmount.toPlainString(),
                optionalString(payload.get("invoiceFileUrl")),
                optionalString(payload.get("remark")));
        jdbcTemplate.update("UPDATE test_entrusted_info SET is_invoice = '是' WHERE id = ?", entrustId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInvoice(Map<String, Object> payload) {
        ensureInvoiceLifecycleColumns();
        Long id = requiredLong(payload, "id", "发票记录不能为空");
        Map<String, Object> invoice = findInvoice(id);
        List<String> sets = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (payload.containsKey("registrationTime")) {
            sets.add("registration_time = ?");
            params.add(optionalDate(payload.get("registrationTime"), new Date(), "开票日期格式不正确"));
        }
        if (payload.containsKey("invoiceNo")) {
            sets.add("invoice_no = ?");
            params.add(optionalString(payload.get("invoiceNo")));
        }
        if (payload.containsKey("invoiceType")) {
            sets.add("invoice_type = ?");
            params.add(optionalString(payload.get("invoiceType")));
        }

        BigDecimal nextInvoiceAmount = money(invoice.get("invoice_amount"));
        BigDecimal nextTaxRate = money(invoice.get("tax_rate"));
        boolean amountChanged = payload.containsKey("invoiceAmount");
        boolean taxRateChanged = payload.containsKey("taxRate");
        if (amountChanged) {
            nextInvoiceAmount = optionalPositiveMoney(payload.get("invoiceAmount"), nextInvoiceAmount, "开票金额格式不正确");
            sets.add("invoice_amount = ?");
            params.add(nextInvoiceAmount.toPlainString());
        }
        if (taxRateChanged) {
            nextTaxRate = optionalNonNegativeMoney(payload.get("taxRate"), nextTaxRate, "税率格式不正确").setScale(4, BigDecimal.ROUND_HALF_UP);
            sets.add("tax_rate = ?");
            params.add(nextTaxRate.toPlainString());
        }
        if (payload.containsKey("taxAmount")) {
            BigDecimal taxAmount = optionalNonNegativeMoney(payload.get("taxAmount"), BigDecimal.ZERO, "税额格式不正确");
            sets.add("tax_amount = ?");
            params.add(taxAmount.toPlainString());
        } else if (amountChanged || taxRateChanged) {
            sets.add("tax_amount = ?");
            params.add(invoiceTaxAmount(nextInvoiceAmount, nextTaxRate, null).toPlainString());
        }
        if (payload.containsKey("invoiceFileUrl")) {
            sets.add("invoice_file_url = ?");
            params.add(optionalString(payload.get("invoiceFileUrl")));
        }
        if (payload.containsKey("remark")) {
            sets.add("remark = ?");
            params.add(optionalString(payload.get("remark")));
        }
        if (sets.isEmpty()) {
            return;
        }
        sets.add("update_time = NOW()");
        params.add(id);

        int updated = jdbcTemplate.update(
                "UPDATE test_billing_registration SET " + String.join(", ", sets) + " WHERE id = ?",
                params.toArray());
        if (updated == 0) {
            throw new IllegalArgumentException("发票记录不存在");
        }
    }

    @Override
    public Map<String, Object> invoiceLedger(Integer pageNum, Integer pageSize, String search, String status) {
        ensureInvoiceLifecycleColumns();
        int offset = Math.max(pageNum - 1, 0) * pageSize;
        List<Object> params = new ArrayList<>();
        String where = buildInvoiceWhere(search, status, params);

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM test_billing_registration b " +
                        "LEFT JOIN test_entrusted_info e ON e.id = b.entrustment_id " + where,
                params.toArray(),
                Long.class);

        List<Object> listParams = new ArrayList<>(params);
        listParams.add(pageSize);
        listParams.add(offset);
        String sql = "SELECT CAST(b.id AS CHAR) id, CAST(b.entrustment_id AS CHAR) entrust_id, " +
                "b.entrustment_no entrust_no, b.entrust_company customer, e.project_name project_name, " +
                "b.invoice_no, b.invoice_type, COALESCE(b.invoice_amount, " + RECEIVABLE_AMOUNT + ") invoice_amount, " +
                "b.tax_rate, b.tax_amount, b.invoice_file_url, b.sample_name, b.registration_time, b.registered_name, " +
                "b.remark, b.crate_time, b.update_time, COALESCE(b.invoice_status, 'NORMAL') invoice_status, " +
                "b.status_reason, b.status_time, b.status_user " +
                "FROM test_billing_registration b " +
                "LEFT JOIN test_entrusted_info e ON e.id = b.entrustment_id " +
                where +
                "ORDER BY b.registration_time DESC, b.id DESC LIMIT ? OFFSET ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, listParams.toArray());

        Map<String, Object> result = new HashMap<>();
        result.put("total", total == null ? 0 : total);
        result.put("list", rows);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInvoiceStatus(Map<String, Object> payload) {
        ensureInvoiceLifecycleColumns();
        Long id = requiredLong(payload, "id", "发票记录不能为空");
        String status = requiredString(payload, "status", "发票状态不能为空");
        validateInvoiceStatus(status);
        String reason = optionalString(payload.get("reason"));
        if (!"NORMAL".equals(status) && (reason == null || reason.trim().isEmpty())) {
            throw new IllegalArgumentException("作废/红冲原因不能为空");
        }
        Map<String, Object> invoice = findInvoice(id);
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        String statusUser = displayName(userInfo) + "&" + (userInfo == null ? "" : userInfo.getUserId());

        int updated = jdbcTemplate.update(
                "UPDATE test_billing_registration SET invoice_status = ?, status_reason = ?, status_time = NOW(), status_user = ?, update_time = NOW() WHERE id = ?",
                status,
                reason,
                statusUser,
                id);
        if (updated == 0) {
            throw new IllegalArgumentException("发票记录不存在");
        }

        Object entrustIdValue = invoice.get("entrust_id");
        if (entrustIdValue != null && !String.valueOf(entrustIdValue).trim().isEmpty()) {
            Long entrustId = Long.valueOf(String.valueOf(entrustIdValue));
            Long activeCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM test_billing_registration WHERE entrustment_id = ? AND COALESCE(invoice_status, 'NORMAL') = 'NORMAL'",
                    Long.class,
                    entrustId);
            jdbcTemplate.update(
                    "UPDATE test_entrusted_info SET is_invoice = ? WHERE id = ?",
                    activeCount != null && activeCount > 0 ? "是" : "否",
                    entrustId);
        }
    }

    @Override
    public Map<String, Object> profitAnalysis(Integer pageNum, Integer pageSize, String search) {
        int offset = Math.max(pageNum - 1, 0) * pageSize;
        List<Object> params = new ArrayList<>();
        String where = buildWhere(search, params);

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM test_entrusted_info e " + where,
                params.toArray(),
                Long.class);

        String summarySql = "SELECT COUNT(*) entrustCount, " +
                "COALESCE(SUM(x.revenue_amount), 0) revenueAmount, " +
                "COALESCE(SUM(x.cost_amount), 0) costAmount, " +
                "COALESCE(SUM(x.revenue_amount - x.cost_amount), 0) profitAmount, " +
                "CASE WHEN COALESCE(SUM(x.revenue_amount), 0) > 0 " +
                "THEN ROUND(COALESCE(SUM(x.revenue_amount - x.cost_amount), 0) / SUM(x.revenue_amount) * 100, 2) ELSE 0 END profitRate " +
                "FROM (" + baseProfitSql(where) + ") x";
        Map<String, Object> summaryRow = jdbcTemplate.queryForMap(summarySql, params.toArray());

        List<Object> listParams = new ArrayList<>(params);
        listParams.add(pageSize);
        listParams.add(offset);
        String listSql = "SELECT x.*, " +
                "(x.revenue_amount - x.cost_amount) profit_amount, " +
                "CASE WHEN x.revenue_amount > 0 THEN ROUND((x.revenue_amount - x.cost_amount) / x.revenue_amount * 100, 2) ELSE 0 END profit_rate " +
                "FROM (" + baseProfitSql(where) + ") x " +
                "ORDER BY profit_amount ASC, x.acceptance_date DESC, x.id DESC LIMIT ? OFFSET ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(listSql, listParams.toArray());

        Map<String, Object> result = new HashMap<>();
        result.put("total", total == null ? 0 : total);
        result.put("list", rows);
        result.put("entrustCount", number(summaryRow.get("entrustCount")));
        result.put("revenueAmount", money(summaryRow.get("revenueAmount")));
        result.put("costAmount", money(summaryRow.get("costAmount")));
        result.put("profitAmount", money(summaryRow.get("profitAmount")));
        result.put("profitRate", money(summaryRow.get("profitRate")));
        return result;
    }

    @Override
    public Map<String, Object> statement(Long entrustId) {
        ensureInvoiceLifecycleColumns();
        Map<String, Object> entrust = findFinanceRow(entrustId);
        List<Map<String, Object>> remittances = jdbcTemplate.queryForList(
                "SELECT id, amount, registration_date, payment_method, payment_source, note, create_time, registrant " +
                        "FROM test_entrust_remittance_registration WHERE entrusted_id = ? ORDER BY registration_date ASC, id ASC",
                entrustId);
        List<Map<String, Object>> invoices = jdbcTemplate.queryForList(
                "SELECT CAST(id AS CHAR) id, entrustment_no, entrust_company, sample_name, registration_time, registered_name, " +
                        "invoice_no, invoice_type, invoice_amount, tax_rate, tax_amount, invoice_file_url, remark, crate_time, " +
                        "COALESCE(invoice_status, 'NORMAL') invoice_status, status_reason, status_time, status_user " +
                        "FROM test_billing_registration WHERE entrustment_id = ? OR entrustment_no = ? ORDER BY registration_time ASC, id ASC",
                entrustId,
                stringValue(entrust.get("entrust_no")));

        Map<String, Object> result = new HashMap<>();
        result.put("entrust", entrust);
        result.put("remittances", remittances);
        result.put("invoices", invoices);
        return result;
    }

    private String baseFinanceSql(String where, boolean includeSearchWhere) {
        String whereSql = includeSearchWhere ? where : "WHERE e.id > 0 AND (e.state IS NULL OR e.state <> 144)";
        return "SELECT CAST(e.id AS CHAR) id, " +
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

    private String baseProfitSql(String where) {
        return "SELECT CAST(e.id AS CHAR) id, " +
                "IFNULL(CONCAT(e.entrust_category_type, e.entrustment_no), e.entrustment_no) entrust_no, " +
                "e.entrust_company customer, e.project_name project_name, e.entrust_test_type category, " +
                "e.acceptance_date, " +
                RECEIVABLE_AMOUNT + " revenue_amount, " +
                "COALESCE(task.task_cost, 0) cost_amount, COALESCE(task.task_count, 0) task_count, " +
                "COALESCE(pay.paid_amount, 0) paid_amount " +
                "FROM test_entrusted_info e " +
                PAID_SUBQUERY +
                TASK_COST_SUBQUERY +
                where;
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

    private String buildInvoiceWhere(String search, String status, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE b.id > 0 ");
        if (search != null && !search.trim().isEmpty()) {
            String value = "%" + search.trim() + "%";
            where.append("AND (b.entrustment_no LIKE ? OR b.entrust_company LIKE ? OR e.project_name LIKE ? OR b.registered_name LIKE ? OR b.invoice_no LIKE ?) ");
            params.add(value);
            params.add(value);
            params.add(value);
            params.add(value);
            params.add(value);
        }
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            validateInvoiceStatus(status.trim());
            where.append("AND COALESCE(b.invoice_status, 'NORMAL') = ? ");
            params.add(status.trim());
        }
        return where.toString();
    }

    private Map<String, Object> findFinanceRow(Long entrustId) {
        ensureInvoiceLifecycleColumns();
        String where = "WHERE e.id = ? AND (e.state IS NULL OR e.state <> 144) ";
        String sql = "SELECT x.*, " +
                "GREATEST(x.receivable_amount - x.paid_amount, 0) balance_amount, " +
                "CASE " +
                "WHEN x.receivable_amount <= 0 THEN '待核算' " +
                "WHEN x.paid_amount >= x.receivable_amount THEN '已结清' " +
                "WHEN x.paid_amount > 0 THEN '部分回款' " +
                "ELSE '待回款' END billing_status, " +
                "CASE WHEN x.invoice_count > 0 THEN '已登记' ELSE '未登记' END invoice_status, " +
                "CASE " +
                "WHEN x.receivable_amount <= x.paid_amount THEN '正常' " +
                "WHEN x.acceptance_date IS NULL THEN '正常' " +
                "WHEN DATEDIFF(CURDATE(), x.acceptance_date) > 60 THEN '逾期' " +
                "WHEN DATEDIFF(CURDATE(), x.acceptance_date) > 45 THEN '临期' " +
                "ELSE '正常' END risk " +
                "FROM (" + baseFinanceSql(where, true) + ") x";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, entrustId);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("委托单不存在");
        }
        return rows.get(0);
    }

    private Map<String, Object> findInvoice(Long id) {
        ensureInvoiceLifecycleColumns();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT CAST(id AS CHAR) id, CAST(entrustment_id AS CHAR) entrust_id, entrustment_no, entrust_company, " +
                        "invoice_no, invoice_type, invoice_amount, tax_rate, tax_amount, invoice_file_url, registration_time, remark, " +
                        "COALESCE(invoice_status, 'NORMAL') invoice_status, status_reason, status_time, status_user " +
                        "FROM test_billing_registration WHERE id = ?",
                id);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("发票记录不存在");
        }
        return rows.get(0);
    }

    private Map<String, Object> findRemittance(Long id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT CAST(id AS CHAR) id, CAST(entrusted_id AS CHAR) entrusted_id, entrustment_no, amount, " +
                        "registration_date, payment_method, payment_source, note, create_time, update_time, registrant " +
                        "FROM test_entrust_remittance_registration WHERE id = ?",
                id);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("回款记录不存在");
        }
        return rows.get(0);
    }

    private Long requiredLong(Map<String, Object> payload, String key, String message) {
        Object value = payload == null ? null : payload.get(key);
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        try {
            return Long.valueOf(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message);
        }
    }

    private String requiredString(Map<String, Object> payload, String key, String message) {
        Object value = payload == null ? null : payload.get(key);
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return String.valueOf(value).trim();
    }

    private void validateInvoiceStatus(String status) {
        if (!"NORMAL".equals(status) && !"VOID".equals(status) && !"RED_OFFSET".equals(status)) {
            throw new IllegalArgumentException("发票状态不正确");
        }
    }

    private synchronized void ensureInvoiceLifecycleColumns() {
        if (invoiceLifecycleColumnsChecked) {
            return;
        }
        if (!hasColumn("test_billing_registration", "invoice_status")) {
            jdbcTemplate.execute("ALTER TABLE test_billing_registration " +
                    "ADD COLUMN invoice_status VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '发票状态：NORMAL正常，VOID作废，RED_OFFSET红冲'");
        }
        if (!hasColumn("test_billing_registration", "status_reason")) {
            jdbcTemplate.execute("ALTER TABLE test_billing_registration " +
                    "ADD COLUMN status_reason VARCHAR(255) NULL COMMENT '发票状态变更原因'");
        }
        if (!hasColumn("test_billing_registration", "status_time")) {
            jdbcTemplate.execute("ALTER TABLE test_billing_registration " +
                    "ADD COLUMN status_time DATETIME NULL COMMENT '发票状态变更时间'");
        }
        if (!hasColumn("test_billing_registration", "status_user")) {
            jdbcTemplate.execute("ALTER TABLE test_billing_registration " +
                    "ADD COLUMN status_user VARCHAR(100) NULL COMMENT '发票状态变更人'");
        }
        if (!hasColumn("test_billing_registration", "invoice_no")) {
            jdbcTemplate.execute("ALTER TABLE test_billing_registration " +
                    "ADD COLUMN invoice_no VARCHAR(100) NULL COMMENT '发票号码'");
        }
        if (!hasColumn("test_billing_registration", "invoice_type")) {
            jdbcTemplate.execute("ALTER TABLE test_billing_registration " +
                    "ADD COLUMN invoice_type VARCHAR(50) NULL COMMENT '发票类型'");
        }
        if (!hasColumn("test_billing_registration", "invoice_amount")) {
            jdbcTemplate.execute("ALTER TABLE test_billing_registration " +
                    "ADD COLUMN invoice_amount DECIMAL(16,2) NULL COMMENT '含税开票金额'");
        }
        if (!hasColumn("test_billing_registration", "tax_rate")) {
            jdbcTemplate.execute("ALTER TABLE test_billing_registration " +
                    "ADD COLUMN tax_rate DECIMAL(8,4) NULL COMMENT '税率百分比'");
        }
        if (!hasColumn("test_billing_registration", "tax_amount")) {
            jdbcTemplate.execute("ALTER TABLE test_billing_registration " +
                    "ADD COLUMN tax_amount DECIMAL(16,2) NULL COMMENT '税额'");
        }
        if (!hasColumn("test_billing_registration", "invoice_file_url")) {
            jdbcTemplate.execute("ALTER TABLE test_billing_registration " +
                    "ADD COLUMN invoice_file_url VARCHAR(2048) NULL COMMENT '票据附件或链接'");
        }
        invoiceLifecycleColumnsChecked = true;
    }

    private boolean hasColumn(String tableName, String columnName) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                        "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Long.class,
                tableName,
                columnName);
        return count != null && count > 0;
    }

    private BigDecimal optionalPositiveMoney(Object value, BigDecimal defaultValue, String message) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return defaultValue == null ? BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP) : defaultValue.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        try {
            BigDecimal amount = new BigDecimal(String.valueOf(value).replace(",", "").replace("￥", "").trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(message);
            }
            return amount.setScale(2, BigDecimal.ROUND_HALF_UP);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message);
        }
    }

    private BigDecimal optionalNonNegativeMoney(Object value, BigDecimal defaultValue, String message) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return defaultValue == null ? BigDecimal.ZERO : defaultValue;
        }
        try {
            BigDecimal amount = new BigDecimal(String.valueOf(value).replace(",", "").replace("￥", "").trim());
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(message);
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message);
        }
    }

    private BigDecimal invoiceTaxAmount(BigDecimal invoiceAmount, BigDecimal taxRate, Object taxAmountValue) {
        if (taxAmountValue != null && !String.valueOf(taxAmountValue).trim().isEmpty()) {
            return optionalNonNegativeMoney(taxAmountValue, BigDecimal.ZERO, "税额格式不正确").setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        if (invoiceAmount == null || taxRate == null || invoiceAmount.compareTo(BigDecimal.ZERO) <= 0 || taxRate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return invoiceAmount.multiply(taxRate)
                .divide(taxRate.add(new BigDecimal("100")), 2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal requiredMoney(Map<String, Object> payload, String key, String message) {
        Object value = payload == null ? null : payload.get(key);
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        try {
            BigDecimal amount = new BigDecimal(String.valueOf(value).replace(",", "").replace("￥", "").trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(message);
            }
            return amount.setScale(2, BigDecimal.ROUND_HALF_UP);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message);
        }
    }

    private Date optionalDate(Object value, Date defaultValue, String message) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return defaultValue;
        }
        String text = String.valueOf(value).trim();
        List<String> patterns = new ArrayList<>();
        patterns.add("yyyy-MM-dd HH:mm:ss");
        patterns.add("yyyy-MM-dd");
        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern).parse(text);
            } catch (ParseException ignored) {
            }
        }
        throw new IllegalArgumentException(message);
    }

    private String optionalString(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String displayName(SysUserEntity userInfo) {
        if (userInfo == null) {
            return "系统";
        }
        if (userInfo.getName() != null && !userInfo.getName().trim().isEmpty()) {
            return userInfo.getName();
        }
        return userInfo.getUsername() == null ? "系统" : userInfo.getUsername();
    }

    private String sampleName(Long entrustId, Map<String, Object> entrust) {
        String sql = "SELECT GROUP_CONCAT(s.alias_name SEPARATOR ', ') " +
                "FROM test_entrusted_sample_details_rel rel " +
                "LEFT JOIN test_sample s ON rel.sample_id = s.id " +
                "WHERE rel.entrustment_id = ?";
        String sampleName = jdbcTemplate.queryForObject(sql, String.class, entrustId);
        if (sampleName != null && !sampleName.trim().isEmpty()) {
            return sampleName;
        }
        String projectName = stringValue(entrust.get("project_name"));
        return projectName.isEmpty() ? stringValue(entrust.get("category")) : projectName;
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
