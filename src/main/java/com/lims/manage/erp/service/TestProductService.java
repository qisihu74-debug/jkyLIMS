package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestInstrument;
import com.lims.manage.erp.entity.TestProduct;
import com.lims.manage.erp.entity.TestProduct;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.TestInstrumentVo;
import com.lims.manage.erp.vo.TestProductItemVo;
import com.lims.manage.erp.vo.TestProductSelVo;
import com.lims.manage.erp.vo.TestProductVo;

import java.util.List;

/**
 * 产品信息(TestProduct)表服务接口
 *
 * @author makejava
 * @since 2022-03-02 15:00:15
 */
public interface TestProductService extends IService<TestProduct> {
    Result addTestProduct(TestProductItemVo testProductItemVo);

    Result updTestProduct(TestProductItemVo testProductItemVo);

    /**
     * 删除产品-及绑定关系
     * @param idList
     * @return
     */
    Result delTestProduct(List<Long> idList);

    IPage<TestProductVo> getPageList(Page<TestProductVo> page, QueryWrapper<TestProduct> queryWrapper);

    TestProductSelVo getTestProductSelVo(TestProduct testProduct);

    /**
     * 查询详情
     *
     * @param testProduct
     * @return
     */
    TestProductItemVo getTestProductItemVo(TestProduct testProduct);

    TestProduct getProductInfo(Integer productId);

    Result updateProductStatus(TestProductItemVo testProductItemVo);
}

