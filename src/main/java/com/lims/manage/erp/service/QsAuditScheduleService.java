package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.QsAuditScheduleEntity;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2024/7/12 14:14
 */
public interface QsAuditScheduleService extends IService<QsAuditScheduleEntity> {
    /**
     * 更新日程安排
     *
     * @param newAuditScheduleEntityList
     * @param activeId
     */
    public void updateAuditSchedule(List<QsAuditScheduleEntity> newAuditScheduleEntityList, Integer activeId);

}
