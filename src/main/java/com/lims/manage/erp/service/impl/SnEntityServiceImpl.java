package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.SnEntity;
import com.lims.manage.erp.mapper.SnEntityMapper;
import com.lims.manage.erp.service.ISnEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @Description: 自定义编号
 * @Author: zhq
 * @Date: 2023-06-13
 * @Version: V1.0
 */
@Service
public class SnEntityServiceImpl extends ServiceImpl<SnEntityMapper, SnEntity> implements ISnEntityService {
    @Autowired
    private SnEntityMapper snEntityMapper;

    @Override
    public void updateResetByTypeAndTenantId(String receiptType,int status) {
        snEntityMapper.updateResetByTypeAndTenantId(receiptType,status);
    }
}
