package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.QsAuditScheduleEntity;
import com.lims.manage.erp.mapper.QsAuditScheduleMapper;
import com.lims.manage.erp.service.QsAuditScheduleService;
import com.lims.manage.erp.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: DLC
 * @Date: 2024/7/12 14:17
 */
@Service
public class QsAuditScheduleServiceImpl extends ServiceImpl<QsAuditScheduleMapper, QsAuditScheduleEntity> implements QsAuditScheduleService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAuditSchedule(List<QsAuditScheduleEntity> newAuditScheduleEntityList, Integer activeId) {

        if (CollectionUtil.isNotEmpty(newAuditScheduleEntityList)) {
            for (QsAuditScheduleEntity qsAuditScheduleEntity : newAuditScheduleEntityList) {
                // 进行 审核周期 拆分: auditTimeCycle
                if (StringUtils.isNotEmpty(qsAuditScheduleEntity.getScheduleDateCycle())) {
                    String[] times = qsAuditScheduleEntity.getScheduleDateCycle().split("~");
                    // "2024-07-16" 转 Date 格式
                    qsAuditScheduleEntity.setStartTime(DateUtil.timeFormat(times[0]));
                    qsAuditScheduleEntity.setEndTime(DateUtil.timeFormat(times[1]));
                }
            }
        }

        // 获取 对应日程 信息：
        LambdaQueryWrapper<QsAuditScheduleEntity> auditScheduleWrapper = new LambdaQueryWrapper<>();
        auditScheduleWrapper.eq(QsAuditScheduleEntity::getActiveId, activeId);
        List<QsAuditScheduleEntity> oldAuditScheduleList = this.baseMapper.selectList(auditScheduleWrapper);
        // 获取 待删除的 日程集合
        List<QsAuditScheduleEntity> deleteAuditScheduleList = oldAuditScheduleList.stream()
                .filter(old -> !newAuditScheduleEntityList.stream()
                        .anyMatch(newAudit -> old.getScheduleId().equals(newAudit.getScheduleId())))
                .collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(deleteAuditScheduleList)) {
            for (QsAuditScheduleEntity auditSchedule : deleteAuditScheduleList) {
                LambdaQueryWrapper<QsAuditScheduleEntity> deleteWrapper = new LambdaQueryWrapper<>();
                deleteWrapper.eq(QsAuditScheduleEntity::getScheduleId, auditSchedule.getScheduleId());
                this.baseMapper.delete(deleteWrapper);
            }
        }

        // 获取 待新增的 日程集合
        for (QsAuditScheduleEntity qsAuditScheduleEntity : newAuditScheduleEntityList) {
            if (qsAuditScheduleEntity.getScheduleId() == null) {
                qsAuditScheduleEntity.setActiveId(activeId);
            }
        }
        saveOrUpdateBatch(newAuditScheduleEntityList);
    }
}
