package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestProductType;
import com.lims.manage.erp.result.Result;

import java.util.List;

/**
 * 产品分类(TestProductType)表服务接口
 *
 * @author makejava
 * @since 2022-03-02 10:03:13
 */
public interface TestProductTypeService extends IService<TestProductType> {
    Result addProductType(TestProductType type);
    Result updProductType(TestProductType type);
    Result delProductType(List<Long> idList);
}

