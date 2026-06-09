package com.lims.manage.erp.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lims.manage.erp.entity.CustomerAccountEntity;
import com.lims.manage.erp.mapper.CustomerAccountMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;

@Component
public class CustomerPortalAuthSupport {

    @Autowired
    private CustomerAccountMapper customerAccountMapper;

    public CustomerAccountEntity requireCustomer(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token)) token = request.getParameter("token");
        if (StringUtils.isBlank(token)) return null;
        return customerAccountMapper.selectOne(new LambdaQueryWrapper<CustomerAccountEntity>()
                .eq(CustomerAccountEntity::getLastToken, token)
                .ne(CustomerAccountEntity::getState, "PROHIBIT")
                .gt(CustomerAccountEntity::getTokenExpireTime, new Timestamp(System.currentTimeMillis()))
                .last("LIMIT 1"));
    }

    public Result unauth() {
        return ResultUtil.error(-1, "客户登录已过期，请重新登录");
    }
}
