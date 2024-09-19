package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.HKDoorLaboratoryRelEntity;
import com.lims.manage.erp.entity.HKPersonUserRelEntity;
import com.lims.manage.erp.entity.HkDoor;
import com.lims.manage.erp.result.Result;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2024-09-14 11:15
 * @Copyright © 河南交科院
 */
public interface HkDoorService extends IService<HkDoor> {


    /**
     * 编辑门禁与实验室id 进行关联
     *
     * @param hkDoorLaboratoryRelEntity
     * @return
     */
    Result editDoorLaboratoryRel(HKDoorLaboratoryRelEntity hkDoorLaboratoryRelEntity);

    /**
     * 编辑人员与userid 进行关联
     *
     * @param hkPersonUserRelEntity
     * @return
     */
    Result editPersonUserRel(HKPersonUserRelEntity hkPersonUserRelEntity);
}
