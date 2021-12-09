package com.lims.manage.erp.service;

import com.lims.manage.erp.vo.CheckItemDetailVo;
import com.lims.manage.erp.vo.LabelValueVo;

import java.util.List;
import java.util.Map;

public interface ProductService {

    /**
     * 查询产品id、name--模糊查询
     *
     * @param productName
     * @return
     */
    List<LabelValueVo> selectProductList(String productName);

    /**
     * 查询产品检测项
     *
     * @param productId
     * @return
     */
    List<CheckItemDetailVo> getAllItemByProductId(Integer productId);

    /**
     * 查询判定依据
     *
     * @param productId
     * @return
     */
    List<LabelValueVo> getJudges(Integer productId);

    /**
     * 查询检测项 方法 依据
     *
     * @param id
     * @return
     */
    Map<String, List<LabelValueVo>> getItemMethodStandard(Integer id);
}
