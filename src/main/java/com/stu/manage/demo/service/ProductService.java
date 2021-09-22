package com.stu.manage.demo.service;

import com.stu.manage.demo.entity.ProductVo;

import java.util.List;

public interface ProductService {
    /**
     * 获取所有产品类型
     * @return
     */
    List<ProductVo> getProductType();

    /**
     * 根据产品类型获取该类型下所有产品
     * @return
     */
    List<ProductVo> getProduct(int type);
}
