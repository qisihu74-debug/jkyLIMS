package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lims.manage.erp.entity.CustomerAccountEntity;
import com.lims.manage.erp.entity.CustomerEntrustDraftEntity;
import com.lims.manage.erp.mapper.CustomerEntrustDraftMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.util.CustomerPortalAuthSupport;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer/entrust")
public class CustomerEntrustController {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    @Autowired
    private CustomerPortalAuthSupport customerAuth;
    @Autowired
    private CustomerEntrustDraftMapper draftMapper;

    @GetMapping("/drafts")
    public Result drafts(HttpServletRequest request,
                         @RequestParam(required = false) String status,
                         @RequestParam(required = false) String keyword) {
        CustomerAccountEntity account = customerAuth.requireCustomer(request);
        if (account == null) return customerAuth.unauth();
        LambdaQueryWrapper<CustomerEntrustDraftEntity> wrapper = new LambdaQueryWrapper<CustomerEntrustDraftEntity>()
                .eq(CustomerEntrustDraftEntity::getAccountId, account.getAccountId())
                .ne(CustomerEntrustDraftEntity::getStatus, STATUS_CANCELLED)
                .orderByDesc(CustomerEntrustDraftEntity::getUpdateTime)
                .orderByDesc(CustomerEntrustDraftEntity::getId);
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(CustomerEntrustDraftEntity::getStatus, status.trim());
        }
        if (StringUtils.isNotBlank(keyword)) {
            String key = keyword.trim();
            wrapper.and(q -> q.like(CustomerEntrustDraftEntity::getDraftNo, key)
                    .or().like(CustomerEntrustDraftEntity::getProjectName, key)
                    .or().like(CustomerEntrustDraftEntity::getEntrustCompany, key)
                    .or().like(CustomerEntrustDraftEntity::getSampleNames, key));
        }
        List<CustomerEntrustDraftEntity> list = draftMapper.selectList(wrapper);
        return ResultUtil.success(list);
    }

    @GetMapping("/draft/{id}")
    public Result detail(HttpServletRequest request, @PathVariable Long id) {
        CustomerAccountEntity account = customerAuth.requireCustomer(request);
        if (account == null) return customerAuth.unauth();
        CustomerEntrustDraftEntity draft = loadOwnDraft(account, id);
        if (draft == null) return ResultUtil.error(404, "草稿不存在");
        return ResultUtil.success(draft);
    }

    @PostMapping("/draft/save")
    @Transactional
    public Result save(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        return upsert(request, body, false);
    }

    @PostMapping("/draft/submit")
    @Transactional
    public Result submit(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        return upsert(request, body, true);
    }

    @PostMapping("/draft/cancel")
    @Transactional
    public Result cancel(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        CustomerAccountEntity account = customerAuth.requireCustomer(request);
        if (account == null) return customerAuth.unauth();
        Long id = lng(body.get("id"));
        if (id == null) return ResultUtil.error(500, "缺少草稿ID");
        CustomerEntrustDraftEntity draft = loadOwnDraft(account, id);
        if (draft == null) return ResultUtil.error(404, "草稿不存在");
        draft.setStatus(STATUS_CANCELLED);
        draft.setUpdateTime(now());
        draftMapper.updateById(draft);
        return ResultUtil.success(draft);
    }

    private Result upsert(HttpServletRequest request, Map<String, Object> body, boolean submit) {
        CustomerAccountEntity account = customerAuth.requireCustomer(request);
        if (account == null) return customerAuth.unauth();
        Long id = lng(body.get("id"));
        CustomerEntrustDraftEntity draft = id == null ? null : loadOwnDraft(account, id);
        if (id != null && draft == null) return ResultUtil.error(404, "草稿不存在");
        if (draft == null) {
            draft = new CustomerEntrustDraftEntity();
            draft.setAccountId(account.getAccountId());
            draft.setDraftNo(nextDraftNo());
            draft.setCreateTime(now());
        }
        fillPayload(draft, account, body);
        if (submit) {
            Result invalid = validateSubmit(draft);
            if (invalid != null) return invalid;
            draft.setStatus(STATUS_SUBMITTED);
            draft.setSubmitTime(now());
        } else {
            draft.setStatus(StringUtils.defaultIfBlank(draft.getStatus(), STATUS_DRAFT));
        }
        draft.setUpdateTime(now());
        if (draft.getId() == null) {
            draftMapper.insert(draft);
        } else {
            draftMapper.updateById(draft);
        }
        return ResultUtil.success(draft);
    }

    private void fillPayload(CustomerEntrustDraftEntity draft, CustomerAccountEntity account, Map<String, Object> body) {
        draft.setBindCompanyId(account.getBindCompanyId());
        draft.setBindCustomerId(account.getBindCustomerId());
        draft.setEntrustCompany(str(body.get("entrustCompany")));
        draft.setEntrustPeople(StringUtils.defaultIfBlank(str(body.get("entrustPeople")), account.getName()));
        draft.setEntrustPhone(StringUtils.defaultIfBlank(str(body.get("entrustPhone")), account.getMobile()));
        draft.setWitnessUnit(str(body.get("witnessUnit")));
        draft.setWitnessPerson(str(body.get("witnessPerson")));
        draft.setWitnessPhone(str(body.get("witnessPhone")));
        draft.setProjectName(str(body.get("projectName")));
        draft.setProjectPart(str(body.get("projectPart")));
        draft.setSampleNames(str(body.get("sampleNames")));
        draft.setCheckItems(str(body.get("checkItems")));
        draft.setRequestDate(str(body.get("requestDate")));
        draft.setReportCount(integer(body.get("reportCount")));
        draft.setReportType(str(body.get("reportType")));
        draft.setAddress(str(body.get("address")));
        draft.setRemark(str(body.get("remark")));
    }

    private Result validateSubmit(CustomerEntrustDraftEntity draft) {
        if (StringUtils.isBlank(draft.getEntrustCompany())
                || StringUtils.isBlank(draft.getEntrustPeople())
                || StringUtils.isBlank(draft.getEntrustPhone())
                || StringUtils.isBlank(draft.getProjectName())
                || StringUtils.isBlank(draft.getSampleNames())
                || StringUtils.isBlank(draft.getCheckItems())) {
            return ResultUtil.error(500, "委托单位、联系人、电话、工程名称、样品信息和检测项目不能为空");
        }
        return null;
    }

    private CustomerEntrustDraftEntity loadOwnDraft(CustomerAccountEntity account, Long id) {
        if (id == null) return null;
        return draftMapper.selectOne(new LambdaQueryWrapper<CustomerEntrustDraftEntity>()
                .eq(CustomerEntrustDraftEntity::getId, id)
                .eq(CustomerEntrustDraftEntity::getAccountId, account.getAccountId())
                .last("LIMIT 1"));
    }

    private String nextDraftNo() {
        return "CED" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + RandomStringUtils.randomNumeric(4);
    }

    private Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private Integer integer(Object value) {
        if (value == null || StringUtils.isBlank(String.valueOf(value))) return null;
        return Integer.valueOf(String.valueOf(value));
    }

    private Long lng(Object value) {
        if (value == null || StringUtils.isBlank(String.valueOf(value))) return null;
        return Long.valueOf(String.valueOf(value));
    }
}
