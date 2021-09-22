package com.stu.manage.demo.service.impl;

import com.stu.manage.demo.entity.ProductVo;
import com.stu.manage.demo.mapper.ProductMapper;
import com.stu.manage.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductMapper productMapper;
    @Override
    public List<ProductVo> getProductType() {
        return productMapper.getProductType();
    }

    @Override
    public List<ProductVo> getProduct(int type) {
        return productMapper.getProduct(type);
    }
}
