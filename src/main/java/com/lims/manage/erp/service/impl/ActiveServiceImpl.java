package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.AuditTeamNumber;
import com.lims.manage.erp.entity.DivideEntity;
import com.lims.manage.erp.entity.QsActiveEntity;
import com.lims.manage.erp.entity.QsAuditScheduleEntity;
import com.lims.manage.erp.mapper.ActiveMapper;
import com.lims.manage.erp.mapper.AuditTeamNumberDao;
import com.lims.manage.erp.mapper.DivideDao;
import com.lims.manage.erp.mapper.QsAuditScheduleMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.ActiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 内审基础信息
 * @Author: DLC
 * @Date: 2024/7/10 16:25
 */
@Service
public class ActiveServiceImpl extends ServiceImpl<ActiveMapper, QsActiveEntity> implements ActiveService {


    @Autowired
    private AuditTeamNumberDao auditTeamNumberDao;
    @Autowired
    private DivideDao divideDao;
    @Autowired
    private QsAuditScheduleMapper qsAuditScheduleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addQsActiveData(QsActiveEntity qsActiveEntity) {
        // 进行保存内审表
        this.baseMapper.insert(qsActiveEntity);
        Integer activeId = qsActiveEntity.getActiveId();
        System.out.println("内容输出 activeId == " + activeId);

        // 进行 内审组员的批量保存
        if (CollectionUtil.isNotEmpty(qsActiveEntity.getAuditTeamList())) {
            for (AuditTeamNumber auditTeamNumber : qsActiveEntity.getAuditTeamList()) {
                auditTeamNumber.setActiveId(activeId);
                auditTeamNumberDao.insert(auditTeamNumber);
            }
        }
        // 评审分工的 新增
        if (CollectionUtil.isNotEmpty(qsActiveEntity.getDivideList())) {
            // 获取当前 最大id +1.
            QueryWrapper<DivideEntity> entityLambdaQueryChainWrapper = new QueryWrapper<>();
            entityLambdaQueryChainWrapper.select("IFNULL(max( divide_id ) + 1,1) as as divide_id");
            entityLambdaQueryChainWrapper.last("limit 1");
            DivideEntity divideEntity = divideDao.selectOne(entityLambdaQueryChainWrapper);
            Integer divideId = divideEntity.getDivideId();
            // key = deptId value = 对应的id
            Map<Integer, Integer> map = new HashMap<>();
            for (DivideEntity divideEntity1 : qsActiveEntity.getDivideList()) {
                if (map.get(divideEntity1.getDeptId()) == null) {
                    map.put(Integer.parseInt(divideEntity1.getDeptId()), divideId);
                    divideId = divideId + 1;
                }
            }
            for (DivideEntity divideEntity1 : qsActiveEntity.getDivideList()) {
                // 活动id 根据指派的人员相同科室 进行一致。
                divideEntity1.setActiveId(activeId);
                if (map.get(divideEntity1.getDeptId()) != null) {
                    divideEntity1.setDivideId(map.get(divideEntity1.getDeptId()));
                }
                divideDao.insert(divideEntity1);
            }
        }
        // 日程安排
        if (CollectionUtil.isNotEmpty(qsActiveEntity.getQsAuditScheduleEntityList())) {
            for (QsAuditScheduleEntity qsAuditScheduleEntity : qsActiveEntity.getQsAuditScheduleEntityList()) {
                qsAuditScheduleEntity.setActiveId(activeId);
                qsAuditScheduleMapper.insert(qsAuditScheduleEntity);
            }
        }

        return ResultUtil.success("创建内审活动成功");
    }
}
