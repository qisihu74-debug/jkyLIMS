package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.SysLogininfor;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2024-01-18 11:30
 * @Copyright © 河南交科院
 */
public interface SysLoginLogDao extends BaseMapper<SysLogininfor> {

    @Insert("insert into sys_login_log (user_name, status, ipaddr, login_location, browser, os, msg, login_time)\n" +
            "\t\tvalues (#{userName}, #{status}, #{ipaddr}, #{loginLocation}, #{browser}, #{os}, #{msg}, sysdate())")
    void save(SysLogininfor sysLogininfor);
}
