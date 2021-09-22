package com.stu.manage.demo.controller;

import com.stu.manage.demo.result.Result;
import com.stu.manage.demo.result.ResultUtil;
import com.stu.manage.demo.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/product/")
public class ProductController {
    Logger logger = LoggerFactory.getLogger(ProductController.class);
    @Autowired
    private ProductService productService;

    @GetMapping("product_types")
    public Result getProductType(){
        return ResultUtil.success(productService.getProductType());
    }

    @GetMapping("products")
    public Result getProduct(int type){
        return ResultUtil.success(productService.getProduct(type));
    }
}
