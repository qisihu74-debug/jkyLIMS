package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.DivideRectificationRecord;
import com.lims.manage.erp.mapper.DivideRectificationRecordDao;
import com.lims.manage.erp.service.DivideRectificationRecordService;
import org.springframework.stereotype.Service;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2024-07-09 16:09
 * @Copyright © 河南交科院
 */
@Service
public class DivideRectificationRecordServiceImpl extends ServiceImpl<DivideRectificationRecordDao, DivideRectificationRecord> implements DivideRectificationRecordService {

    @javax.annotation.Resource
    private com.lims.manage.erp.mapper.DivideRectificationRecordDao nonconformityDao;

    @Override
    public java.util.List<com.lims.manage.erp.vo.NonconformityVo> nonconformityList(String state, String deptName) {
        return nonconformityDao.nonconformityList(state, deptName);
    }
}
