package com.lims.manage.erp.service;

import com.lims.manage.erp.vo.CheckItemDetailVo;
import com.lims.manage.erp.vo.EntrustAddVo;

import java.util.List;

public interface EntrustService{
    /**
     * 新增委托
     * @param vo
     * @return
     */
    Boolean addEntrust(EntrustAddVo vo);

    /**
     * 查询产品检测项
     * @param productId
     * @return
     */
    List<CheckItemDetailVo> getAllItemByProductId(Integer productId);
}
