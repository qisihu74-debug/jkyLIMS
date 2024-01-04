package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.InternalAudit;
import com.lims.manage.erp.entity.InternalAuditInfo;
import com.lims.manage.erp.mapper.InternalAuditDao;
import com.lims.manage.erp.mapper.InternalAuditInfoDao;
import com.lims.manage.erp.service.InternalAuditInfoService;
import com.lims.manage.erp.service.InternalAuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2024-01-02 14:51
 * @Copyright © 河南交科院
 */
@Service
@Slf4j
public class InternalAuditInfoServiceImpl extends ServiceImpl<InternalAuditInfoDao, InternalAuditInfo> implements InternalAuditInfoService {
}
