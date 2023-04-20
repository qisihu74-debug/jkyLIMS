package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.ReqParamBean;

import java.io.IOException;

/**
 * @Author: DLC
 * @Date: 2023/4/19 9:58
 *  在线编辑
 */
public interface PageOfficeService {

    /**
     * 返回的 原始记录表头 塞入 的URL
     * @param bean
     * @return
     */
    String getProductExcelUrl(ReqParamBean bean) throws IOException;

}
