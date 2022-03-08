package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestMethod;
import com.lims.manage.erp.entity.TestProductItem;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.TestProductItemVo;

import java.util.List;

/**
 * 产品检测项(TestProductItem)表服务接口
 *
 * @author makejava
 * @since 2022-03-02 15:14:51
 */
public interface TestProductItemService extends IService<TestProductItem> {
    Result addTestProductItem(TestProductItemVo testProductItem);
    Result updTestProductItem(TestProductItemVo testProductItem);
    Result delTestProductItem(List<Long> idList);
}

