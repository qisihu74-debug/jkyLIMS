package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.mapper.TestProductSpecsDao;
import com.lims.manage.erp.entity.TestProductSpecs;
import com.lims.manage.erp.service.TestProductSpecsService;
import org.springframework.stereotype.Service;

/**
 * 产品规格等级(TestProductSpecs)表服务实现类
 *
 * @author makejava
 * @since 2022-03-08 17:12:49
 */
@Service("testProductSpecsService")
public class TestProductSpecsServiceImpl extends ServiceImpl<TestProductSpecsDao, TestProductSpecs> implements TestProductSpecsService {

}

