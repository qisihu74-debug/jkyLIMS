package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestMethod;
import com.lims.manage.erp.entity.TestProductItemMethodRel;
import com.lims.manage.erp.result.Result;

import java.util.List;

/**
 * 检测项的检测方法(TestProductItemMethodRel)表服务接口
 *
 * @author makejava
 * @since 2022-03-02 15:15:27
 */
public interface TestProductItemMethodRelService extends IService<TestProductItemMethodRel> {
    Result addTestMethodRel(TestProductItemMethodRel testProductItemMethodRel);
    Result updTestMethodRel(TestProductItemMethodRel testProductItemMethodRel);
    Result delTestMethodRel(List<Long> idList);
}

