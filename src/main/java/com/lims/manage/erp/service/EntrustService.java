package com.lims.manage.erp.service;

import com.lims.manage.erp.vo.EntrustAddVo;

public interface EntrustService{
    /**
     * 新增委托
     * @param vo
     * @return
     */
    Boolean addEntrust(EntrustAddVo vo);
}
