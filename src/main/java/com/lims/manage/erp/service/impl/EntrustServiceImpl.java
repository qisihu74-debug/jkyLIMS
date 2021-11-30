package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.entity.EntrustEntity;
import com.lims.manage.erp.mapper.EntrustEntityMapper;
import com.lims.manage.erp.mapper.ProductItemEntityMapper;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.vo.CheckItemDetailVo;
import com.lims.manage.erp.vo.EntrustAddVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntrustServiceImpl implements EntrustService {

    @Autowired
    private EntrustEntityMapper entityMapper;

    @Autowired
    private ProductItemEntityMapper itemEntityMapper;
    /**
     * 新增委托任务
     * @param vo
     * @return
     */
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

    @Override
    public List<CheckItemDetailVo> getAllItemByProductId(Integer productId) {
        return itemEntityMapper.getAllItemByProductId(productId);
    }
}
