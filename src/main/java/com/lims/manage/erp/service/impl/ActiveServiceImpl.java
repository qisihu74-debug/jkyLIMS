package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.lims.manage.erp.service.AuditTeamNumberService;
import com.lims.manage.erp.service.DivideService;
import com.lims.manage.erp.service.QsAuditScheduleService;
import com.lims.manage.erp.vo.DivideVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    private AuditTeamNumberService auditTeamNumberService;
    @Autowired
    private DivideDao divideDao;
    @Autowired
    private QsAuditScheduleMapper qsAuditScheduleMapper;
    @Autowired
    private QsAuditScheduleService qsAuditScheduleService;
    @Autowired
    private DivideService divideService;

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
            entityLambdaQueryChainWrapper.select("IFNULL(max( divide_id ) + 1,1) as divide_id");
            entityLambdaQueryChainWrapper.last("limit 1");
            DivideEntity divideEntity = divideDao.selectOne(entityLambdaQueryChainWrapper);
            Integer divideId = divideEntity.getDivideId();
            // key = deptId value = 对应的id
            Map<String, Integer> map = new HashMap<>();
            for (DivideVo divideVo : qsActiveEntity.getDivideList()) {
                if (map.get(divideVo.getDeptId()) == null) {
                    map.put(divideVo.getDeptId(), divideId);
                    divideId = divideId + 1;
                }
            }
            for (DivideVo divideVo : qsActiveEntity.getDivideList()) {
                // 活动id 根据指派的人员相同科室 进行一致。
                for (DivideEntity divideEntity1 : divideVo.getDivideList()) {
                    divideEntity1.setDivideId(map.get(divideVo.getDeptId()));
                    divideEntity1.setActiveId(activeId);
                    divideDao.insert(divideEntity1);
                }
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

    /**
     * 更新内审活动
     *
     * @param qsActiveEntity
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateQsActiveData(QsActiveEntity qsActiveEntity) {
        // 内审计划信息 更新：
        this.baseMapper.updateById(qsActiveEntity);

        // 审核组员 更新：
        if (CollectionUtil.isNotEmpty(qsActiveEntity.getAuditTeamList())) {
            // 调用方法 ： 执行 更新
            auditTeamNumberService.updateAuditTeamNumber(qsActiveEntity.getAuditTeamList(), qsActiveEntity.getActiveId());
        } else {
            // 组员信息 全部删除
            //
        }

        // 评审分工 更新：
        if (CollectionUtil.isNotEmpty(qsActiveEntity.getDivideList())) {
            // 新增 或 删除
            List<DivideVo> newDivideVoList = qsActiveEntity.getDivideList();
            // 调用方法 执行 更新评审分工信息
            divideService.updateDivide(newDivideVoList, qsActiveEntity.getActiveId());
        }

        // 日程安排
        if (CollectionUtil.isNotEmpty(qsActiveEntity.getQsAuditScheduleEntityList())) {

            // 调用更新操作
            qsAuditScheduleService.updateAuditSchedule(qsActiveEntity.getQsAuditScheduleEntityList(), qsActiveEntity.getActiveId());

        } else {
            // 日程信息 全部删除
            //
        }

        return ResultUtil.success("变更内审活动成功");
    }

    /**
     * 查询详情内审活动
     *
     * @param activeId
     * @return
     */
    @Override
    public Result queryDetailsQsActiveData(String activeId) {
        //验证 内审是否存在
        LambdaQueryWrapper<QsActiveEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QsActiveEntity::getActiveId, activeId);
        queryWrapper.last("LIMIT 1");
        QsActiveEntity qsActiveEntity = this.baseMapper.selectOne(queryWrapper);
        if (qsActiveEntity == null) {
            // 不存在
            return null;
        }
        // 进行供返回数据
        Map<String, Object> map = new HashMap<>();

        // 获取内审计划详情：

        //      1、评审组员:
        List<AuditTeamNumber> teamNumberList = new ArrayList<>();
        LambdaQueryWrapper<AuditTeamNumber> teamNumberWrapper = new LambdaQueryWrapper<>();
        teamNumberWrapper.eq(AuditTeamNumber::getActiveId, activeId);
        teamNumberList = auditTeamNumberDao.selectList(teamNumberWrapper);
        qsActiveEntity.setAuditTeamList(teamNumberList);

        //      2、评审分工：
        List<DivideEntity> divideList = new ArrayList<>();
        LambdaQueryWrapper<DivideEntity> divideWrapper = new LambdaQueryWrapper<>();
        divideWrapper.eq(DivideEntity::getActiveId, activeId);
        // 排序: 按照分工id 排序 正序
        divideWrapper.orderByAsc(DivideEntity::getDivideId);
        divideList = divideDao.selectList(divideWrapper);
        if (CollectionUtil.isNotEmpty(divideList)) {
            // 待返回的分工集合
            List<DivideVo> divideVoList = new ArrayList<>();
            // key = deptId，value 为集合
            LinkedHashMap<String, List<DivideEntity>> deptMap = new LinkedHashMap<>();

            for (DivideEntity divideEntity : divideList) {
                // deptId 数据为空
                if (deptMap.get(divideEntity.getDeptId() + "&" + divideEntity.getDeptName()) == null) {
                    List<DivideEntity> divideEntities = new ArrayList<>();
                    divideEntities.add(divideEntity);
                    deptMap.put(divideEntity.getDeptId() + "&" + divideEntity.getDeptName(), divideEntities);
                } else {
                    List<DivideEntity> deptLists = deptMap.get(divideEntity.getDeptId() + "&" + divideEntity.getDeptName());
                    deptLists.add(divideEntity);
                    deptMap.put(divideEntity.getDeptId() + "&" + divideEntity.getDeptName(), deptLists);
                }
            }

            // 拆分map数据
            for (String key : deptMap.keySet()) {
                List<DivideEntity> divideEntities = deptMap.get(key);
                DivideVo divideVo = new DivideVo();
                String[] arrays = key.split("&");
                divideVo.setDeptId(arrays[0]);
                divideVo.setDeptName(arrays[1]);
                divideVo.setActiveId(divideEntities.get(0).getActiveId());
                divideVo.setDivideId(divideEntities.get(0).getDivideId());
                divideVo.setDivideList(divideEntities);
                divideVoList.add(divideVo);
            }
            qsActiveEntity.setDivideList(divideVoList);
        }


        //      3、日程 安排
        List<QsAuditScheduleEntity> auditScheduleList = new ArrayList<>();
        LambdaQueryWrapper<QsAuditScheduleEntity> auditScheduleWrapper = new LambdaQueryWrapper<>();
        auditScheduleWrapper.eq(QsAuditScheduleEntity::getActiveId, activeId);
        // 排序： 按照scheduleId 排序 正序
        auditScheduleWrapper.orderByAsc(QsAuditScheduleEntity::getScheduleId);
        auditScheduleList = qsAuditScheduleMapper.selectList(auditScheduleWrapper);
        qsActiveEntity.setQsAuditScheduleEntityList(auditScheduleList);

        // 添加内审计划
        map.put("internalAuditPlan", qsActiveEntity);


        return ResultUtil.success(map);
    }
}
