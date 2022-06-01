package com.lims.manage.erp.controller;

import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.ProductService;
import com.lims.manage.erp.vo.LabelValueVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/product/")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private LogManagerService logManagerService;

    /**
     * 样品基本信息--查询产品
     *
     * @param productName
     * @return
     */
    @RequestMapping("/getProductList")
    public Result getAllItemByProductName(String productName) {
        List<LabelValueVo> dataList = productService.selectProductList(productName);
        if (dataList.isEmpty()) {
            return ResultUtil.error(ResultEnum.DATA_IS_NULL.getCode(), ResultEnum.DATA_IS_NULL.getMsg());
        }
        return ResultUtil.success(dataList);
    }

    /**
     * 查询产品判定依据
     *
     * @param productId
     * @return
     */
    @RequestMapping("/getJudgeBasis")
    public Result getJudgeBasis(Integer productId) {
        if (productId == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            return ResultUtil.success(productService.getJudges(productId));
        }
    }

    /**
     * 查询检测项 方法 依据
     *
     * @param itemId
     * @return
     */
    @RequestMapping("/getItemMethodStandard")
    public Result getItemMethodStandard(Integer itemId) {
        if (itemId == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            return ResultUtil.success(productService.getItemMethodStandard(itemId));
        }
    }

    /**
     * 查询产品的外观描述
     * @param productId
     * @return
     */
    @GetMapping("/getProductOutward")
    public Result getProductOutward(Integer productId) {
        if (productId == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }
        String productOutward = productService.getProductOutward(productId);
        return ResultUtil.success(productOutward);
    }
}
