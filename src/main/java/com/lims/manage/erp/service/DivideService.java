package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.DivideEntity;
import com.lims.manage.erp.vo.DivideVo;

import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2024-07-10 11:30
 * @Copyright © 河南交科院
 */
public interface DivideService extends IService<DivideEntity> {

    /**
     * 更新 评审分工信息
     *
     * @param newDivideVoList
     * @param activeId
     */
    public void updateDivide(List<DivideVo> newDivideVoList, Integer activeId);

}
