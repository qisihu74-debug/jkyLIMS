package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.ReqParamBean;
import com.zhuozhengsoft.pageoffice.FileSaver;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @Author: DLC
 * @Date: 2023/4/19 9:58
 *  在线编辑
 */
public interface PageOfficeService {

    /**
     * 返回的 原始记录表头 塞入 的URL
     * @param ids
     * @return
     */
    String getProductExcelUrl(Integer[] ids) throws IOException;

    /**
     * 完成编辑
     * @param request
     * @param file
     * @return
     */
    String saveOriginalRecord(HttpServletRequest request, FileSaver file);

}
