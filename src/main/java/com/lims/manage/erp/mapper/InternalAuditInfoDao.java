package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.InternalAudit;
import com.lims.manage.erp.entity.InternalAuditInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2024-01-02 14:52
 * @Copyright © 河南交科院
 */
@Component
@Mapper
public interface InternalAuditInfoDao extends BaseMapper<InternalAuditInfo> {
}
