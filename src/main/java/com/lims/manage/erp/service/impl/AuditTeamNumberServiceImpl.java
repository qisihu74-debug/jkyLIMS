package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.AuditTeamNumber;
import com.lims.manage.erp.mapper.AuditTeamNumberDao;
import com.lims.manage.erp.service.AuditTeamNumberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2024-07-09 17:45
 * @Copyright © 河南交科院
 */
@Service
public class AuditTeamNumberServiceImpl extends ServiceImpl<AuditTeamNumberDao, AuditTeamNumber> implements AuditTeamNumberService {
    /**
     * 审核组员 更新：
     *
     * @param newAuditTeamList
     * @param activeId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAuditTeamNumber(List<AuditTeamNumber> newAuditTeamList, Integer activeId) {

        // 获取 对应组员 信息：
        LambdaQueryWrapper<AuditTeamNumber> auditTeamNumberWrapper = new LambdaQueryWrapper<>();
        auditTeamNumberWrapper.eq(AuditTeamNumber::getActiveId, activeId);
        List<AuditTeamNumber> oldAuditTeamNumberList = this.baseMapper.selectList(auditTeamNumberWrapper);
        // 获取 待删除的 组员集合
        List<AuditTeamNumber> deleteAuditTeamNumberList = oldAuditTeamNumberList.stream()
                .filter(old -> !newAuditTeamList.stream()
                        .anyMatch(newAudit -> old.getUserId().equals(newAudit.getUserId())))
                .collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(deleteAuditTeamNumberList)) {
            for (AuditTeamNumber auditTeamNumber : deleteAuditTeamNumberList) {
                LambdaQueryWrapper<AuditTeamNumber> deleteWrapper = new LambdaQueryWrapper<>();
                deleteWrapper.eq(AuditTeamNumber::getUserId, auditTeamNumber.getUserId());
                deleteWrapper.eq(AuditTeamNumber::getActiveId, activeId);
                this.baseMapper.delete(deleteWrapper);
            }
        }

        // 获取 待新增的 组员集合
        List<AuditTeamNumber> insertAuditTeamNumberList = newAuditTeamList.stream()
                .filter(newAuditData -> !oldAuditTeamNumberList.stream()
                        .anyMatch(oldAudit -> newAuditData.getUserId().equals(oldAudit.getUserId())))
                .collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(insertAuditTeamNumberList)) {
            for (AuditTeamNumber auditTeamNumber : insertAuditTeamNumberList) {
                auditTeamNumber.setActiveId(activeId);
            }
            saveBatch(insertAuditTeamNumberList);
        }


    }
}
