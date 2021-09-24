package com.stu.manage.demo.service;

import com.stu.manage.demo.entity.CheckItemCostVo;
import com.stu.manage.demo.entity.EntrustInfo;
import com.stu.manage.demo.entity.ProductVo;
import com.stu.manage.demo.entity.StatusEntity;

import java.util.List;

public interface EntrustService {
    /**
     * 再来一单--委托基本信息
     * @param entrustId
     * @return
     */
    EntrustInfo onceMore(int entrustId);

    /**
     * 根据产品获取产品全部检测项
     * @param productId
     * @return
     */
    List<CheckItemCostVo> getCheckItemsByProductId(int productId);
    /**
     * 根据产品获取产品检测依据
     * @param productId
     * @return
     */
    List<ProductVo> getCheckBasisByProductId(int productId);

    /**
     * 查询委托单状态
     * @param id
     * @return
     */
    StatusEntity status(Integer id);
}
