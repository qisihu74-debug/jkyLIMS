package com.lims.manage.erp.service.impl;

import com.github.pagehelper.PageHelper;
import com.google.common.collect.Maps;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.mapper.ProductItemEntityMapper;
import com.lims.manage.erp.mapper.TestProductDao;
import com.lims.manage.erp.service.ProductService;
import com.lims.manage.erp.vo.CheckItemDetailVo;
import com.lims.manage.erp.vo.LabelValueVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    TestProductDao testProductDao;

    @Autowired
    private ProductItemEntityMapper itemEntityMapper;

    @Override
    public List<LabelValueVo> selectProductList(String productName) {
        return testProductDao.selectProductList(productName);
    }

    @Override
    public List<CheckItemDetailVo> getAllItemByProductId(Integer productId) {
        PageHelper.clearPage();
        return itemEntityMapper.getAllItemByProductId(productId);
    }

    @Override
    public List<LabelValueVo> getJudges(Integer productId) {
        return testProductDao.getJudges(productId);
    }

    @Override
    public Map<String,List<LabelValueVo>> getItemMethodStandard(Integer id) {
        Map<String,List<LabelValueVo>> result = Maps.newHashMap();
        List<LabelValueVo> itemMethod = itemEntityMapper.getItemMethod(id);
        List<LabelValueVo> itemStandard = itemEntityMapper.getItemStandard(id);
        result.put("itemMethod",itemMethod);
        result.put("itemStandard",itemStandard);
        return result;
    }

    @Override
    public SampleEntity getProductOutward(Integer productId) {
        return testProductDao.getProductOutward(productId);
    }
}
