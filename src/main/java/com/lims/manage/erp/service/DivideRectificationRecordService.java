package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.DivideRectificationRecord;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2024-07-09 16:08
 * @Copyright © 河南交科院
 */
public interface DivideRectificationRecordService extends IService<DivideRectificationRecord> {

    java.util.List<com.lims.manage.erp.vo.NonconformityVo> nonconformityList(String state, String deptName);
}
