package com.lims.manage.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lims.manage.erp.entity.CustomerAccountEntity;
import com.lims.manage.erp.entity.CustomerClaimRequestEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestCompanyEntity;
import com.lims.manage.erp.mapper.CustomerAccountMapper;
import com.lims.manage.erp.mapper.CustomerClaimRequestMapper;
import com.lims.manage.erp.mapper.CustomerPortalMapper;
import com.lims.manage.erp.mapper.TestCompanyDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.util.SHA256Util;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.CustomerClaimAdminVo;
import com.lims.manage.erp.vo.CustomerClaimCandidateVo;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/customer")
public class CustomerAuthController {

    private static final String STATE_NORMAL = "NORMAL";
    private static final String STATE_CLAIMED = "CLAIMED";
    private static final String STATE_PROHIBIT = "PROHIBIT";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    @Autowired
    private CustomerAccountMapper customerAccountMapper;
    @Autowired
    private CustomerClaimRequestMapper customerClaimRequestMapper;
    @Autowired
    private CustomerPortalMapper customerPortalMapper;
    @Autowired
    private TestCompanyDao testCompanyDao;

    @PostMapping("/sendSmsCode")
    public Result sendSmsCode(@RequestBody Map<String, Object> body) {
        String mobile = str(body.get("mobile"));
        if (StringUtils.isBlank(mobile)) {
            return ResultUtil.error(500, "手机号不能为空");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("mobile", mobile);
        data.put("debugCode", "000000");
        data.put("message", "短信通道待接入，staging 暂用 000000");
        return ResultUtil.success(data);
    }

    @PostMapping("/register")
    @Transactional
    public Result register(@RequestBody Map<String, Object> body) {
        String mobile = str(body.get("mobile"));
        String password = str(body.get("password"));
        String name = str(body.get("name"));
        if (StringUtils.isBlank(mobile) || StringUtils.isBlank(password) || StringUtils.isBlank(name)) {
            return ResultUtil.error(500, "姓名、手机号和密码不能为空");
        }
        CustomerAccountEntity exists = customerAccountMapper.selectOne(new LambdaQueryWrapper<CustomerAccountEntity>()
                .eq(CustomerAccountEntity::getMobile, mobile)
                .last("LIMIT 1"));
        if (exists != null) {
            return ResultUtil.error(500, "该手机号已注册");
        }

        Timestamp now = now();
        String salt = RandomStringUtils.randomAlphanumeric(20);
        CustomerAccountEntity account = new CustomerAccountEntity();
        account.setMobile(mobile);
        account.setName(name);
        account.setSalt(salt);
        account.setPassword(SHA256Util.sha256(password, salt));
        account.setState(STATE_NORMAL);
        account.setCreateTime(now);
        account.setUpdateTime(now);
        issueToken(account, now);
        customerAccountMapper.insert(account);
        return ResultUtil.success(buildLoginPayload(account));
    }

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, Object> body) {
        String mobile = str(body.get("mobile"));
        String password = str(body.get("password"));
        if (StringUtils.isBlank(mobile) || StringUtils.isBlank(password)) {
            return ResultUtil.error(500, "手机号和密码不能为空");
        }
        CustomerAccountEntity account = customerAccountMapper.selectOne(new LambdaQueryWrapper<CustomerAccountEntity>()
                .eq(CustomerAccountEntity::getMobile, mobile)
                .last("LIMIT 1"));
        if (account == null || STATE_PROHIBIT.equals(account.getState())) {
            return ResultUtil.error(500, "账号不存在或已停用");
        }
        if (!SHA256Util.sha256(password, account.getSalt()).equals(account.getPassword())) {
            return ResultUtil.error(500, "手机号或密码错误");
        }
        Timestamp now = now();
        issueToken(account, now);
        customerAccountMapper.updateById(account);
        return ResultUtil.success(buildLoginPayload(account));
    }

    @GetMapping("/profile")
    public Result profile(HttpServletRequest request) {
        CustomerAccountEntity account = requireCustomer(request);
        if (account == null) return customerUnauth();
        Map<String, Object> data = new HashMap<>();
        data.put("account", account);
        data.put("claim", latestClaim(account.getAccountId()));
        data.put("userType", "customer");
        return ResultUtil.success(data);
    }

    @GetMapping("/claim/candidates")
    public Result claimCandidates(HttpServletRequest request,
                                  @RequestParam(required = false) String name,
                                  @RequestParam(required = false) String mobile,
                                  @RequestParam(required = false) String companyName) {
        CustomerAccountEntity account = requireCustomer(request);
        if (account == null) return customerUnauth();
        String finalName = StringUtils.defaultIfBlank(name, account.getName());
        String finalMobile = StringUtils.defaultIfBlank(mobile, account.getMobile());
        if (StringUtils.isBlank(finalName) && StringUtils.isBlank(finalMobile) && StringUtils.isBlank(companyName)) {
            return ResultUtil.error(500, "请至少填写姓名、手机号或单位名称");
        }
        List<CustomerClaimCandidateVo> candidates = customerPortalMapper.selectClaimCandidates(finalName, finalMobile, companyName);
        return ResultUtil.success(candidates);
    }

