package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.PatentAuthorization;
import com.lims.manage.erp.mapper.PatentAuthorizationDao;
import com.lims.manage.erp.service.PatentAuthorizationService;
import org.springframework.stereotype.Service;

/**
 * @description: 专利对外授权业务层
 * @author: ycsong
 * @since: 2023/3/23
 */
@Service("patentAuthorizationService")
public class PatentAuthorizationServiceImpl extends ServiceImpl<PatentAuthorizationDao, PatentAuthorization> implements PatentAuthorizationService {


}
