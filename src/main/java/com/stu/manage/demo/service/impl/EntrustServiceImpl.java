package com.stu.manage.demo.service.impl;

import com.stu.manage.demo.entity.*;
import com.stu.manage.demo.mapper.EntrustMapper;
import com.stu.manage.demo.service.EntrustService;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntrustServiceImpl implements EntrustService {
    @Autowired
    private EntrustMapper entrustMapper;
    @Override
    public EntrustInfo onceMore(int entrustId) {
        EntrustInfo entrustInfo = entrustMapper.onceMore(entrustId);
        List<SampleInfoVo> sampleInfo = entrustMapper.getSampleInfo(entrustId);
        List<CheckItemInfoVo> checkItemInfo = entrustMapper.getCheckItemInfo(entrustId);

        for (int i = 0; i < sampleInfo.size(); i++) {
            SampleInfoVo sampleInfoVo = sampleInfo.get(i);
            List<CheckItemInfoVo> items = Lists.newArrayList();
            for (int j = 0; j < checkItemInfo.size(); j++) {
                CheckItemInfoVo checkItemInfoVo = checkItemInfo.get(j);
                if(sampleInfoVo.getProductId() == checkItemInfoVo.getProductId()){
                    items.add(checkItemInfoVo);
                }
            }
            sampleInfoVo.setItems(items);
        }

        EntrustInfo result = new EntrustInfo(entrustInfo,sampleInfo);

        return result;
    }

    @Override
    public List<CheckItemCostVo> getCheckItemsByProductId(int productId) {
        return entrustMapper.getCheckItemsByProductId(productId);
    }

    @Override
    public List<ProductVo> getCheckBasisByProductId(int productId) {
        return entrustMapper.getCheckBasisByProductId(productId);
    }
}
