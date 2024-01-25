package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.SysOperLog;
import org.apache.ibatis.annotations.Insert;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2021/11/15 16:42
 * @Copyright © 河南交科院
 */
public interface SysOperateLogDao extends BaseMapper<SysOperLog> {

    @Insert("insert into sys_operate_log(title, business_type, method, request_method, operator_type, oper_name, dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg, cost_time, oper_time)\n" +
            "        values (#{title}, #{businessType}, #{method}, #{requestMethod}, #{operatorType}, #{operName}, #{deptName}, #{operUrl}, #{operIp}, #{operLocation}, #{operParam}, #{jsonResult}, #{status}, #{errorMsg}, #{costTime}, sysdate())")
    void save(SysOperLog operLog);
}
