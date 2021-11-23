package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.DingDeptEntity;
import com.lims.manage.erp.vo.DingDeptVo;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2021/11/19 15:16
 * @Copyright © 河南交科院
 */
public interface DeptService extends IService<DingDeptEntity> {
    /**
     * 查询组织架构信息--树状
     * @return
     */
    List<DingDeptVo> getAllDept();
}