    @PostMapping("/claim/apply")
    @Transactional
    public Result applyClaim(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        CustomerAccountEntity account = requireCustomer(request);
        if (account == null) return customerUnauth();
        Integer companyId = integer(body.get("candidateCompanyId"));
        Integer customerId = integer(body.get("candidateCustomerId"));
        String matchBasis = str(body.get("matchBasis"));
        String applyRemark = str(body.get("applyRemark"));
        if (companyId == null) {
            return ResultUtil.error(500, "请选择要认领的历史客户单位");
        }
        TestCompanyEntity company = testCompanyDao.selectById(companyId);
        if (company == null) {
            return ResultUtil.error(500, "候选单位不存在");
        }
        CustomerClaimRequestEntity existing = customerClaimRequestMapper.selectOne(new LambdaQueryWrapper<CustomerClaimRequestEntity>()
                .eq(CustomerClaimRequestEntity::getAccountId, account.getAccountId())
                .in(CustomerClaimRequestEntity::getStatus, STATUS_PENDING, STATUS_APPROVED)
                .orderByDesc(CustomerClaimRequestEntity::getId)
                .last("LIMIT 1"));
        if (existing != null) {
            return ResultUtil.success("已有认领记录", existing);
        }

        Timestamp now = now();
        CustomerClaimRequestEntity entity = new CustomerClaimRequestEntity();
        entity.setAccountId(account.getAccountId());
        entity.setCandidateCompanyId(companyId);
        entity.setCandidateCustomerId(customerId);
        entity.setMatchBasis(StringUtils.defaultIfBlank(matchBasis, "客户自行选择历史单位"));
        entity.setApplyRemark(applyRemark);
        entity.setStatus(STATUS_PENDING);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        customerClaimRequestMapper.insert(entity);
        return ResultUtil.success(entity);
    }

    @GetMapping("/claim/myStatus")
    public Result myClaimStatus(HttpServletRequest request) {
        CustomerAccountEntity account = requireCustomer(request);
        if (account == null) return customerUnauth();
        Map<String, Object> data = new HashMap<>();
        data.put("account", account);
        data.put("claim", latestClaim(account.getAccountId()));
        return ResultUtil.success(data);
    }

    @GetMapping("/claim/list")
    public Result claimReviewList(@RequestParam(required = false) String status) {
        SysUserEntity user = ShiroUtils.getUserInfo();
        if (user == null) return ResultUtil.error(-1, "未登录");
        List<CustomerClaimAdminVo> list = customerPortalMapper.selectClaimReviewList(status);
        return ResultUtil.success(list);
    }

    @PostMapping("/claim/review")
    @Transactional
    public Result reviewClaim(@RequestBody Map<String, Object> body) {
        SysUserEntity user = ShiroUtils.getUserInfo();
        if (user == null) return ResultUtil.error(-1, "未登录");
        Long id = lng(body.get("id"));
        Boolean approve = bool(body.get("approve"));
        String remark = str(body.get("remark"));
        if (id == null || approve == null) {
            return ResultUtil.error(500, "审核参数不完整");
        }
        CustomerClaimRequestEntity claim = customerClaimRequestMapper.selectById(id);
        if (claim == null) return ResultUtil.error(500, "认领申请不存在");
        if (!STATUS_PENDING.equals(claim.getStatus())) {
            return ResultUtil.error(500, "该申请已审核，不能重复处理");
        }

        Timestamp now = now();
        claim.setStatus(approve ? STATUS_APPROVED : STATUS_REJECTED);
        claim.setReviewRemark(remark);
        claim.setReviewUserId(user.getUserId());
        claim.setReviewTime(now);
        claim.setUpdateTime(now);
        customerClaimRequestMapper.updateById(claim);

        if (approve) {
            customerAccountMapper.update(null, new LambdaUpdateWrapper<CustomerAccountEntity>()
                    .eq(CustomerAccountEntity::getAccountId, claim.getAccountId())
                    .set(CustomerAccountEntity::getBindCompanyId, claim.getCandidateCompanyId())
                    .set(CustomerAccountEntity::getBindCustomerId, claim.getCandidateCustomerId())
                    .set(CustomerAccountEntity::getState, STATE_CLAIMED)
                    .set(CustomerAccountEntity::getUpdateTime, now));
        }
        return ResultUtil.success(claim);
    }

    private CustomerClaimRequestEntity latestClaim(Long accountId) {
        if (accountId == null) return null;
        return customerClaimRequestMapper.selectOne(new LambdaQueryWrapper<CustomerClaimRequestEntity>()
                .eq(CustomerClaimRequestEntity::getAccountId, accountId)
                .orderByDesc(CustomerClaimRequestEntity::getId)
                .last("LIMIT 1"));
    }

    private void issueToken(CustomerAccountEntity account, Timestamp now) {
        account.setLastToken("customer:" + UUID.randomUUID().toString().replace("-", ""));
        account.setTokenExpireTime(Timestamp.from(Instant.now().plus(Duration.ofDays(7))));
        account.setLastLoginTime(now);
        account.setUpdateTime(now);
    }

    private Map<String, Object> buildLoginPayload(CustomerAccountEntity account) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("token", account.getLastToken());
        payload.put("userType", "customer");
        payload.put("account", account);
        payload.put("claim", latestClaim(account.getAccountId()));
        return payload;
    }

    private CustomerAccountEntity requireCustomer(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token)) token = request.getParameter("token");
        if (StringUtils.isBlank(token)) return null;
        return customerAccountMapper.selectOne(new LambdaQueryWrapper<CustomerAccountEntity>()
                .eq(CustomerAccountEntity::getLastToken, token)
                .ne(CustomerAccountEntity::getState, STATE_PROHIBIT)
                .gt(CustomerAccountEntity::getTokenExpireTime, now())
                .last("LIMIT 1"));
    }

    private Result customerUnauth() {
        return ResultUtil.error(-1, "客户登录已过期，请重新登录");
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
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.valueOf(String.valueOf(value));
    }
}
