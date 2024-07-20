package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.*;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.*;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.DivideVo;
import com.lims.manage.erp.vo.InternalAuditDetailsVo;
import com.lims.manage.erp.vo.QsActiveVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
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
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private QsAuditScheduleRelService qsAuditScheduleRelService;
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private DeptDao deptDao;
    @Autowired
    private AduditBaseDataDao aduditBaseDataDao;
    @Autowired
    private DivideAuditDetailRelService divideAuditDetailRelService;
    @Autowired
    private DivideRectificationRecordDao divideRectificationRecordDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addQsActiveData(QsActiveEntity qsActiveEntity) {
        // 进行保存内审表
        // 创建时： state = “待开始”
        qsActiveEntity.setState("待开始");
        // 进行 审核周期 拆分: auditTimeCycle
        if (StringUtils.isNotEmpty(qsActiveEntity.getAuditTimeCycle())) {
            String[] times = qsActiveEntity.getAuditTimeCycle().split("~");
            // "2024-07-16" 转 Date 格式
            qsActiveEntity.setStartTime(DateUtil.timeFormat(times[0]));
            qsActiveEntity.setEndTime(DateUtil.timeFormat(times[1]));
        }
        // 评审分工的 新增-效验
        if (CollectionUtil.isNotEmpty(qsActiveEntity.getDivideList())) {
            Set<String> deptSet = new HashSet<>();
            for (DivideVo divideVo : qsActiveEntity.getDivideList()) {
                deptSet.add(divideVo.getDeptId());
            }
            if (deptSet.size() != qsActiveEntity.getDivideList().size()) {
                return ResultUtil.error("操作失败，请检查 评审分工");
            }
        }
        this.baseMapper.insert(qsActiveEntity);
        Integer activeId = qsActiveEntity.getActiveId();
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
                    divideEntity1.setDeptId(divideVo.getDeptId());
                    divideEntity1.setDeptName(divideVo.getDeptName());
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
                // 进行 周期 拆分: scheduleDateCycle
                if (StringUtils.isNotEmpty(qsAuditScheduleEntity.getScheduleDateCycle())) {
                    String[] times = qsAuditScheduleEntity.getScheduleDateCycle().split("~");
                    // "2024-07-16 14:50" 转 Date 格式
                    qsAuditScheduleEntity.setStartTime(DateUtil.timeMinuteFormat(times[0]));
                    qsAuditScheduleEntity.setEndTime(DateUtil.timeMinuteFormat(times[1]));
                }
                qsAuditScheduleMapper.insert(qsAuditScheduleEntity);
            }
        }

        // 钉钉发送消息
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        try {
            methodForEachNotice(userInfo.getName(), qsActiveEntity, qsActiveEntity.getAuditTeamList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 钉钉发送消息-部门负责人通知
        try {
            methodnotifyDepartmentHead(userInfo.getName(), qsActiveEntity, qsActiveEntity.getDivideList());
        } catch (Exception e) {
            e.printStackTrace();
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
        // 进行 审核周期 拆分: auditTimeCycle
        if (StringUtils.isNotEmpty(qsActiveEntity.getAuditTimeCycle())) {
            String[] times = qsActiveEntity.getAuditTimeCycle().split("~");
            // "2024-07-16" 转 Date 格式
            qsActiveEntity.setStartTime(DateUtil.timeFormat(times[0]));
            qsActiveEntity.setEndTime(DateUtil.timeFormat(times[1]));
        }
        // 评审分工的 新增-效验
        if (CollectionUtil.isNotEmpty(qsActiveEntity.getDivideList())) {
            Set<String> deptSet = new HashSet<>();
            for (DivideVo divideVo : qsActiveEntity.getDivideList()) {
                deptSet.add(divideVo.getDeptId());
            }
            if (deptSet.size() != qsActiveEntity.getDivideList().size()) {
                return ResultUtil.error("操作失败，请检查 评审分工");
            }
        }
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

        // 钉钉发送消息
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        try {
            methodForEachNotice(userInfo.getName(), qsActiveEntity, qsActiveEntity.getAuditTeamList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 钉钉发送消息-部门负责人通知
        try {
            methodnotifyDepartmentHead(userInfo.getName(), qsActiveEntity, qsActiveEntity.getDivideList());
        } catch (Exception e) {
            e.printStackTrace();
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
            return ResultUtil.success(new QsActiveEntity());
        }

        // 获取内审计划详情：
        // 进行 审核周期 拆分: auditTimeCycle
        if (qsActiveEntity.getStartTime() != null && qsActiveEntity.getEndTime() != null) {
            // Date 转 "2024-07-16" 格式
            String startTime = DateUtil.formatDate(qsActiveEntity.getStartTime());
            String endTime = DateUtil.formatDate(qsActiveEntity.getEndTime());
            qsActiveEntity.setAuditTimeCycle(startTime + "~" + endTime);
        }

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
        if (CollectionUtil.isNotEmpty(auditScheduleList)) {
            for (QsAuditScheduleEntity qsAuditScheduleEntity : auditScheduleList) {
                if (qsAuditScheduleEntity.getStartTime() != null && qsAuditScheduleEntity.getEndTime() != null) {
                    // Date 转 "2024-07-16 14:50" 格式
                    String startTime = DateUtil.formatMinuteDate(qsAuditScheduleEntity.getStartTime());
                    String endTime = DateUtil.formatMinuteDate(qsAuditScheduleEntity.getEndTime());
                    qsAuditScheduleEntity.setScheduleDateCycle(startTime + "~" + endTime);
                }
            }
        }
        qsActiveEntity.setQsAuditScheduleEntityList(auditScheduleList);

        return ResultUtil.success(qsActiveEntity);
    }

    @Override
    public Result getInternalAuditBasics() {
        // 查询基础表 进行 返回数据 呈现
        QsActiveEntity qsActiveEntity = new QsActiveEntity();

        // 查询基础信息
        List<TestInitDataEntity> list = taskMapper.selectEntrustBasis(40);
        qsActiveEntity.setPurpose(list.get(0).getRemark());
        qsActiveEntity.setNature(list.get(1).getRemark());
        qsActiveEntity.setRangeText(list.get(2).getRemark());
        qsActiveEntity.setBasis(list.get(3).getRemark());
        qsActiveEntity.setPoints(list.get(4).getRemark());
        return ResultUtil.success(qsActiveEntity);
    }

    /**
     * 开始进行内审计划
     *
     * @param qsActiveVo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result startInternalAuditPlan(QsActiveVo qsActiveVo) {
        // 通过 内审id 和 状态类型 进行效验
        QsActiveEntity qsActiveEntity = this.baseMapper.selectById(qsActiveVo.getActiveId());
        if (qsActiveEntity == null) {
            return ResultUtil.error("操作失败： 内审不存在");
        }
        Integer type = Integer.parseInt(qsActiveVo.getType());
        switch (type) {
            case 1:
                // 开始执行
                if (!qsActiveEntity.getState().equals("待开始")) {
                    return ResultUtil.error("操作失败： " + qsActiveEntity.getName() + " 状态为 " + qsActiveEntity.getState());
                }
                // 更改状态为 "首次会议"
                qsActiveEntity.setState("首次会议");
                this.baseMapper.updateById(qsActiveEntity);
                return ResultUtil.success("操作成功");
            case 2:
                // 内审检查
                if (!qsActiveEntity.getState().equals("内审检查")) {
                    return ResultUtil.error("操作失败： " + qsActiveEntity.getName() + " 状态为 " + qsActiveEntity.getState());
                }
                if (StringUtils.isNotEmpty(qsActiveVo.getHastenWork())) {
                    // 钉钉发送消息

                    SysUserEntity userInfo = ShiroUtils.getUserInfo();
                    try {
                        // 内审检查： 催办
                        methodHastenWorkMeeting(userInfo.getName(), "内审检查", qsActiveEntity);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return ResultUtil.success("操作成功");
                }
                if (StringUtils.isNotEmpty(qsActiveVo.getOperationComplete())) {
                    // 内审检查：操作完成  更改状态为 "末次会议"
                    qsActiveEntity.setState("末次会议");
                    this.baseMapper.updateById(qsActiveEntity);
                    return ResultUtil.success("操作成功");
                }
            case 3:
                // 完成整改
                if (!qsActiveEntity.getState().equals("问题整改")) {
                    return ResultUtil.error("操作失败： " + qsActiveEntity.getName() + " 状态为 " + qsActiveEntity.getState());
                }
                if (StringUtils.isNotEmpty(qsActiveVo.getHastenWork())) {
                    // 钉钉发送消息

                    SysUserEntity userInfo = ShiroUtils.getUserInfo();
                    try {
                        // 内审检查： 催办
                        methodHastenWorkMeeting(userInfo.getName(), "问题整改", qsActiveEntity);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return ResultUtil.success("操作成功");
                }
                if (StringUtils.isNotEmpty(qsActiveVo.getOperationComplete())) {
                    // 完成整改：操作完成  更改状态为 "待总结"
                    qsActiveEntity.setState("待总结");
                    this.baseMapper.updateById(qsActiveEntity);
                    return ResultUtil.success("操作成功");
                }
        }

        return ResultUtil.success("操作失败");
    }

    /**
     * 发起会议：首次会议、末次会议
     *
     * @param qsAuditScheduleRelEntity
     * @param file
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result initiateAMeeting(QsAuditScheduleRelEntity qsAuditScheduleRelEntity, MultipartFile[] file) {

        // 效验 内审id的对应的 首次会议、末次会议 是否存在？存在就抛出异常：为空 继续执行
        QsActiveEntity qsActiveEntity = this.baseMapper.selectById(qsAuditScheduleRelEntity.getActiveId());
        if (qsActiveEntity == null) {
            return ResultUtil.error("操作失败： 内审单不存在");
        }
        // 查询 会议信息 是否存在
        LambdaQueryWrapper<QsAuditScheduleRelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QsAuditScheduleRelEntity::getActiveId, qsAuditScheduleRelEntity.getActiveId());
        // 判断 数据 为空 = 首次会议
        List<QsAuditScheduleRelEntity> list = qsAuditScheduleRelService.list(queryWrapper);
        if (CollectionUtil.isEmpty(list)) {
            qsAuditScheduleRelEntity.setMeetingType("首次会议");
            if (!qsActiveEntity.getState().equals("首次会议")) {
                // 内审单 = 首次会议 状态不一致 则抛出异常
                return ResultUtil.error("操作失败： 内审单状态为" + qsActiveEntity.getState());
            }

            // 完成 首次会议后：
            qsActiveEntity.setState("内审检查");
        } else {
            qsAuditScheduleRelEntity.setMeetingType("末次会议");

            if (!qsActiveEntity.getState().equals("末次会议")) {
                // 内审单 = 末次会议 状态不一致 则抛出异常
                return ResultUtil.error("操作失败： 内审单状态为" + qsActiveEntity.getState());
            }
            // 完成 末次会议后：state =  "完成整改" or state =  "待总结"
            qsActiveEntity.setState("问题整改");
        }
        queryWrapper.eq(QsAuditScheduleRelEntity::getMeetingType, qsAuditScheduleRelEntity.getMeetingType());
        List<QsAuditScheduleRelEntity> list2 = qsAuditScheduleRelService.list(queryWrapper);
        if (CollectionUtil.isNotEmpty(list2)) {
            return ResultUtil.error("操作失败：" + qsAuditScheduleRelEntity.getMeetingType() + "已存在");
        }

        // 处理附件 信息 多个 使用 逗号截取
        StringBuffer stringBuilder = new StringBuffer();
        if (file != null && file.length > 0) {
            for (MultipartFile multipartFile : file) {
                Long fileCode = GenID.getID();
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");

                String upload = MinIoUtil.upload(BucketsConst.internal_audit, multipartFile, fileCode + "." + strings[strings.length - 1]);
                if (!org.springframework.util.StringUtils.isEmpty(upload)) {
                    String[] fileUrls = upload.split("\\?");
                    stringBuilder.append(fileUrls[0]);
                    stringBuilder.append(",");
                }
            }
            qsAuditScheduleRelEntity.setUrl(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
        }

        // 组员信息 进行遍历截取
        if (CollectionUtil.isNotEmpty(qsAuditScheduleRelEntity.getAuditTeamList())) {
            // 组员信息
            StringBuffer groupMembersBuffer = new StringBuffer();
            for (AuditTeamNumber auditTeamNumber : qsAuditScheduleRelEntity.getAuditTeamList()) {
                groupMembersBuffer.append(auditTeamNumber.getUserId() + "&" + auditTeamNumber.getName() + ",");
            }
            qsAuditScheduleRelEntity.setAttendance(groupMembersBuffer.deleteCharAt(groupMembersBuffer.length() - 1).toString());
        }

        // 进行 会议周期 拆分: auditTimeCycle
        if (StringUtils.isNotEmpty(qsAuditScheduleRelEntity.getMeetingCycle())) {
            String[] times = qsAuditScheduleRelEntity.getMeetingCycle().split("~");
            // "2024-07-16" 转 Date 格式
            qsAuditScheduleRelEntity.setStartTime(DateUtil.timeFormat(times[0]));
            qsAuditScheduleRelEntity.setEndTime(DateUtil.timeFormat(times[1]));
        }

        // 执行新增
        qsAuditScheduleRelService.save(qsAuditScheduleRelEntity);

        // 更改内审 状态：
        this.baseMapper.updateById(qsActiveEntity);
        // 钉钉发送消息
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        try {
            methodStartMeeting(userInfo.getName(), qsActiveEntity, qsAuditScheduleRelEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResultUtil.success("操作成功");
    }

    @Override
    public Result submitInternalAuditDocument(QsAuditScheduleRelEntity qsAuditScheduleRelEntity, MultipartFile[] file) {

        // 效验 内审id的对应的 首次会议、末次会议 是否存在？存在就抛出异常：为空 继续执行
        QsActiveEntity qsActiveEntity = this.baseMapper.selectById(qsAuditScheduleRelEntity.getActiveId());
        if (qsActiveEntity == null) {
            return ResultUtil.error("操作失败： 内审单不存在");
        }
        if (!qsActiveEntity.getState().equals("待总结")) {
            return ResultUtil.error("操作失败：" + qsActiveEntity.getName() + " 状态为 " + qsActiveEntity.getState());
        }

        // 处理附件 信息 多个 使用 逗号截取
        StringBuffer stringBuilder = new StringBuffer();
        if (file != null && file.length > 0) {
            for (MultipartFile multipartFile : file) {
                Long fileCode = GenID.getID();
                String name = multipartFile.getOriginalFilename();
                String[] strings = name.split("\\.");

                String upload = MinIoUtil.upload(BucketsConst.internal_audit, multipartFile, fileCode + "." + strings[strings.length - 1]);
                if (!org.springframework.util.StringUtils.isEmpty(upload)) {
                    String[] fileUrls = upload.split("\\?");
                    stringBuilder.append(fileUrls[0]);
                    stringBuilder.append(",");
                }
            }
            qsActiveEntity.setUrl(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
            qsActiveEntity.setState("完成");
            this.baseMapper.updateById(qsActiveEntity);
            return ResultUtil.success("操作成功");
        }

        return ResultUtil.error("附件不能为空");
    }

    @Override
    public List<String> getDiviDeStates(int activeId, int divideId) {
        return this.baseMapper.getDiviDeStates(activeId,divideId);
    }

    /**
     * @param activeId
     * @param type     = 1 内审检查 根据内审ID 展示 详情 、 type = 2 问题整改详情（展示整改详情，不展示 检查记录）
     * @return
     */
    @Override
    public Result getInternalAuditInspectionDetails(String activeId, Integer type) {
        // 判断内审id 是否存在：
        LambdaQueryWrapper<QsActiveEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QsActiveEntity::getActiveId, activeId);
        queryWrapper.last("LIMIT 1");
        QsActiveEntity qsActiveEntity = this.baseMapper.selectOne(queryWrapper);
        if (qsActiveEntity == null) {
            // 不存在
            return ResultUtil.error("查询失败，内审单不存在");
        }

        // 获取分工id 集合
        LambdaQueryWrapper<DivideEntity> divideWrapper = new LambdaQueryWrapper<>();
        divideWrapper.eq(DivideEntity::getActiveId, activeId);
        List<DivideEntity> list = divideDao.selectList(divideWrapper);

        // 进行整合 组员信息
        if (CollectionUtil.isNotEmpty(list)) {

            // 进行分组展示 key = deptId value = 分工集合
            Map<String, List<DivideEntity>> map = new HashMap<>();
            for (DivideEntity divideEntity : list) {
                // 进行分组
                if (map.get(divideEntity.getDeptId()) == null) {
                    List<DivideEntity> divideEntities = new ArrayList<>();
                    divideEntities.add(divideEntity);
                    map.put(divideEntity.getDeptId(), divideEntities);
                } else {
                    List<DivideEntity> divideEntities = map.get(divideEntity.getDeptId());
                    divideEntities.add(divideEntity);
                    map.put(divideEntity.getDeptId(), divideEntities);
                }
            }

            // 待返回数据集合
            List<InternalAuditDetailsVo> dataSet = new ArrayList<>();

            // 部门id 集合
            List<Long> deptIds = new ArrayList<>();

            /**
             * 分工 id集合
             */
            List<Integer> divideIds = new ArrayList<>();

            // 处理map 数据
            for (String key : map.keySet()) {
                // 分组下的数据集合
                List<DivideEntity> divideEntities = map.get(key);

                // 数据集
                InternalAuditDetailsVo auditDetailsVo = new InternalAuditDetailsVo();
                // 内审员集合
                StringBuffer auditorNameBuffer = new StringBuffer();
                for (DivideEntity divideEntity : divideEntities) {
                    // 获取内审员 名称顺序： 无序
                    auditorNameBuffer.append(divideEntity.getAuditorName() + ",");
                }
                // 内审员名字 多个使用逗号拼接
                auditDetailsVo.setAuditorName(auditorNameBuffer.deleteCharAt(auditorNameBuffer.length() - 1).toString());
                // 对应的 分组ID
                auditDetailsVo.setDivideId(divideEntities.get(0).getDivideId());
                divideIds.add(divideEntities.get(0).getDivideId());

                // 对应的部门ID
                auditDetailsVo.setDeptId(divideEntities.get(0).getDeptId());
                // 对应部门名称
                auditDetailsVo.setDeptName(divideEntities.get(0).getDeptName());

                // 部门id 集合
                deptIds.add(Long.valueOf(divideEntities.get(0).getDeptId()));
                // 内审检查 详情
                if (type == 1) {
                    // 检查记录集合
                    List<AduditBaseData> aduditBaseDataList = aduditBaseDataDao.selectmergingList(divideEntities.get(0).getDivideId());
                    auditDetailsVo.setAduditBaseDataList(aduditBaseDataList);
                }

                dataSet.add(auditDetailsVo);
            }

            // 通过部门id集合 获取 负责人名字
            LambdaQueryWrapper<DingDeptEntity> deptWrapper = new LambdaQueryWrapper<>();
            deptWrapper.in(DingDeptEntity::getId, deptIds);
            List<DingDeptEntity> deptList = deptDao.selectList(deptWrapper);
            for (InternalAuditDetailsVo internalAuditDetailsVo : dataSet) {
                // 比较 deptId 获取 部门负责人名字
                for (DingDeptEntity deptEntity : deptList) {
                    if (internalAuditDetailsVo.getDeptId().equals(deptEntity.getId().toString())) {
                        internalAuditDetailsVo.setUserName(deptEntity.getUserName());
                    }
                }
            }

            // 根据分工id集合 进行整理符合项信息集合
            LambdaQueryWrapper<DivideAuditDetailRel> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.in(DivideAuditDetailRel::getDivideId, divideIds);
            List<DivideAuditDetailRel> divideAuditDetailRels = divideAuditDetailRelService.list(queryWrapper1);
            for (InternalAuditDetailsVo internalAuditDetailsVo : dataSet) {
                for (DivideAuditDetailRel divideAuditDetailRel : divideAuditDetailRels) {

                    // 进行赋值
                    if (divideAuditDetailRel.getDivideId() == divideAuditDetailRel.getDivideId()) {
                        // 不符合项
                        internalAuditDetailsVo.setNonConformance(divideAuditDetailRel.getNonConformance());
                        // 不符合程度
                        internalAuditDetailsVo.setNonComplianceDegree(divideAuditDetailRel.getNonComplianceDegree());
                        // 检查结果
                        internalAuditDetailsVo.setCheckResult(divideAuditDetailRel.getCheckResult());
                        // 不符合程序
                        internalAuditDetailsVo.setNonConformanceProgram(divideAuditDetailRel.getNonConformanceProgram());
                        // 不符合标准
                        internalAuditDetailsVo.setSubstandard(divideAuditDetailRel.getSubstandard());
                        // 状态
                        internalAuditDetailsVo.setState(divideAuditDetailRel.getState());
                        // 操作时间
                        if (divideAuditDetailRel.getCheckDate() != null) {
                            // Date 转 "2024-07-16" 格式
                            String startTime = DateUtil.formatDate(divideAuditDetailRel.getCheckDate());
                            internalAuditDetailsVo.setOperatingTime(startTime);
                        }
                    }
                }
            }

            // 问题纠正日期展示
            if (type == 2) {
                // 根据分组id 集合
                LambdaQueryWrapper<DivideRectificationRecord> divideRectificationRecordWrapper = new LambdaQueryWrapper<>();
                divideRectificationRecordWrapper.in(DivideRectificationRecord::getDivideId, divideIds);
                List<DivideRectificationRecord> divideRectificationRecords = divideRectificationRecordDao.selectList(divideRectificationRecordWrapper);
                // 补充 问题反馈信息
                for (InternalAuditDetailsVo internalAuditDetailsVo : dataSet) {
                    if (CollectionUtil.isEmpty(divideAuditDetailRels)) {
                        internalAuditDetailsVo.setDivideRectificationRecord(new DivideRectificationRecord());
                        // 整改对象 = null 状态 = 0
                        internalAuditDetailsVo.setProblemRectificationState("0");
                    }
                    for (DivideRectificationRecord divideRectificationRecord : divideRectificationRecords) {
                        // 补充进 问题纠正中
                        if (internalAuditDetailsVo.getDivideId() == divideRectificationRecord.getDivideId()) {

                            // Date 转 string
                            if (divideRectificationRecord.getReceivedDate() != null) {
                                // Date 转 "2024-07-16" 格式
                                String time1 = DateUtil.formatDate(divideRectificationRecord.getReceivedDate());
                                divideRectificationRecord.setReceivedTime(time1);
                            }

                            if (divideRectificationRecord.getVerificationDate() != null) {
                                // Date 转 "2024-07-16" 格式
                                String time2 = DateUtil.formatDate(divideRectificationRecord.getVerificationDate());
                                divideRectificationRecord.setVerificationTime(time2);
                            }

                            if (divideRectificationRecord.getRequiredCompletionDate() != null) {
                                // Date 转 "2024-07-16" 格式
                                String time3 = DateUtil.formatDate(divideRectificationRecord.getRequiredCompletionDate());
                                divideRectificationRecord.setRequiredCompletionTime(time3);
                            }

                            if (divideRectificationRecord.getActualFinishingDate() != null) {
                                // Date 转 "2024-07-16" 格式
                                String time4 = DateUtil.formatDate(divideRectificationRecord.getActualFinishingDate());
                                divideRectificationRecord.setActualFinishingTime(time4);
                            }

                            internalAuditDetailsVo.setDivideRectificationRecord(divideRectificationRecord);
                            // 设置优先级
                            if (divideRectificationRecord.getState().equals("整改通知")) {
                                internalAuditDetailsVo.setProblemRectificationState("0");
                            }
                            if (divideRectificationRecord.getState().equals("等待纠正")) {
                                internalAuditDetailsVo.setProblemRectificationState("1");
                            }
                            if (divideRectificationRecord.getState().equals("等待验证")) {
                                internalAuditDetailsVo.setProblemRectificationState("3");
                            }
                            if (divideRectificationRecord.getState().equals("已完成")) {
                                internalAuditDetailsVo.setProblemRectificationState("2");
                            }
                        } else {
                            internalAuditDetailsVo.setDivideRectificationRecord(new DivideRectificationRecord());
                            // 整改对象 = null 状态 = 0
                            internalAuditDetailsVo.setProblemRectificationState("0");
                        }
                    }
                }
            }

            return ResultUtil.success(dataSet);
        }

        return ResultUtil.success(new InternalAuditDetailsVo());
    }

    /**
     * 创建评审:部门负责人 调用方法循环 通知信息
     */
    void methodnotifyDepartmentHead(String userName, QsActiveEntity qsActiveEntity, List<DivideVo> divideList) throws Exception {
        // 进行钉钉发布消息操作
        DingNotifyUtils dingNotifyUtils = new DingNotifyUtils();

        // 组员信息列表
        if (CollectionUtil.isNotEmpty(divideList)) {
            for (DivideVo divideVo : divideList) {

                // 获取部门负责人的 id
                LambdaQueryWrapper<DingDeptEntity> deptWrapper = new LambdaQueryWrapper<>();
                deptWrapper.eq(DingDeptEntity::getId, divideVo.getDeptId());
                deptWrapper.last("LIMIT 1");
                DingDeptEntity dingDeptEntity = deptDao.selectOne(deptWrapper);

                // 获取 任务单下检测人信息 userId
                LambdaQueryWrapper<SysUserEntity> queryWrapper3 = new LambdaQueryWrapper<>();
                queryWrapper3.eq(SysUserEntity::getUserId, dingDeptEntity.getUserId());
                queryWrapper3.last("LIMIT 1");
                SysUserEntity userDetails3 = sysUserDao.selectOne(queryWrapper3);
                // 钉钉id
                String dingId3 = userDetails3.getDingUserId();
                StringBuffer crewBuffer = new StringBuffer();
                crewBuffer.append("受审部门为：" + dingDeptEntity.getName());
                crewBuffer.append("内审名称为： " + qsActiveEntity.getName() + " 请及时操作");
                dingNotifyUtils.OAWorkNotice(dingId3, crewBuffer.toString(), userName, null);
            }
        }
    }

    /**
     * 创建评审:调用方法循环 通知信息
     */
    void methodForEachNotice(String userName, QsActiveEntity qsActiveEntity, List<AuditTeamNumber> auditTeamList) throws Exception {
        // 进行钉钉发布消息操作
        DingNotifyUtils dingNotifyUtils = new DingNotifyUtils();

        // 审核组长
        StringBuffer titleBuffer = new StringBuffer();
        titleBuffer.append("内审活动中指派您：" + qsActiveEntity.getGroupLeaderName() + "为" + "审核组长 ");
        titleBuffer.append("内审名称为： " + qsActiveEntity.getName() + " 请及时操作");

        // 获取 任务单下检测人信息 userId
        LambdaQueryWrapper<SysUserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserEntity::getUserId, qsActiveEntity.getGroupLeaderId());
        SysUserEntity userDetails = sysUserDao.selectOne(queryWrapper);
        // 钉钉id
        String dingId = userDetails.getDingUserId();
        dingNotifyUtils.OAWorkNotice(dingId, titleBuffer.toString(), userName, null);

        // 编制人
        StringBuffer editorBuffer = new StringBuffer();
        editorBuffer.append("内审活动中指派您：" + qsActiveEntity.getEditorName() + "为" + "编制人 ");
        editorBuffer.append("内审名称为： " + qsActiveEntity.getName() + " 请及时操作");
        // 获取 任务单下检测人信息 userId
        LambdaQueryWrapper<SysUserEntity> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(SysUserEntity::getUserId, qsActiveEntity.getGroupLeaderId());
        SysUserEntity userDetails2 = sysUserDao.selectOne(queryWrapper2);
        // 钉钉id
        String dingId2 = userDetails2.getDingUserId();
        dingNotifyUtils.OAWorkNotice(dingId2, titleBuffer.toString(), userName, null);

        // 组员信息列表
        if (CollectionUtil.isNotEmpty(auditTeamList)) {
            for (AuditTeamNumber auditTeamNumber : auditTeamList) {
                // 获取 任务单下检测人信息 userId
                LambdaQueryWrapper<SysUserEntity> queryWrapper3 = new LambdaQueryWrapper<>();
                queryWrapper3.eq(SysUserEntity::getUserId, auditTeamNumber.getUserId());
                SysUserEntity userDetails3 = sysUserDao.selectOne(queryWrapper3);
                // 钉钉id
                String dingId3 = userDetails3.getDingUserId();
                StringBuffer crewBuffer = new StringBuffer();
                crewBuffer.append("内审活动中指派您：" + auditTeamNumber.getName() + "为" + "组员 ");
                crewBuffer.append("内审名称为： " + qsActiveEntity.getName() + " 请及时操作");
                dingNotifyUtils.OAWorkNotice(dingId3, crewBuffer.toString(), userName, null);
            }
        }
    }

    /**
     * 发起会议：首次会议、末次会议:调用方法循环 通知信息
     */
    void methodStartMeeting(String userName, QsActiveEntity qsActiveEntity, QsAuditScheduleRelEntity qsAuditScheduleRelEntity) throws Exception {
        // 进行钉钉发布消息操作
        DingNotifyUtils dingNotifyUtils = new DingNotifyUtils();

        // 主持人
        StringBuffer titleBuffer = new StringBuffer();
        titleBuffer.append("内审名称为： " + qsActiveEntity.getName());
        titleBuffer.append("会议纪要为： " + qsAuditScheduleRelEntity.getMeetingType() + " ");
        titleBuffer.append(qsAuditScheduleRelEntity.getHostName() + "为" + "主持人 " + " 请及时操作");

        // 获取 任务单下检测人信息 userId
        LambdaQueryWrapper<SysUserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserEntity::getUserId, qsAuditScheduleRelEntity.getHostUserId());
        SysUserEntity userDetails = sysUserDao.selectOne(queryWrapper);
        // 钉钉id
        String dingId = userDetails.getDingUserId();
        dingNotifyUtils.OAWorkNotice(dingId, titleBuffer.toString(), userName, null);

        // 记录人
        StringBuffer editorBuffer = new StringBuffer();
        editorBuffer.append("内审名称为： " + qsActiveEntity.getName());
        editorBuffer.append("会议纪要为： " + qsAuditScheduleRelEntity.getMeetingType() + " ");
        editorBuffer.append(qsAuditScheduleRelEntity.getRecorderName() + "为" + "记录人 " + " 请及时操作");
        // 获取 任务单下检测人信息 userId
        LambdaQueryWrapper<SysUserEntity> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(SysUserEntity::getUserId, qsAuditScheduleRelEntity.getRecorderUserId());
        SysUserEntity userDetails2 = sysUserDao.selectOne(queryWrapper2);
        // 钉钉id
        String dingId2 = userDetails2.getDingUserId();
        dingNotifyUtils.OAWorkNotice(dingId2, titleBuffer.toString(), userName, null);

        // 出席人信息列表
        if (StringUtils.isNotEmpty(qsAuditScheduleRelEntity.getAttendance())) {
            String[] arrays = qsAuditScheduleRelEntity.getAttendance().split(",");

            for (int i = 0; i < arrays.length; i++) {
                String[] userNames = arrays[i].split("&");

                // 获取 任务单下检测人信息 userId
                LambdaQueryWrapper<SysUserEntity> queryWrapper3 = new LambdaQueryWrapper<>();
                queryWrapper3.eq(SysUserEntity::getUserId, userNames[0]);
                SysUserEntity userDetails3 = sysUserDao.selectOne(queryWrapper3);
                // 钉钉id
                String dingId3 = userDetails3.getDingUserId();
                StringBuffer crewBuffer = new StringBuffer();
                crewBuffer.append("内审名称为： " + qsActiveEntity.getName());
                crewBuffer.append("会议纪要为： " + qsAuditScheduleRelEntity.getMeetingType() + " ");
                crewBuffer.append(userNames[1] + "为" + "出席人 " + " 请及时操作");
                dingNotifyUtils.OAWorkNotice(dingId3, crewBuffer.toString(), userName, null);
            }
        }
    }

    /**
     * 根据内审id 进行催办:调用方法循环 通知信息
     */
    void methodHastenWorkMeeting(String userName, String type, QsActiveEntity qsActiveEntity) throws Exception {
        // 进行钉钉发布消息操作
        DingNotifyUtils dingNotifyUtils = new DingNotifyUtils();

        // 通过内审id 获取 对应的部门id 及 负责人 进行催办

        //      2、评审分工：
        LambdaQueryWrapper<DivideEntity> divideWrapper = new LambdaQueryWrapper<>();
        divideWrapper.eq(DivideEntity::getActiveId, qsActiveEntity.getActiveId());
        List<DivideEntity> divideList = divideDao.selectList(divideWrapper);

        for (DivideEntity divideEntity : divideList) {
            // 通过部门id集合 获取 负责人名字
            LambdaQueryWrapper<DingDeptEntity> deptWrapper = new LambdaQueryWrapper<>();
            deptWrapper.in(DingDeptEntity::getId, divideEntity.getDeptId());
            deptWrapper.last("limit 1");
            DingDeptEntity dingDeptEntity = deptDao.selectOne(deptWrapper);
            // 获取 任务单下检测人信息 userId
            LambdaQueryWrapper<SysUserEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUserEntity::getUserId, dingDeptEntity.getUserId());
            SysUserEntity userDetails = sysUserDao.selectOne(queryWrapper);
            // 钉钉id
            String dingId = userDetails.getDingUserId();

            // 发送内容
            StringBuffer titleBuffer = new StringBuffer();
            titleBuffer.append("内审名称为： " + qsActiveEntity.getName());
            titleBuffer.append("在 " + type + "中 部门负责人： " + userDetails.getName() + "  请尽快完成检查");
            String time = DateUtil.formatMinuteDate(new Date());
            titleBuffer.append("催办时间为 " + time);

            dingNotifyUtils.OAWorkNotice(dingId, titleBuffer.toString(), userName, null);
        }
    }
}
