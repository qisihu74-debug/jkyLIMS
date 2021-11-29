package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.entity.EntrustEntity;
import com.lims.manage.erp.mapper.EntrustEntityMapper;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.vo.EntrustAddVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntrustServiceImpl implements EntrustService {
    @Autowired
    private EntrustEntityMapper entityMapper;


    @Override
    public Boolean addEntrust(EntrustAddVo vo) {
        Boolean result = false;
        //存放委托基本信息
        EntrustEntity basisInfo = new EntrustEntity(vo);
        entityMapper.insert(basisInfo);
        //存放样品信息
        result = true;
        return result;
    }
}
