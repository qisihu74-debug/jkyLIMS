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

        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        jdbcTemplate.update(
                "INSERT INTO test_billing_registration " +
                        "(entrustment_id, entrustment_no, entrust_company, sample_name, registration_time, registered_name, registered_userid, remark, crate_time, update_time) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())",
                entrustId,
                stringValue(entrust.get("entrust_no")),
                stringValue(entrust.get("customer")),
                sampleName(entrustId, entrust),
                registrationTime,
                displayName(userInfo),
                userInfo == null ? null : userInfo.getUserId(),
                optionalString(payload.get("remark")));
        jdbcTemplate.update("UPDATE test_entrusted_info SET is_invoice = '是' WHERE id = ?", entrustId);
    }

    @Override
    public Map<String, Object> statement(Long entrustId) {
        Map<String, Object> entrust = findFinanceRow(entrustId);
        List<Map<String, Object>> remittances = jdbcTemplate.queryForList(
                "SELECT id, amount, registration_date, payment_method, payment_source, note, create_time, registrant " +
                        "FROM test_entrust_remittance_registration WHERE entrusted_id = ? ORDER BY registration_date ASC, id ASC",
                entrustId);
        List<Map<String, Object>> invoices = jdbcTemplate.queryForList(
                "SELECT id, entrustment_no, entrust_company, sample_name, registration_time, registered_name, remark, crate_time " +
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

    private Map<String, Object> findFinanceRow(Long entrustId) {
        String where = "WHERE e.id = ? AND (e.state IS NULL OR e.state <> 144) ";
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
                "FROM (" + baseFinanceSql(where, true) + ") x";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, entrustId);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("委托单不存在");
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
