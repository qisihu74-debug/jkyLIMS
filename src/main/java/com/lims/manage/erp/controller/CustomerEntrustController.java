package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lims.manage.erp.entity.CustomerAccountEntity;
import com.lims.manage.erp.entity.CustomerEntrustDraftEntity;
import com.lims.manage.erp.entity.EntrustEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.mapper.CustomerEntrustDraftMapper;
import com.lims.manage.erp.mapper.EntrustEntityMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.util.CustomerPortalAuthSupport;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.util.SnowflakeIdGenerator;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer/entrust")
public class CustomerEntrustController {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_ACCEPTED = "ACCEPTED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    @Autowired
    private CustomerPortalAuthSupport customerAuth;
    @Autowired
    private CustomerEntrustDraftMapper draftMapper;
    @Autowired
    private EntrustEntityMapper entrustMapper;
    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

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
        if (STATUS_ACCEPTED.equals(draft.getStatus())) {
            return ResultUtil.error(500, "草稿已受理，不能取消");
        }
        draft.setStatus(STATUS_CANCELLED);
        draft.setUpdateTime(now());
        draftMapper.updateById(draft);
        return ResultUtil.success(draft);
    }

    @GetMapping("/admin/drafts")
    public Result adminDrafts(@RequestParam(required = false) String status,
                              @RequestParam(required = false) String keyword) {
        SysUserEntity user = ShiroUtils.getUserInfo();
        if (user == null) return ResultUtil.error(-1, "未登录");
        LambdaQueryWrapper<CustomerEntrustDraftEntity> wrapper = new LambdaQueryWrapper<CustomerEntrustDraftEntity>()
                .ne(CustomerEntrustDraftEntity::getStatus, STATUS_CANCELLED)
                .orderByDesc(CustomerEntrustDraftEntity::getSubmitTime)
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
                    .or().like(CustomerEntrustDraftEntity::getEntrustPeople, key)
                    .or().like(CustomerEntrustDraftEntity::getSampleNames, key));
        }
        List<CustomerEntrustDraftEntity> list = draftMapper.selectList(wrapper);
        return ResultUtil.success(list);
    }

    @PostMapping("/admin/review")
    @Transactional
    public Result adminReview(@RequestBody Map<String, Object> body) {
        SysUserEntity user = ShiroUtils.getUserInfo();
        if (user == null) return ResultUtil.error(-1, "未登录");
        Long id = lng(body.get("id"));
        Boolean approve = bool(body.get("approve"));
        String remark = str(body.get("remark"));
        if (id == null || approve == null) {
            return ResultUtil.error(500, "受理参数不完整");
        }
        CustomerEntrustDraftEntity draft = draftMapper.selectById(id);
        if (draft == null) return ResultUtil.error(404, "草稿不存在");
        if (!STATUS_SUBMITTED.equals(draft.getStatus())) {
            return ResultUtil.error(500, "只有待受理草稿可以处理");
        }
        Timestamp reviewTime = now();
        draft.setReviewRemark(remark);
        draft.setReviewUserId(user.getUserId());
        draft.setReviewUserName(displayUser(user));
        draft.setReviewTime(reviewTime);
        if (approve) {
            EntrustEntity entrust = buildPreEntrust(draft, user, remark);
            entrustMapper.insertEntrustInfo(entrust);
            draft.setStatus(STATUS_ACCEPTED);
            draft.setFormalEntrustId(entrust.getId());
            draft.setFormalEntrustmentNo(entrust.getEntrustmentNo());
        } else {
            draft.setStatus(STATUS_REJECTED);
        }
        draft.setUpdateTime(reviewTime);
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
            if (STATUS_ACCEPTED.equals(draft.getStatus())) {
                return ResultUtil.error(500, "草稿已受理，不能再次提交");
            }
            draft.setStatus(STATUS_SUBMITTED);
            draft.setSubmitTime(now());
        } else {
            if (STATUS_SUBMITTED.equals(draft.getStatus()) || STATUS_ACCEPTED.equals(draft.getStatus())) {
                return ResultUtil.error(500, "草稿已提交，不能继续修改");
            }
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

    private EntrustEntity buildPreEntrust(CustomerEntrustDraftEntity draft, SysUserEntity user, String reviewRemark) {
        Date nowDate = new Date();
        EntrustEntity entrust = new EntrustEntity();
        entrust.setId(GenID.getID());
        entrust.setEntrustmentNo(snowflakeIdGenerator.nextId());
        entrust.setEntrustType("客户自助委托");
        entrust.setEntrustCompany(draft.getEntrustCompany());
        entrust.setEntrustCompanyId(draft.getBindCompanyId());
        entrust.setEntrustPeople(draft.getEntrustPeople());
        entrust.setEntrustPhone(draft.getEntrustPhone());
        entrust.setWitnessUint(draft.getWitnessUnit());
        entrust.setWitnessPerson(draft.getWitnessPerson());
        entrust.setWitnessPhone(draft.getWitnessPhone());
        entrust.setProjectName(draft.getProjectName());
        entrust.setProjectPart(draft.getProjectPart());
        entrust.setSamplingMethod("客户自助填报");
        entrust.setCheckPurpose(defaultText(draft.getCheckItems(), "客户自助委托"));
        entrust.setReportCount(draft.getReportCount() == null ? 1 : draft.getReportCount());
        entrust.setReportType(StringUtils.defaultIfBlank(draft.getReportType(), "电子报告"));
        entrust.setRequestDate(parseRequestDate(draft.getRequestDate(), nowDate));
        entrust.setAcceptanceDate(nowDate);
        entrust.setBusinessAcceptor(displayUser(user));
        entrust.setState(201);
        entrust.setRemark(buildFormalRemark(draft, reviewRemark));
        entrust.setOperateUser(user.getUserId());
        entrust.setOperateDate(nowDate);
        entrust.setIsSave("否");
        entrust.setAddress(draft.getAddress());
        entrust.setAddressee(draft.getEntrustPeople());
        entrust.setMobile(draft.getEntrustPhone());
        entrust.setTaskSource("客户门户");
        entrust.setAuditState("1");
        entrust.setReportReceivingUnit(draft.getEntrustCompany());
        entrust.setCreateTime(nowDate);
        entrust.setOperatingPersonnel(displayUser(user));
        entrust.setIsReserve("否");
        entrust.setOperateType(0);
        return entrust;
    }

    private String buildFormalRemark(CustomerEntrustDraftEntity draft, String reviewRemark) {
        StringBuilder builder = new StringBuilder();
        builder.append("客户门户草稿号：").append(draft.getDraftNo());
        if (StringUtils.isNotBlank(draft.getSampleNames())) {
            builder.append("\n样品信息：").append(draft.getSampleNames());
        }
        if (StringUtils.isNotBlank(draft.getCheckItems())) {
            builder.append("\n检测项目：").append(draft.getCheckItems());
        }
        if (StringUtils.isNotBlank(draft.getRemark())) {
            builder.append("\n客户备注：").append(draft.getRemark());
        }
        if (StringUtils.isNotBlank(reviewRemark)) {
            builder.append("\n受理备注：").append(reviewRemark);
        }
        return builder.toString();
    }

    private Date parseRequestDate(String value, Date fallback) {
        if (StringUtils.isBlank(value)) return fallback;
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(value.trim());
        } catch (ParseException e) {
            return fallback;
        }
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.defaultIfBlank(value, fallback);
    }

    private String displayUser(SysUserEntity user) {
        return StringUtils.defaultIfBlank(user.getName(), user.getUsername());
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

    private Boolean bool(Object value) {
        if (value == null || StringUtils.isBlank(String.valueOf(value))) return null;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.valueOf(String.valueOf(value));
    }
}
