package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.*;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.service.TestTaskPoolService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 任务单 服务实现类
 * </p>
 *
 * @author dlc
 * @since 2023-10-08
 */
@Service
public class TestTaskPoolServiceImpl extends ServiceImpl<TestTaskPoolMapper, TestTaskPool> implements TestTaskPoolService {

    @Autowired
    private TestTaskPoolMapper taskPoolMapper;
    @Autowired
    private SampleEntityMapper sampleEntityMapper;
    @Autowired
    private TestCheckItemsTaskRelMapper testCheckItemsTaskRelMapper;
    @Autowired
    private TeamMapper teamMapper;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private EntrustEntityMapper entityMapper;
    @Autowired
    private LogManagerService logManagerService;
    @Autowired
    private EntrustServiceImpl entrustService;
    @Autowired
    private SysRoleDao sysRoleDao;
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private TestCheckItemTeamRelDao testCheckItemTeamRelDao;
    @Autowired
    private TestDetectionDao testDetectionDao;
    @Autowired
    private TaskService taskService;
    @Resource
    private TestTaskOrderWorkingHoursMapper testTaskOrderWorkingHoursMapper;

    /**
     * 任务大厅 展示详情数据
     *
     * @param poolId
     * @param entrustId
     * @return
     */
    @Override
    public Result taskHallDetailsDisplay(Long poolId, Long entrustId) {
        // 进行构造数据 返回。
        // 1、 展示模拟任务单号
        LambdaQueryWrapper<TestTaskPool> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestTaskPool::getId, poolId);
        queryWrapper.eq(TestTaskPool::getEntrustmentId, entrustId);
        // 任务单模拟单据
        TestTaskPool detailedData = taskPoolMapper.selectOne(queryWrapper);
        if (detailedData == null) {
            return ResultUtil.error("查看失败： 预任务单不存在");
        }
        // 通过委托单id 查询任务列表
        List<TaskProgressVo> taskProgressVos = taskMapper.getTaskStateByEntrustId(entrustId);
        // 2、 展示每组下样品列表
        List<SampleEntity> sampleList = sampleEntityMapper.selectSampleListGroup(entrustId);
        // 新增样品信息下检测项
        List<SampleEntity> addNewSamples = new ArrayList<>();
        // 3、 查看检测项及所属类型：
        //      （0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生）
        // 3.1：通过委托单id 查看检测项列表。
        List<SampleItemEntity> itemList = taskPoolMapper.selectItems(entrustId, null);
        // 3.2：通过委托单id 查看检测项下指派人员信息。
        LambdaQueryWrapper<TestCheckItemsTaskRel> taskRelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        taskRelLambdaQueryWrapper.eq(TestCheckItemsTaskRel::getEntrustId, entrustId);
        List<TestCheckItemsTaskRel> itemsTaskRels = testCheckItemsTaskRelMapper.selectList(taskRelLambdaQueryWrapper);
        // 进行数据的 集成展示。
        if (CollectionUtil.isNotEmpty(sampleList)) {
            // 遍历检测项与对应指派人员的信息
            if (CollectionUtil.isNotEmpty(itemList)) {
                for (SampleItemEntity sampleItemEntity : itemList) {
                    List<TestCheckItemsTaskRel> itemsTaskRels1 = new ArrayList<>();
                    // 检测人员信息展示
                    if (CollectionUtil.isNotEmpty(itemsTaskRels)) {
                        for (TestCheckItemsTaskRel testCheckItemsTaskRel : itemsTaskRels) {
                            // 进行每组检测项 与 检测项指派人员的集成。
                            if (testCheckItemsTaskRel.getItemId().equals(sampleItemEntity.getId())) {
                                //TDODDO: 假如： 检测项重复使用 需要通过任务单进行区分。暂时未过滤
                                itemsTaskRels1.add(testCheckItemsTaskRel);
                            }
                        }
                    }
                    if (CollectionUtil.isNotEmpty(itemsTaskRels1)) {
                        // 调用方法 遍历：
                        TaskListToolSortUtils.methodForEachTaskHallDetails(sampleItemEntity, itemsTaskRels1);
                    }
                    // 每组检测项中 补充样品描述说明
                    if (CollectionUtil.isNotEmpty(taskProgressVos)) {
                        if (sampleItemEntity.getTaskId() != null) {
                            for (TaskProgressVo taskProgressVo : taskProgressVos) {
                                if (sampleItemEntity.getTaskId().equals(taskProgressVo.getTaskId())) {
                                    sampleItemEntity.setSampleStateDescription(
                                            StringUtils.isEmpty(taskProgressVo.getSampleStateDescription()) ?
                                                    "-" : taskProgressVo.getSampleStateDescription());
                                }
                            }
                        }
                    }
                }
            }
            for (SampleEntity sampleEntity : sampleList) {
                List<SampleItemEntity> sampleItemEntities = new ArrayList<>();
                // 新增检测项中 taskId = null的。
                List<SampleItemEntity> addNewSampleItemEntities = new ArrayList<>();
                // 遍历检测项数据 存放至 样品中
                if (CollectionUtil.isNotEmpty(itemList)) {
                    for (SampleItemEntity sampleItemEntity : itemList) {
                        if (sampleItemEntity.getSampleId().equals(sampleEntity.getId())) {
                            // 全部可选 可分组
                            sampleItemEntity.setPriority("1");
                            sampleItemEntity.setCheckBox("1");
                            sampleItemEntities.add(sampleItemEntity);
                            if (CollectionUtil.isNotEmpty(taskProgressVos) && sampleItemEntity.getTaskId() == null) {
                                addNewSampleItemEntities.add(sampleItemEntity);
                            }
                        }
                    }
                }
                sampleEntity.setSampleCheckItem(sampleItemEntities);
                // 样品外观描述 不为null
                sampleEntity.setOutwardDescribe(sampleEntity.getOutwardDescribe() == null ? "-" : sampleEntity.getOutwardDescribe());
                SampleEntity sampleData = new SampleEntity();
                sampleData.setId(sampleEntity.getId());
                sampleData.setSampleCode(sampleEntity.getSampleCode());
                sampleData.setSampleName(sampleEntity.getSampleName());
                sampleData.setAliasName(sampleEntity.getAliasName());
                sampleData.setSpecs(sampleEntity.getSpecs());
                sampleData.setBatchNumber(sampleEntity.getBatchNumber());
                sampleData.setManufacturer(sampleEntity.getManufacturer());
                sampleData.setSampleOrigin(sampleEntity.getSampleOrigin());
                sampleData.setOutward(sampleEntity.getOutward());
                sampleData.setOutwardDescribe(sampleEntity.getOutwardDescribe());
                sampleData.setSampleCheckItem(addNewSampleItemEntities);
                addNewSamples.add(sampleData);
            }
        }
        JSONObject jsonObject = new JSONObject();
        // 流水号任务单信息
        jsonObject.put("testTaskPool", detailedData);
        // 样品信息
        jsonObject.put("samples", sampleList);
        // addNewSamples 下 无检测项信息时 则 为空
        Integer times = 0;
        for (SampleEntity sampleEntity : addNewSamples) {
            if (CollectionUtil.isEmpty(sampleEntity.getSampleCheckItem())) {
                times = times + 1;
            }
        }
        if (addNewSamples.size() == times) {
            // 新增样品信息下检测项
            jsonObject.put("addNewSamples", Collections.emptyList());
        } else {
            // 新增样品信息下检测项
            jsonObject.put("addNewSamples", addNewSamples);
        }
        // 领样人
        if (CollectionUtil.isNotEmpty(taskProgressVos)) {
            jsonObject.put("sampler", taskProgressVos.get(0).getSampler());
        } else {
            jsonObject.put("sampler", null);
        }
        return ResultUtil.success(jsonObject);
    }


    /**
     * 新增样品与未分配样品处理
     *
     * @param itemList      : 检测项列表
     * @param teamId        ： 当前登录人部门id
     * @param sampleList    ： 委托单id下的样品集合（返回信息）
     * @param addNewSamples ： 新增样品信息下检测项（返回信息）
     * @param taskId        ： 当前登录人的任务单id 有可能为空
     */
    private void newSamplesAndUnallocatedSamplesAreProcessed(List<SampleItemEntity> itemList,
                                                             Long teamId, List<SampleEntity> sampleList, List<SampleEntity> addNewSamples, Long taskId) {
        // 进行检测项 与样品信息 归类:
        if (CollectionUtil.isNotEmpty(itemList) && CollectionUtil.isNotEmpty(sampleList)) {
            // 新增检测项中 taskId = null的。
            List<SampleItemEntity> addNewSampleItemEntities = new ArrayList<>();
            for (SampleEntity sampleEntity : sampleList) {
                // 当前样品属于配合比原材时： 配合比实验交叉参数默认分配给已拥有配合比实验的团队
                Boolean falg = false;
                if (sampleEntity.getPid() != null) {

                    // 通过样品父级pid 查询 返回团队id
                    falg = verifyMixRatioSampleInformation(sampleEntity.getPid(), teamId);
                    for (SampleItemEntity sampleItemEntity : itemList) {
                        if (falg) {
                            sampleItemEntity.setPriority("1");
                        } else {
                            // 所有检测项不可领取：需要配合比试验团队领取后才行
                            sampleItemEntity.setPriority(null);
                            sampleItemEntity.setTeamName("专属团队领取");
                        }
                    }
                }
                List<SampleItemEntity> sampleItemEntities = new ArrayList<>();
                // 遍历检测项数据 存放至 样品中
                if (CollectionUtil.isNotEmpty(itemList) && taskId != null) {

                    // 当前任务id 不为空 并且检测项 选中：获取 对应的检测管理
                    SampleItemEntity sampleItemData = new SampleItemEntity();
                    for (SampleItemEntity sampleItemEntity : itemList) {
                        if (sampleItemEntity.getPriority() != null && sampleItemEntity.getInspector() != null) {
                            sampleItemData = sampleItemEntity;
                        }
                    }

                    Iterator<SampleItemEntity> it = itemList.iterator();
                    while (it.hasNext()) {
                        SampleItemEntity sampleItemEntity = it.next();
                        if ((sampleItemEntity.getPriority() == null)) {
                            addNewSampleItemEntities.add(sampleItemEntity);
                            it.remove();
                        } else if (sampleItemEntity.getSampleId().equals(sampleEntity.getId())) {
                            sampleItemEntity.setTaskId(taskId);
                            // 进行赋值
                            if (sampleItemEntity.getInspector() == null) {
                                // 检测人
                                sampleItemEntity.setInspector(sampleItemData.getInspector());
                                // 记录人
                                sampleItemEntity.setRecorder(sampleItemData.getRecorder());
                                // 复核人
                                sampleItemEntity.setReviewer(sampleItemData.getReviewer());
                                // 报告制作人
                                sampleItemEntity.setReportProducer(sampleItemData.getReportProducer());
                                // 辅助人员
                                sampleItemEntity.setAuxiliaryPersonnel(sampleItemData.getAuxiliaryPersonnel() != null ? sampleItemData.getAuxiliaryPersonnel() : null);
                                //
                                sampleItemEntity.setProbationer(sampleItemData.getProbationer() != null ? sampleItemData.getProbationer() : null);
                                //
                                sampleItemEntity.setInterns(sampleItemData.getInterns() != null ? sampleItemData.getInterns() : null);
                            }
                            sampleItemEntities.add(sampleItemEntity);
                        }
                    }
                } else if (CollectionUtil.isNotEmpty(itemList) && taskId == null) {
                    for (SampleItemEntity sampleItemEntity : itemList) {
                        if (sampleItemEntity.getSampleId().equals(sampleEntity.getId())) {
                            sampleItemEntities.add(sampleItemEntity);
                        }
                    }
                }
                sampleEntity.setSampleCheckItem(sampleItemEntities);
                // 样品外观描述 不为null
                sampleEntity.setOutwardDescribe(sampleEntity.getOutwardDescribe() == null ? "-" : sampleEntity.getOutwardDescribe());
                SampleEntity sampleData = new SampleEntity();
                sampleData.setId(sampleEntity.getId());
                sampleData.setSampleCode(sampleEntity.getSampleCode());
                sampleData.setSampleName(sampleEntity.getSampleName());
                sampleData.setAliasName(sampleEntity.getAliasName());
                sampleData.setSpecs(sampleEntity.getSpecs());
                sampleData.setBatchNumber(sampleEntity.getBatchNumber());
                sampleData.setManufacturer(sampleEntity.getManufacturer());
                sampleData.setSampleOrigin(sampleEntity.getSampleOrigin());
                sampleData.setOutward(sampleEntity.getOutward());
                sampleData.setOutwardDescribe(sampleEntity.getOutwardDescribe());

                List<SampleItemEntity> addNewItems = addNewSampleItemEntities.stream().
                        filter(SampleItemEntity -> SampleItemEntity.getSampleId().equals(sampleData.getId())).collect(Collectors.toList());
                if (!addNewItems.isEmpty()) {
                    sampleData.setSampleCheckItem(addNewItems);
                }

                if (taskId != null) {
                    addNewSamples.add(sampleData);
                }
            }
        }
    }

    /**
     * 任务大厅 - 根据登录人、返回所属团队成员的对应检测项。
     *
     * @param poolId
     * @param entrustId
     * @param userInfo  当前登录人与所属团队
     * @return
     */
    @Override
    public Result getTaskDetectionItemDetails(Long poolId, Long entrustId, SysUserEntity userInfo) {
        // 进行构造数据 返回。
        // 1、 展示模拟任务单号
        LambdaQueryWrapper<TestTaskPool> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestTaskPool::getId, poolId);
        queryWrapper.eq(TestTaskPool::getEntrustmentId, entrustId);
        // 任务单模拟单据
        TestTaskPool detailedData = taskPoolMapper.selectOne(queryWrapper);
        if (detailedData == null) {
            return ResultUtil.error("查看失败： 预任务单不存在");
        }

        // 2、 展示每组下样品列表
        List<SampleEntity> sampleList = sampleEntityMapper.selectSampleListGroup(entrustId);
        // 委托单id下的产品id集合
        List<Integer> productIds = new ArrayList<>();
        productIds = sampleList.stream().filter(SampleEntity -> SampleEntity.getProductId() != null).map(SampleEntity::getProductId).collect(Collectors.toList());
        LambdaQueryWrapper<TestCheckItemTeamRel> checkItemTeamRelWrapper = new LambdaQueryWrapper<>();
        checkItemTeamRelWrapper.in(TestCheckItemTeamRel::getProductId, productIds);
        // 产品中存在的团队与检测项关系
        List<TestCheckItemTeamRel> teamRelList = testCheckItemTeamRelDao.selectList(checkItemTeamRelWrapper);

        // 获取当前登录人
        Long teamId = userInfo.getTechnicistId().longValue();

        // 3： 通过委托单id 查看检测项列表。
        List<SampleItemEntity> itemList = taskPoolMapper.selectItems(entrustId, null);

        // 新增样品信息下检测项
        List<SampleEntity> addNewSamples = new ArrayList<>();


        JSONObject jsonObject = new JSONObject();

        // 领样人 : 通过委托单ID 获取任务列表
        Long taskId = null;
        List<TaskVo> taskList = taskMapper.selectTaskCreateTimeList(entrustId, teamId.intValue());
        if (CollectionUtil.isNotEmpty(taskList)) {
            jsonObject.put("sampler", taskList.get(0).getSampler());
            taskId = taskList.get(0).getId();
        } else {
            jsonObject.put("sampler", null);
        }

        List<TestCheckItemsTaskRel> itemsTaskRels = new ArrayList<>();

        if (taskId != null) {

            // 3.2：通过任务单id 查看检测项下指派人员信息。
            LambdaQueryWrapper<TestCheckItemsTaskRel> taskRelLambdaQueryWrapper = new LambdaQueryWrapper<>();
            taskRelLambdaQueryWrapper.eq(TestCheckItemsTaskRel::getTaskId, taskId);
            itemsTaskRels = testCheckItemsTaskRelMapper.selectList(taskRelLambdaQueryWrapper);

        }

        // 遍历当前检测项 与团队 优先级关系：
        if (CollectionUtil.isNotEmpty(itemList) && CollectionUtil.isNotEmpty(teamRelList)) {
            // 检测项集合 与 检测项团队 优先级处理
            TaskListToolSortUtils.itemAndTeamProcessing(itemList, teamRelList, teamId);
        }

        if (taskId != null) {

            // 3.2：通过任务单id 查看检测项下指派人员信息。
            TaskListToolSortUtils.checkItemPersonnelInformation(itemList, teamId, itemsTaskRels);
        }

        // 当前所属团队未领取任务单时
        if (taskId == null) {
            // 委托单下 检测项列表 遍历
            for (SampleItemEntity sampleItemEntity : itemList) {
                // 检测项已领取，不是当前团队 则标记为空
                if (sampleItemEntity.getTaskId() != null && !sampleItemEntity.getTechnicistId().equals(teamId)) {
                    sampleItemEntity.setPriority(null);
                    sampleItemEntity.setTeamName("其他团队已领取");
                }
            }
        }

        // 新增样品与未分配样品处理
        newSamplesAndUnallocatedSamplesAreProcessed(itemList, teamId, sampleList, addNewSamples, taskId);

        // addNewSamples 下 无检测项信息时 则 为空
        Integer times = 0;
        for (SampleEntity sampleEntity : addNewSamples) {
            if (CollectionUtil.isEmpty(sampleEntity.getSampleCheckItem())) {
                times = times + 1;
                sampleEntity.setSampleCheckItem(new ArrayList<>());
            }
        }
        if (addNewSamples.size() == times) {
            // 新增样品信息 返回空集合
            jsonObject.put("addNewSamples", Collections.emptyList());
        } else {
            // 新增样品信息下检测项
            jsonObject.put("addNewSamples", addNewSamples);
        }
        // 流水号任务单信息
        jsonObject.put("testTaskPool", detailedData);
        // 样品信息
        jsonObject.put("samples", sampleList);

        return ResultUtil.success(jsonObject);
    }

    /**
     * 验证配合比样品信息
     *
     * @param pid
     * @param teamId
     */
    public Boolean verifyMixRatioSampleInformation(Integer pid, Long teamId) {

        List<SampleItemInstrumentEntity> itemList = sampleEntityMapper.selectSampleTestInformation(pid);
        if (CollectionUtil.isNotEmpty(itemList)) {
            // 通过当前 teamId 获取 deptIds
            List<Integer> oldTeamIds = testCheckItemTeamRelDao.selectTeamIdRel(teamId.intValue());
            for (SampleItemInstrumentEntity sampleItemInstrumentEntity : itemList) {
                if (sampleItemInstrumentEntity.getDeptId() != null) {
                    // 检测项所属id 与 当前部门id 匹配的话
                    if (sampleItemInstrumentEntity.getDeptId().equals(teamId.intValue())) {
                        return true;
                    }
                }
                if (CollectionUtil.isNotEmpty(oldTeamIds)) {
                    for (Integer oldTeamId : oldTeamIds) {
                        if (sampleItemInstrumentEntity.getDeptId().equals(oldTeamId)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    /**
     * 任务大厅 领取任务单
     *
     * @param list
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Result addTaskCollection(List<SampleItemEntity> list, Long entrustId, SysUserEntity userInfo, Long teamId) {
        // 当前登录人 不能是 复核人
        for (SampleItemEntity sampleItemEntity : list) {
            // 一致的话。进行返回数据 "郭家林&1647502446459100,邓喜旺&1647657004269101"
            String[] arrays = sampleItemEntity.getReviewer().split(",");
            for (int i = 0; i < arrays.length; i++) {
                String[] userIds = arrays[i].split("&");
                if (userIds[0].equals(userInfo.getUserId())) {
                    return ResultUtil.error("操作失败！ 当前登录人不能是 复核人");
                }
            }
        }
        // 通过委托单id 查询旧任务列表
        List<TaskProgressVo> oldTaskProgressVos = taskMapper.getTaskStateByEntrustId(entrustId);
        // 通过委托单 获取流水任务单详情
        LambdaQueryWrapper<TestTaskPool> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestTaskPool::getEntrustmentId, entrustId);
        TestTaskPool testTaskPool = taskPoolMapper.selectOne(queryWrapper);
        if (testTaskPool == null) {
            return ResultUtil.error("领取失败： 当前流水号任务单不存在");
        }

        // 领取任务单： 1、合并任务单 2、拆分任务单
        // 判断旧任务单列表 == null：领取
        if (CollectionUtils.isEmpty(oldTaskProgressVos)) {
            // 领取任务单
            return createTaskList(list, entrustId, teamId, testTaskPool, oldTaskProgressVos, userInfo);
        } else {
            // 修改任务单： 直接修改即可
            return updateTaskList(oldTaskProgressVos, list, userInfo, entrustId, testTaskPool);
        }
    }

    /**
     * 我的任务 修改任务单
     *
     * @param list
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Result updateTaskCollection(List<SampleItemEntity> list, Long entrustId, SysUserEntity userInfo) {
        // 当前登录人 不能是 复核人
        for (SampleItemEntity sampleItemEntity : list) {
            // 一致的话。进行返回数据 "郭家林&1647502446459100,邓喜旺&1647657004269101"
            String[] arrays = sampleItemEntity.getReviewer().split(",");
            for (int i = 0; i < arrays.length; i++) {
                String[] userIds = arrays[i].split("&");
                if (userIds[0].equals(userInfo.getUserId())) {
                    return ResultUtil.error("操作失败！ 当前登录人不能是 复核人");
                }
            }
        }
        // 通过委托单id 查询旧任务列表
        List<TaskProgressVo> oldTaskProgressVos = taskMapper.getTaskStateByEntrustId(entrustId);
        // 通过委托单 获取流水任务单详情
        LambdaQueryWrapper<TestTaskPool> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestTaskPool::getEntrustmentId, entrustId);
        TestTaskPool testTaskPool = taskPoolMapper.selectOne(queryWrapper);
        if (testTaskPool == null) {
            return ResultUtil.error("领取失败： 当前流水号任务单不存在");
        }
        // 修改任务单： 直接修改即可-不需要再新增任务单信息
        return updateTaskList1(oldTaskProgressVos, list, userInfo, entrustId, testTaskPool);

    }


    /**
     * 任务大厅or修改时验证任务单真伪
     * 1、通过通过检测项主键 获取 委托单是否存在
     * 2、查询当前领单人 是否为 授权角色id = 66
     *
     * @param list   任务大厅传递数据
     * @param userId 登录人id
     * @return 抛出error 直接抛出，success 传递 entrustId
     */
    @Override
    public Result verifyTheTaskListStatus(List<SampleItemEntity> list, Long userId) {
        // 通过检测项主键 获取 委托单id
        Long entrustId = taskPoolMapper.selectEntrustmentId(list.get(0).getItemIds().get(0));
        if (entrustId == null) {
            return ResultUtil.error("数据异常、委托单不存在");
        }
        // 查询当前登录人 是否为 授权角色
        List<SysRoleEntity> roleList = sysRoleDao.selectSysRoleByUserId(userId);
        if (CollectionUtils.isEmpty(roleList)) {
            return ResultUtil.error("领取失败：登录人无角色");
        }
        Boolean flag = false;
        for (SysRoleEntity sysRoleEntity : roleList) {
            if (sysRoleEntity.getRoleId() == 66) {
                flag = true;
            }
        }
        if (!flag) {
            return ResultUtil.error("领取失败：登录人无授权签字人角色");
        }
        return ResultUtil.success(entrustId);
    }

    /**
     * 任务单领取
     *
     * @param list
     * @param entrustId
     * @param userInfo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Result addNewTicket(List<SampleItemEntity> list, Long entrustId, SysUserEntity userInfo, Long teamId) {
        // 通过委托单 获取流水任务单详情
        LambdaQueryWrapper<TestTaskPool> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestTaskPool::getEntrustmentId, entrustId);
        TestTaskPool testTaskPool = taskPoolMapper.selectOne(queryWrapper);
        if (testTaskPool == null) {
            return ResultUtil.error("领取失败： 当前流水号任务单不存在");
        }

        // 领取时：检测项所属部门需要为空
        List<SampleItemInstrumentEntity> itemList = testDetectionDao.getTestEntrustedSampleCheckitemRelDetailList(list.get(0).getItemIds());
        if (CollectionUtil.isEmpty(itemList)) {
            return ResultUtil.error("领取失败，当前检测项不存在");
        }
        for (SampleItemInstrumentEntity sampleItemInstrumentEntity : itemList) {
            if (sampleItemInstrumentEntity.getDeptId() != null) {
                return ResultUtil.error("领取失败，当前检测项 " + sampleItemInstrumentEntity.getCheckItemName() + " 已经被 " + sampleItemInstrumentEntity.getDeptId() + "团队 领取 请重新领取！");
            }
        }


        // TODO: 2024年1月3日  任务生成规则根据签发人所属团队走   = 授权操作人 = 领取人。
        //    规则：人员跨部门可以选。 （新的任务单号 生成时、只有一个单号。）
        newAddTask(list, entrustId, teamId, testTaskPool);


        // 所有检测项 全部分配完毕 则 流转时间 更新
        List<String> checkNames = taskPoolMapper.selectCheckitemRelAboutTaskList(entrustId);
        if (CollectionUtil.isEmpty(checkNames)) {
            testTaskPool.setReceiveDate(new Date());
        }

        // 调用方法 进行 更新流水号任务单信息
        methodUpdateTaskPool(entrustId, testTaskPool);
        return ResultUtil.success("领取成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Result updateNewTicket(List<SampleItemEntity> list, Long entrustId, SysUserEntity userInfo, Long teamId) {
        // 通过委托单 获取流水任务单详情
        LambdaQueryWrapper<TestTaskPool> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestTaskPool::getEntrustmentId, entrustId);
        TestTaskPool testTaskPool = taskPoolMapper.selectOne(queryWrapper);
        if (testTaskPool == null) {
            return ResultUtil.error("修改失败： 当前流水号任务单不存在");
        }

        // 领取时：检测项所属部门需要为空
        List<SampleItemInstrumentEntity> itemList = testDetectionDao.getTestEntrustedSampleCheckitemRelDetailList(list.get(0).getItemIds());
        if (CollectionUtil.isEmpty(itemList)) {
            return ResultUtil.error("修改失败，当前检测项不存在");
        }
        for (SampleItemInstrumentEntity sampleItemInstrumentEntity : itemList) {
            if (sampleItemInstrumentEntity.getDeptId() != null && !sampleItemInstrumentEntity.getDeptId().equals(teamId.intValue())) {
                return ResultUtil.error("修改失败，当前检测项 " + sampleItemInstrumentEntity.getCheckItemName() + " 已经被 " + sampleItemInstrumentEntity.getDeptId() + "团队 领取 请重新领取！");
            }
        }
        //TODO： 执行我的任务 修改即可
        // 更新 领取任务单
        TaskProgressVo taskProgressVo = new TaskProgressVo();
        // 通过委托单id 和部门id 获取任务单id 及领样人、部门信息
        List<TaskVo> taskList = taskMapper.selectTaskCreateTimeList(entrustId, teamId.intValue());
        TaskVo taskVo = taskList.get(0);
        taskProgressVo.setTaskId(taskVo.getId());
        taskProgressVo.setDeptId(taskVo.getDeptId().intValue());
        taskProgressVo.setSampler(taskVo.getSampler());

        newUpdateTask(list, entrustId, taskProgressVo);
        // 调用方法 进行 更新流水号任务单信息
        methodUpdateTaskPool(entrustId, testTaskPool);
        return ResultUtil.success("更新成功");
    }

    /**
     * 验证领取人对应科室信息
     * <p>
     * userId 用户id
     *
     * @return
     */
    public Result verifyClaimBaseConditions(Long userId) {
        // 领取人的科室id
        List<Long> userList = new ArrayList<>();
        userList.add(userId);
        List<Long> teamIds = teamMapper.getUsersByTechnicist(userList);
        if (CollectionUtil.isEmpty(teamIds)) {
            // 抛出 领取人 需要包含科室
            return ResultUtil.error("领取人 需要存在科室");
        }
        if (teamIds.size() >= 2) {
            // 抛出 领取人 生成单号 指向存在问题。
            return ResultUtil.error("领取人 科室存在多个");
        }
        return ResultUtil.success(teamIds);
    }

    /**
     * 领取任务单
     *
     * @param list               检测数据
     * @param entrustId          委托单ID
     * @param teamId             部门id
     * @param testTaskPool       流水任务单详情
     * @param oldTaskProgressVos 旧任务单集合
     * @param userInfo           用户信息
     * @return 返回操作信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Result createTaskList(List<SampleItemEntity> list, Long entrustId, Long teamId, TestTaskPool testTaskPool, List<TaskProgressVo> oldTaskProgressVos, SysUserEntity userInfo) {
        // TODO: 2024年6月6日： 审核发布时 指向合并或者是拆分。
        // 判定 true = 新任务单，false = 旧任务单 null
        Boolean taskListStatus = false;
        if (testTaskPool.getTaskListStatus() != null && testTaskPool.getTaskListStatus().equals("1")) {
            taskListStatus = false;
        } else {
            taskListStatus = true;
        }
        if (taskListStatus) {
            // TODO: 2024年1月3日  任务生成规则根据签发人所属团队走   = 授权操作人 = 领取人。
            //    规则：人员跨部门可以选。 （新的任务单号 生成时、只有一个单号。）
            newAddTask(list, entrustId, teamId, testTaskPool);
            // 调用方法 进行 更新流水号任务单信息
            testTaskPool.setReceiveDate(new Date());
            methodUpdateTaskPool(entrustId, testTaskPool);
        } else if (!taskListStatus && list.size() == 1) {
            newAddTask(list, entrustId, teamId, testTaskPool);
            // 调用方法 进行 更新流水号任务单信息
            testTaskPool.setReceiveDate(new Date());
            methodUpdateTaskPool(entrustId, testTaskPool);
        } else {
            // TODO： 执行 旧操作 下列操作 全为旧操作
            // 通过委托单id 查看检测项列表。
            List<SampleItemEntity> itemList = taskPoolMapper.selectItems(entrustId, null);
            // 任务领取-循环验证 数据 msg = null 正常执行、！= null 抛出。
            Result msg = loopValidation(list);
            if (msg != null) {
                return msg;
            }
            // 获取每组检测项 对应的 （0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生）
            // 根据科室id 及 委托单主键 查询任务单是否存在？
            //                        // TODO: 2023年12月21日 1、 旧任务单存在 更新任务单 更新检测项 ，2、任务单与前端绑定不一致， 任务单更新为废弃操作、产值 = 0 。
            //                           TODO:          2、 任务单不存在的话，创建任务单即可。
            //调用方法： 补充检测项信息并发布任务单
            // key = deptId value = 旧单号
            Map<Long, TaskProgressVo> taskCodeMap = new HashMap<>();
            // 2、进行录入任务单信息
            if (CollectionUtil.isNotEmpty(list)) {
                for (SampleItemEntity sampleItemEntity : list) {
                    String sampler = sampleItemEntity.getSampler().split("-")[0];
                    methodPublishTaskList(entrustId, sampler, sampleItemEntity, itemList, testTaskPool.getId().longValue(), taskCodeMap);
                }
            }
            // 调用方法 进行 更新流水号任务单信息
            methodUpdateTaskPool(entrustId, testTaskPool);
        }
        return ResultUtil.success("领取成功");
    }

    /**
     * 更新任务单
     *
     * @param oldTaskProgressVos 任务单列表
     * @param list               传递信息
     * @param userInfo           用户信息
     * @param entrustId          委托单id
     * @param testTaskPool       流水号任务单
     * @return 返回信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Result updateTaskList(List<TaskProgressVo> oldTaskProgressVos, List<SampleItemEntity> list, SysUserEntity userInfo, Long entrustId, TestTaskPool testTaskPool) {
        // 遍历
        for (TaskProgressVo taskProgressVo : oldTaskProgressVos) {
            if (taskProgressVo.getReceiver() != null && !taskProgressVo.getReceiver().equals(userInfo.getUserId().toString())) {
                return ResultUtil.error("领取失败： 领单人与任务单存在的领单人不符合");
            }
        }
        if (oldTaskProgressVos.size() == 1) {
            // TODO： 判断任务单状态 task_list_status = 任务单状态：!=null 任务生成规则根据签发人所属团队走
            if (oldTaskProgressVos.get(0).getTaskListStatus() != null) {
                //TODO： 执行我的任务 修改即可
                // 更新 领取任务单
                newUpdateTask(list, entrustId, oldTaskProgressVos.get(0));
                // 调用方法 进行 更新流水号任务单信息
                methodUpdateTaskPool(entrustId, testTaskPool);
                return ResultUtil.success("更新成功");
            }
        } else {
            // TODO： 执行 旧操作 下列操作 全为旧操作
            // 通过委托单id 查看检测项列表。
            List<SampleItemEntity> itemList = taskPoolMapper.selectItems(entrustId, null);
            // 任务领取-循环验证 数据 msg = null 正常执行、！= null 抛出。
            Result msg = loopValidation(list);
            if (msg != null) {
                return msg;
            }
            // 获取每组检测项 对应的 （0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生）
            // 根据科室id 及 委托单主键 查询任务单是否存在？
            //                        // TODO: 2023年12月21日 1、 旧任务单存在 更新任务单 更新检测项 ，2、任务单与前端绑定不一致， 任务单更新为废弃操作、产值 = 0 。
            //                           TODO:          2、 任务单不存在的话，创建任务单即可。
            //调用方法： 补充检测项信息并发布任务单
            // key = deptId value = 旧单号
            Map<Long, TaskProgressVo> taskCodeMap = new HashMap<>();
            // 1.1：任务单列表存在的话
            if (CollectionUtil.isNotEmpty(oldTaskProgressVos)) {
                // 处理旧任务单与检测项关系
                processingOldTaskList(list, oldTaskProgressVos, itemList, entrustId);
                // 针对 list 含有 taskId的 进行集合循环
                Iterator<SampleItemEntity> it = list.iterator();
                while (it.hasNext()) {
                    SampleItemEntity data = it.next();
                    if (data.getTaskId() != null) {
                        it.remove();
                    }
                }
            }
            // 2、进行录入任务单信息
            if (CollectionUtil.isNotEmpty(list)) {
                for (SampleItemEntity sampleItemEntity : list) {
                    String sampler = sampleItemEntity.getSampler().split("-")[0];
                    methodPublishTaskList(entrustId, sampler, sampleItemEntity, itemList, testTaskPool.getId().longValue(), taskCodeMap);
                }
            }
            // 调用方法 进行 更新流水号任务单信息
            methodUpdateTaskPool(entrustId, testTaskPool);
        }
        return ResultUtil.success("操作成功");
    }


    /**
     * 我的任务 更新任务单信息
     *
     * @param oldTaskProgressVos 任务单列表
     * @param list               传递信息
     * @param userInfo           用户信息
     * @param entrustId          委托单id
     * @param testTaskPool       流水号任务单
     * @return 返回信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Result updateTaskList1(List<TaskProgressVo> oldTaskProgressVos, List<SampleItemEntity> list, SysUserEntity userInfo, Long entrustId, TestTaskPool testTaskPool) {
        // 遍历
        for (TaskProgressVo taskProgressVo : oldTaskProgressVos) {
            if (taskProgressVo.getReceiver() != null && !taskProgressVo.getReceiver().equals(userInfo.getUserId().toString())) {
                return ResultUtil.error("领取失败： 领单人与任务单存在的领单人不符合");
            }
        }

        if (oldTaskProgressVos.size() == 1) {
            // TODO： 判断任务单状态 task_list_status = 任务单状态：!=null 任务生成规则根据签发人所属团队走
            // 更新 领取任务单
            newUpdateTask(list, entrustId, oldTaskProgressVos.get(0));
            // 调用方法 进行 更新流水号任务单信息
            methodUpdateTaskPool(entrustId, testTaskPool);
            return ResultUtil.success("更新成功");
        }
        // 通过委托单id 查看检测项列表。
        List<SampleItemEntity> itemList = taskPoolMapper.selectItems(entrustId, null);

        EntrustAddVo entrustAddVo = entityMapper.selectByKeyId(entrustId);
        // 进行遍历输出
        for (SampleItemEntity sampleItemEntity : list) {
            // 通过报告制作人 确认科室
            List<Long> userIds = new ArrayList<>();
            String[] names = sampleItemEntity.getReportProducer().split(",");
            for (int i = 0; i < names.length; i++) {
                userIds.add(Long.valueOf(names[i].split("&")[1]));
            }

            // userIds 获取所属科室及之前的科室
            List<Long> oldTeams = teamMapper.getOldTeamIds(userIds);

            // 获取 taskId 及所属部门
            TaskProgressVo taskVo = new TaskProgressVo();
            for (Long oldTeamId : oldTeams) {
                for (TaskProgressVo taskProgressVo : oldTaskProgressVos) {
                    if (oldTeamId.equals(taskProgressVo.getDeptId().longValue())) {
                        taskVo.setTaskId(taskProgressVo.getTaskId());
                        taskVo.setDeptId(taskProgressVo.getDeptId());
                    }
                }
            }
            if (taskVo != null) {
                List<SampleItemEntity> updateItemList = new ArrayList<>();
                for (SampleItemEntity sampleItemEntity1 : itemList) {
                    for (Integer itemId : sampleItemEntity.getItemIds()) {
                        if (itemId.equals(sampleItemEntity1.getId())) {
                            updateItemList.add(sampleItemEntity1);
                        }
                    }
                }

                // 根据 taskId 和检测项集合 条件删除流转信息
                LambdaQueryWrapper<TestCheckItemsTaskRel> queryWrapper12 = new LambdaQueryWrapper<>();
                queryWrapper12.in(TestCheckItemsTaskRel::getItemId, sampleItemEntity.getItemIds());
                queryWrapper12.or().eq(TestCheckItemsTaskRel::getTaskId, taskVo.getTaskId());
                testCheckItemsTaskRelMapper.delete(queryWrapper12);

                // 更新数据
                updateTask1(sampleItemEntity, taskVo, updateItemList, entrustAddVo);
                //更新检测项分配的部门和任务单号
                List<CheckItemDeptVo> checkItemDeptVoList1 = Lists.newArrayList();
                for (SampleItemEntity sampleItemEntity1 : updateItemList) {

                    CheckItemDeptVo checkItemDeptVo = new CheckItemDeptVo();
                    checkItemDeptVo.setId(sampleItemEntity1.getId());
                    checkItemDeptVo.setDeptId(taskVo.getDeptId().longValue());
                    checkItemDeptVo.setTaskId(taskVo.getTaskId());
                    checkItemDeptVoList1.add(checkItemDeptVo);

                }
                //更新检测项信息
                taskMapper.batchUpdateCheckItem(checkItemDeptVoList1);
            }
        }
        // 调用方法 进行 更新流水号任务单信息
        methodUpdateTaskPool(entrustId, testTaskPool);
        return ResultUtil.success("操作成功");
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateTask1(SampleItemEntity sampleItemEntity, TaskProgressVo taskProgressVo, List<SampleItemEntity> itemList, EntrustAddVo entrustAddVo) {

        long taskId = taskProgressVo.getTaskId();
        long teamId = taskProgressVo.getDeptId();
        // 领样人
        String sampler = "";
        // map信息处理 ： key = （ userType） 、value = （set去重后的人员信息）
        HashMap<Integer, Set<TestCheckItemsTaskRel>> itemsTaskMap = new HashMap<>();
        // 补充每组检测项中人员信息
        // 获取每组检测项 对应的 （0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生）
        List<LabelValueVo> inspectorArraysVos = new ArrayList<>();
        List<TestCheckItemsTaskRel> inspectorRels = new ArrayList<>();
        // 调用方法： 针对记录人、复核人、报告制作人信息 方法读取人员信息
        newMethodForPersonnel(sampleItemEntity, inspectorArraysVos, inspectorRels);
        sampleItemEntity.setItemsTaskRels(inspectorRels);
        // 新增检测项对应的操作人员信息。
        newMethodPublishTaskList(entrustAddVo.getId(), sampleItemEntity, itemList, taskId, itemsTaskMap);
        if (StringUtils.isNotEmpty(sampleItemEntity.getSampler())) {
            sampler = sampleItemEntity.getSampler().split("-")[0];
        }
        // 补充任务单信息
        SampleItemEntity addSampleItemEntity = new SampleItemEntity();
        for (Integer key : itemsTaskMap.keySet()) {
            //  0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生
            switch (key) {
                case 0:
                    //  0：检测人
                    addSampleItemEntity.setInspector(methodReturnString(itemsTaskMap, key, 0));
                    break;
                case 1:
                    // 1：记录人、
                    addSampleItemEntity.setRecorder(methodReturnString(itemsTaskMap, key, 0));
                    break;
                case 2:
                    // 2：复核人、
                    addSampleItemEntity.setReviewer(methodReturnString(itemsTaskMap, key, 0));
                    break;
                case 3:
                    // 3：报告制作人、
                    addSampleItemEntity.setReportProducer(methodReturnString(itemsTaskMap, key, 0));
                    break;
                case 4:
                    // 4：辅助人员、
                    addSampleItemEntity.setAuxiliaryPersonnel(methodReturnString(itemsTaskMap, key, 0));
                    break;
                case 5:
                    // 5、见习生：
                    addSampleItemEntity.setProbationer(methodReturnString(itemsTaskMap, key, 0));
                    break;
                case 6:
                    // 6、实习生：
                    addSampleItemEntity.setInterns(methodReturnString(itemsTaskMap, key, 0));
                    break;
                default:
                    break;
            }
        }
        // 领样人
        addSampleItemEntity.setSampler(sampler);
        // 任务单id
        addSampleItemEntity.setTaskId(taskId);
        // 科室id
        addSampleItemEntity.setTechnicistId(teamId);
        // 委托单id
        addSampleItemEntity.setEntrustId(entrustAddVo.getId());

        // 进行新建任务单信息、补充信息
        TaskVo entity = new TaskVo();
        // 委托单信息
        entity.setEntrustmentId(entrustAddVo.getId());
        // 设置折扣
        entity.setDiscount(Double.parseDouble(entrustAddVo.getDiscount()));
        //计算本单价格
        double taskPrice = 0L;
        for (SampleItemEntity vo : itemList) {
            taskPrice = taskPrice + ((entity.getDiscount() == null ? 0 : entity.getDiscount()) *
                    (vo.getUnitPrice() == null ? 0 : vo.getUnitPrice()) * vo.getTimes());
        }
        // 更新taskId 价格
        TaskTestEntity taskTestEntity = new TaskTestEntity();
        taskTestEntity.setId(taskId);
        taskTestEntity.setTaskPrice(taskPrice);
        taskTestEntity.setSampler(taskProgressVo.getSampler());
        // 补充人员信息 检测人
        taskTestEntity.setInspector(methodRetrunStrSplit(addSampleItemEntity.getInspector(), ","));
        // 记录人
        taskTestEntity.setRecorder(methodRetrunStrSplit(addSampleItemEntity.getRecorder(), ","));
        // 复核人
        taskTestEntity.setReviewer(methodRetrunStrSplit(addSampleItemEntity.getReviewer(), ","));
        // 报告制作人
        taskTestEntity.setReportProducer(methodRetrunStrSplit(addSampleItemEntity.getReportProducer(), ","));
        // 辅助人员
        taskTestEntity.setAuxiliaryPersonnel(methodRetrunStrSplit(addSampleItemEntity.getAuxiliaryPersonnel(), ","));
        // 见习生：实习的新手
        taskTestEntity.setProbationer(methodRetrunStrSplit(addSampleItemEntity.getProbationer(), ","));
        // 实习生
        taskTestEntity.setInterns(methodRetrunStrSplit(addSampleItemEntity.getInterns(), ","));
        // 样品描述信息
        taskTestEntity.setSampleStateDescription(sampleItemEntity.getSampleStateDescription());
        taskMapper.updateTaskEntity(taskTestEntity);

    }


    /**
     * TODO: 12月21日 1、 旧任务单存在 更新任务单 更新检测项 ，2、任务单与前端绑定不一致， 删除任务单操作。
     *
     * @param list            任务大厅 检测项数据
     * @param taskProgressVos old 旧任务单列表
     * @param itemList        委托单下检测项
     * @param entrustId       委托单id
     */
    @Transactional(rollbackFor = Exception.class)
    public void processingOldTaskList(List<SampleItemEntity> list, List<TaskProgressVo> taskProgressVos, List<SampleItemEntity> itemList,
                                      Long entrustId) {
        EntrustAddVo entrustAddVo = entityMapper.selectByKeyId(entrustId);
        // 根据 entrustId 条件删除流转信息
        LambdaQueryWrapper<TestCheckItemsTaskRel> queryWrapper12 = new LambdaQueryWrapper<>();
        queryWrapper12.eq(TestCheckItemsTaskRel::getEntrustId, entrustId);
        testCheckItemsTaskRelMapper.delete(queryWrapper12);
        // 设置折扣
        Double discount = null;
        discount = Double.parseDouble(entrustAddVo.getDiscount());
        for (SampleItemEntity itemEntity : list) {
            //  -- 读取 deptId
            for (TaskProgressVo taskProgressVo : taskProgressVos) {
                if (taskProgressVo.getDeptId().longValue() == itemEntity.getTechnicistId()) {
                    // 任务单保留
                    taskProgressVo.setStatus(true);
                    //  新增检测项流转信息
                    itemEntity.setTaskId(taskProgressVo.getTaskId());
                    itemEntity.setEntrustId(entrustId);
                    inserCheckItemsTaskRel(itemEntity, itemList);
//                    //更新检测项分配的部门和任务单号
                    List<CheckItemDeptVo> checkItemDeptVoList1 = Lists.newArrayList();
                    for (Integer itemId : itemEntity.getItemIds()) {
                        CheckItemDeptVo itemDeptVo = new CheckItemDeptVo();
                        itemDeptVo.setId(itemId);
                        itemDeptVo.setDeptId(itemEntity.getTechnicistId());
                        itemDeptVo.setTaskId(itemEntity.getTaskId());
                        checkItemDeptVoList1.add(itemDeptVo);
                    }
//                    //更新检测项信息
                    taskMapper.batchUpdateCheckItem(checkItemDeptVoList1);
                    //计算本单价格
                    double taskPrice = 0L;
                    for (CheckItemDeptVo itemDeptVo : checkItemDeptVoList1) {
                        for (SampleItemEntity sampleItemEntity : itemList) {
                            if (sampleItemEntity.getId().equals(itemDeptVo.getId())) {
                                taskPrice = taskPrice + ((discount == null ? 0 : discount) *
                                        (sampleItemEntity.getUnitPrice() == null ? 0 : sampleItemEntity.getUnitPrice())) * sampleItemEntity.getTimes();
                            }
                        }
                    }
                    // 更新taskId 价格
                    TaskTestEntity taskTestEntity = new TaskTestEntity();
                    taskTestEntity.setId(itemEntity.getTaskId());
                    taskTestEntity.setTaskPrice(taskPrice);
                    // task == 144的话 变更任务单状态 = 1 领取
                    if (taskProgressVo.getState() == 144) {
                        taskTestEntity.setState(1);
                    }
                    // 补充人员信息 检测人
                    taskTestEntity.setInspector(itemEntity.getInspector());
                    // 记录人
                    taskTestEntity.setRecorder(itemEntity.getRecorder());
                    // 复核人
                    taskTestEntity.setReviewer(itemEntity.getReviewer());
                    // 报告制作人
                    taskTestEntity.setReportProducer(itemEntity.getReportProducer());
                    // 辅助人员
                    taskTestEntity.setAuxiliaryPersonnel(itemEntity.getAuxiliaryPersonnel());
                    // 见习生：实习的新手
                    taskTestEntity.setProbationer(itemEntity.getProbationer());
                    // 实习生
                    taskTestEntity.setInterns(itemEntity.getInterns());
                    // 领样人
                    taskTestEntity.setSampler(itemEntity.getSampler());
                    taskMapper.updateTaskEntity(taskTestEntity);
                }
            }
        }
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        String userName = userInfo.getUserId() + "&" + userInfo.getUsername();
        // TODO：12月29日 变更为：废弃任务单信息。
        for (TaskProgressVo taskProgressVo : taskProgressVos) {
            // itemEntity.getTaskId()!=null 说明任务单更新了。
            //  取 差集 list 和 taskProgressVos 都存在的保留 否则删除任务单。
            if (taskProgressVo.getStatus() == null) {
                TaskTestEntity taskTestEntity = new TaskTestEntity();
                taskTestEntity.setId(taskProgressVo.getTaskId());
                taskTestEntity.setTaskPrice(0.0);
                taskTestEntity.setState(144);
                taskMapper.updateTestTask(taskTestEntity);
            }

        }
    }

    /**
     * 任务大厅-领取任务-新增检测项与使用人的关系
     *
     * @param sampleItemEntity 待绑定的检测项与使用人
     * @param itemList         委托单下检测项
     */
    @Transactional(rollbackFor = Exception.class)
    public void inserCheckItemsTaskRel(SampleItemEntity sampleItemEntity, List<SampleItemEntity> itemList) {
        // 遍历每组检测项分配的itemId数量
        for (int i = 0; i < sampleItemEntity.getItemIds().size(); i++) {
            for (SampleItemEntity sampleItemEntity1 : itemList) {
                Integer itemId = sampleItemEntity.getItemIds().get(i);
                if (sampleItemEntity1.getId().equals(itemId)) {
                    for (TestCheckItemsTaskRel taskRel : sampleItemEntity.getItemsTaskRels()) {
                        // 检测项主键
                        taskRel.setItemId(itemId);
                        // 检测项名称
                        taskRel.setCheckItemName(sampleItemEntity1.getCheckItemName());
                        // 样品名称
                        taskRel.setSampleName(sampleItemEntity1.getSampleName());
                        // 样品编号
                        taskRel.setSampleCode(sampleItemEntity1.getSampleCode());
                        // 样品id
                        taskRel.setSampleId(sampleItemEntity1.getSampleId());
                        // 委托单主键
                        taskRel.setEntrustId(sampleItemEntity.getEntrustId());
                        // 任务单id
                        taskRel.setTaskId(sampleItemEntity.getTaskId());
                        // 调用方法： 对每组检测项的人员信息进行新增。
                        testCheckItemsTaskRelMapper.insert(taskRel);
                    }
                }
            }
        }
    }

    /**
     * 任务领取-循环验证 数据
     *
     * @param list 任务单领取数据值
     * @return = null 正常执行，否则抛出异常值。
     */
    public Result loopValidation(List<SampleItemEntity> list) {
        // 检测人集合
        List<Long> tsetUserIds = new ArrayList<>();
        // 报告制作人集合
        List<Long> reportProducerIds = new ArrayList<>();
        // 根据报告制作人： 查询 所在科室 是 交通工程 = false；
        Boolean claimFlag = true;
        // 参与效验的数据--------------------------------- ↓↓↓↓ ------------------------
        // 1、效验检测人在不在检测科室 2、检测人不能在多个科室 3、补充每组检测项中人员信息
        for (SampleItemEntity sampleItemEntity : list) {
            // 获取每组检测项 对应的 （0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生）
            List<LabelValueVo> inspectorArraysVos = new ArrayList<>();
            List<TestCheckItemsTaskRel> inspectorRels = new ArrayList<>();
            List<Long> userIds = new ArrayList<>();
            // 检测人名
            StringBuffer nameStr = new StringBuffer();
            // 调用方法： 针对记录人、复核人、报告制作人信息 方法读取人员信息
            methodForPersonnel(sampleItemEntity, inspectorArraysVos, inspectorRels);
            // 循环遍历 读取报告制作人
            // 记录每组检测项 是否在一个科室。
            // 记录每组检测项 是否在一个科室。
            if (CollectionUtil.isNotEmpty(inspectorRels)) {
                // 报告制作人信息
                List<Long> producerList = new ArrayList<>();
                //  0：检测人、1：记录人、2、复核人、3、报告制作人
                for (TestCheckItemsTaskRel testCheckItemsTaskRel : inspectorRels) {
                    if (testCheckItemsTaskRel.getUserType() == 1) {
                        userIds.add(Long.valueOf(testCheckItemsTaskRel.getUserId()));
                    }
                    if (testCheckItemsTaskRel.getUserType() == 2) {
                        userIds.add(Long.valueOf(testCheckItemsTaskRel.getUserId()));
                    }
                    if (testCheckItemsTaskRel.getUserType() == 3) {
                        userIds.add(Long.valueOf(testCheckItemsTaskRel.getUserId()));
                        // 根据报告制作人 来 确认科室信息
                        producerList.add(Long.valueOf(testCheckItemsTaskRel.getUserId()));
                        reportProducerIds.add(Long.valueOf(testCheckItemsTaskRel.getUserId()));
                    }
                }
                // 读取报告制作人 是否在同一科室。
                List<Long> detectorsCollection = teamMapper.getUsersByTechnicist(producerList);
                if (CollectionUtils.isEmpty(detectorsCollection)) {
                    return ResultUtil.error("领取失败：" + nameStr + " 报告制作人不在科室");
                }
                if (detectorsCollection.size() >= 2) {
                    return ResultUtil.error("领取失败：" + nameStr + " 报告制作人所在科室不同");
                }
                // 每组检测项根据报告制作人指定科室。
                sampleItemEntity.setTechnicistId(detectorsCollection.get(0));
                // TODO: 10月30日 制作人所在部门 先固定  查询当前用户的 科室信息
                if (detectorsCollection.get(0) == 265 || detectorsCollection.get(0) == 264) {
                    claimFlag = false;
                }
                // 读取0：检测人、1：记录人、2、复核人、3、报告制作人 是否在同一科室。
                List<Long> detectorsCollection2 = teamMapper.getUsersByTechnicist(userIds);
                if (detectorsCollection2.size() >= 2 && claimFlag == true) {
                    return ResultUtil.error("领取失败：" + nameStr + " 检测人、记录人、复核人、报告制作人 必须在同一个团队中");
                }
            }
            // 读取检测项人数据 生成对应的任务单。
            if (StringUtils.isNotEmpty(sampleItemEntity.getInspector())) {
                String[] inspectorArrays = sampleItemEntity.getInspector().split(",");
                for (int i = 0; i < inspectorArrays.length; i++) {
                    String[] names = inspectorArrays[i].split("&");
                    userIds.add(Long.parseLong(names[1]));
                    tsetUserIds.add(Long.parseLong(names[1]));
                    nameStr.append(names[0] + " ");
                    TestCheckItemsTaskRel data = new TestCheckItemsTaskRel();
                    data.setUserName(names[0]);
                    data.setUserId(names[1]);
                    data.setUserType(0);
                    inspectorRels.add(data);
                }
                // 读取检测人信息 是否在同一科室。
                List<Long> detectorsCollection = teamMapper.getUsersByTechnicist(userIds);
                if (CollectionUtils.isEmpty(detectorsCollection)) {
                    return ResultUtil.error("领取失败：" + nameStr + " 检测人不在科室");
                }
                if (detectorsCollection.size() >= 2 && claimFlag == true) {
                    return ResultUtil.error("领取失败：" + nameStr + " 检测人所在科室不同");
                }
            } else {
                return ResultUtil.error("领取失败：检测人信息不能为空");
            }
            sampleItemEntity.setItemsTaskRels(inspectorRels);
            // 设置留样人信息
            sampleItemEntity.setSampler(sampleItemEntity.getSampler().split("-")[0]);
        }
        // 进行效验：每组检测项中 报告制作人 应该不同，相同 会生成两组同样的任务单号
        List<Long> detectorsCollection0 = teamMapper.getUsersByTechnicist(reportProducerIds);
        if (detectorsCollection0.size() != list.size()) {
            return ResultUtil.error("领取失败：" + "报告制作人中： 请选择同一组人员，同一组人员必须要在同一个团队，不同组的人员，不能出现在同一个团队中");
        }
        //每组检测项中 选中的检测人 不能在同一科室。
        List<Long> detectorsCollection = teamMapper.getUsersByTechnicist(tsetUserIds);
        if (detectorsCollection.size() != list.size() && claimFlag == true) {
            return ResultUtil.error("领取失败：" + "请选择同一组人员，同一组人员必须要在同一个团队，不同组的人员，不能出现在同一个团队中");
        }
        // 参与效验的数据--------------------------------- ↑↑↑↑ ------------------------
        return null;
    }

    void methodUpdateTaskPool(Long entrustId, TestTaskPool testTaskPool) {
        // 领取成功后： 补充流水号任务单信息
        // 通过委托单id 查询任务列表
        List<TaskProgressVo> taskProgress2Vos = taskMapper.getTaskStateByEntrustId(entrustId);
        // 任务单号
        StringBuffer taskCodeBuffer = new StringBuffer();
        // 领取人id
        StringBuffer receiveIdBuffer = new StringBuffer();
        // 领取人名字
        StringBuffer receiveNameBuffer = new StringBuffer();
        List<Long> userIds = new ArrayList<>();
        //计算本单价格
        double taskPrice = 0L;
        for (TaskProgressVo taskProgressVo : taskProgress2Vos) {
            // 获取任务单号
            taskCodeBuffer.append(taskProgressVo.getTaskCode() + "&" + taskProgressVo.getTaskId() + ",");
            // 获取价格
            taskPrice += taskProgressVo.getTaskPrice();
            // 领取人id
            receiveIdBuffer.append(taskProgressVo.getReceiver() + ",");
            userIds.add(Long.valueOf(taskProgressVo.getReceiver()));
        }

        // 任务单编号
        testTaskPool.setTaskCode(taskCodeBuffer.deleteCharAt(taskCodeBuffer.length() - 1).toString());
        // 本单费用
        testTaskPool.setPrice(String.valueOf(taskPrice));
        // 领取人id
        testTaskPool.setReceiveId(receiveIdBuffer.deleteCharAt(receiveIdBuffer.length() - 1).toString());

        // 领取人（当前登陆人员）
        LambdaQueryWrapper<SysUserEntity> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.in(SysUserEntity::getUserId, userIds);
        List<SysUserEntity> userList = sysUserDao.selectList(userWrapper);
        StringBuffer userNameBuffer = new StringBuffer();
        for (SysUserEntity sysUserEntity : userList) {
            userNameBuffer.append(sysUserEntity.getName() + ",");
        }
        testTaskPool.setReceiveName(userNameBuffer.deleteCharAt(userNameBuffer.length() - 1).toString());

        LambdaQueryWrapper<TestTaskPool> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(TestTaskPool::getId, testTaskPool.getId());
        taskPoolMapper.update(testTaskPool, queryWrapper1);
    }

    /**
     * 整理每组检测项数据
     *
     * @param entrustId
     * @param sampler
     * @param sampleItemEntity
     * @param itemList
     * @param poolId
     * @param taskCodeMap      key = deptId value = 旧单号
     */
    void methodPublishTaskList(Long entrustId, String sampler, SampleItemEntity sampleItemEntity, List<SampleItemEntity> itemList, Long poolId, Map<Long, TaskProgressVo> taskCodeMap) {
        EntrustAddVo entrustAddVo = entityMapper.selectByKeyId(entrustId);
        // 进行新建任务单信息、补充信息
        TaskVo entity = new TaskVo();
        // 委托单信息
        entity.setEntrustmentId(entrustId);
        // 设置折扣
        entity.setDiscount(Double.parseDouble(entrustAddVo.getDiscount()));
        // 丁连春：任务单完成时间 以委托单下单时间为准
        entity.setRequiredCompletionTime(entrustAddVo.getRequestDate());
        // 任务单下单日期等于委托单受理日期
        entity.setOrderTime(entrustAddVo.getAcceptanceDate());
        // 任务单提供资料等于委托单提供资料
        if(!org.springframework.util.StringUtils.isEmpty(entrustAddVo.getPresentInformation())){
            entity.setPresentInformation(entrustAddVo.getPresentInformation());
        }else {
            entity.setPresentInformation("--");
        }
        // 领样人
        entity.setSampler(sampler);
        // 补充检测项信息
        List<CheckItemDeptVo> checkItemDeptVoList = new ArrayList<>();
        long taskId = GenID.getID();
        // 遍历每组检测项分配的itemId数量
        for (int i = 0; i < sampleItemEntity.getItemIds().size(); i++) {
            // itemList 是通过委托单id查看检测项详情列表
            for (SampleItemEntity sampleItemEntity1 : itemList) {
                Integer itemId = sampleItemEntity.getItemIds().get(i);
                if (sampleItemEntity1.getId().equals(itemId)) {
                    CheckItemDeptVo checkItemDeptVo = new CheckItemDeptVo();
                    // 检测项主键
                    checkItemDeptVo.setId(itemId);
                    // 样次
                    checkItemDeptVo.setTimes(sampleItemEntity1.getTimes());
                    // 单价
                    checkItemDeptVo.setCheckPrice(sampleItemEntity1.getUnitPrice());
                    // 部门
                    checkItemDeptVo.setDeptId(sampleItemEntity.getTechnicistId());
                    checkItemDeptVoList.add(checkItemDeptVo);
                }
            }
            for (SampleItemEntity sampleItemEntity1 : itemList) {
                Integer itemId = sampleItemEntity.getItemIds().get(i);
                if (sampleItemEntity1.getId().equals(itemId)) {
                    for (TestCheckItemsTaskRel taskRel : sampleItemEntity.getItemsTaskRels()) {
                        // 检测项主键
                        taskRel.setItemId(itemId);
                        // 检测项名称
                        taskRel.setCheckItemName(sampleItemEntity1.getCheckItemName());
                        // 样品名称
                        taskRel.setSampleName(sampleItemEntity1.getSampleName());
                        // 样品编号
                        taskRel.setSampleCode(sampleItemEntity1.getSampleCode());
                        // 样品id
                        taskRel.setSampleId(sampleItemEntity1.getSampleId());
                        // 委托单主键
                        taskRel.setEntrustId(entrustId);
                        // 任务单id
                        taskRel.setTaskId(taskId);
                        // 调用方法： 对每组检测项的人员信息进行新增。
                        testCheckItemsTaskRelMapper.insert(taskRel);
                    }
                }
            }
        }
        entity.setCheckItemDeptVoList(checkItemDeptVoList);
        // 发布任务
        distributionTask412(entity, sampleItemEntity, poolId, taskId, entrustAddVo, taskCodeMap);
    }

    /**
     * 针对记录人、复核人、报告制作人信息 方法读取人员信息
     *
     * @param sampleItemEntity
     * @param inspectorArraysVos
     * @param inspectorRels
     */
    void methodForPersonnel(SampleItemEntity sampleItemEntity, List<LabelValueVo> inspectorArraysVos, List<TestCheckItemsTaskRel> inspectorRels) {
        // 获取每组检测项 对应的 （0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生）
        // 读取记录人数据
        if (StringUtils.isNotEmpty(sampleItemEntity.getRecorder())) {
            String[] userArrays = sampleItemEntity.getRecorder().split(",");
            for (int i = 0; i < userArrays.length; i++) {
                String[] names = userArrays[i].split("&");
                LabelValueVo labelValueVo = new LabelValueVo();
                labelValueVo.setLabel(names[0]);
                labelValueVo.setValue(Long.parseLong(names[1]));
                labelValueVo.setText("1");
                inspectorArraysVos.add(labelValueVo);
            }
        }
        // 读取复核人数据
        if (StringUtils.isNotEmpty(sampleItemEntity.getReviewer())) {
            String[] userArrays = sampleItemEntity.getReviewer().split(",");
            for (int i = 0; i < userArrays.length; i++) {
                String[] names = userArrays[i].split("&");
                LabelValueVo labelValueVo = new LabelValueVo();
                labelValueVo.setLabel(names[0]);
                labelValueVo.setValue(Long.parseLong(names[1]));
                labelValueVo.setText("2");
                inspectorArraysVos.add(labelValueVo);
            }
        }
        // 报告制作人
        if (StringUtils.isNotEmpty(sampleItemEntity.getReportProducer())) {
            String[] userArrays = sampleItemEntity.getReportProducer().split(",");
            for (int i = 0; i < userArrays.length; i++) {
                String[] names = userArrays[i].split("&");
                LabelValueVo labelValueVo = new LabelValueVo();
                labelValueVo.setLabel(names[0]);
                labelValueVo.setValue(Long.parseLong(names[1]));
                labelValueVo.setText("3");
                inspectorArraysVos.add(labelValueVo);
            }
        }
        // 辅助人员
        if (StringUtils.isNotEmpty(sampleItemEntity.getAuxiliaryPersonnel())) {
            String[] userArrays = sampleItemEntity.getAuxiliaryPersonnel().split(",");
            for (int i = 0; i < userArrays.length; i++) {
                String[] names = userArrays[i].split("&");
                LabelValueVo labelValueVo = new LabelValueVo();
                labelValueVo.setLabel(names[0]);
                labelValueVo.setValue(Long.parseLong(names[1]));
                labelValueVo.setText("4");
                inspectorArraysVos.add(labelValueVo);
            }
        }
        // 5、见习生：实习的新手
        if (StringUtils.isNotEmpty(sampleItemEntity.getProbationer())) {
            String[] userArrays = sampleItemEntity.getProbationer().split(",");
            for (int i = 0; i < userArrays.length; i++) {
                String[] names = userArrays[i].split("&");
                LabelValueVo labelValueVo = new LabelValueVo();
                labelValueVo.setLabel(names[0]);
                labelValueVo.setValue(Long.parseLong(names[1]));
                labelValueVo.setText("5");
                inspectorArraysVos.add(labelValueVo);
            }
        }
        // 6、实习生
        if (StringUtils.isNotEmpty(sampleItemEntity.getInterns())) {
            String[] userArrays = sampleItemEntity.getInterns().split(",");
            for (int i = 0; i < userArrays.length; i++) {
                String[] names = userArrays[i].split("&");
                LabelValueVo labelValueVo = new LabelValueVo();
                labelValueVo.setLabel(names[0]);
                labelValueVo.setValue(Long.parseLong(names[1]));
                labelValueVo.setText("6");
                inspectorArraysVos.add(labelValueVo);
            }
        }
        // 循环读取 每组检测项
        for (LabelValueVo labelValueVo : inspectorArraysVos) {
            TestCheckItemsTaskRel data = new TestCheckItemsTaskRel();
            data.setUserName(labelValueVo.getLabel());
            data.setUserId(labelValueVo.getValue().toString());
            data.setUserType(Integer.parseInt(labelValueVo.getText()));
            inspectorRels.add(data);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public synchronized Boolean distributionTask412(TaskVo entity, SampleItemEntity sampleItemEntity, Long poolId, Long taskId, EntrustAddVo entrustAddVo, Map<Long, TaskProgressVo> taskCodeMap) {
        List<Long> deptIds = Lists.newArrayList();
        List<CheckItemDeptVo> checkItemDeptVoList = entity.getCheckItemDeptVoList();
        for (CheckItemDeptVo vo : checkItemDeptVoList) {
            if (!deptIds.contains(vo.getDeptId())) {
                deptIds.add(vo.getDeptId());
            }
        }
        //创建任务对象
        List<TaskVo> vos = Lists.newArrayList();
        // 读取检测项中样品id
        List<Integer> itemIds = new ArrayList<>();
        for (Long deptId : deptIds) {
            //计算本单价格
            double taskPrice = 0L;
            for (CheckItemDeptVo vo : checkItemDeptVoList) {
                if (deptId.equals(vo.getDeptId())) {
                    taskPrice = taskPrice + ((entity.getDiscount() == null ? 0 : entity.getDiscount()) *
                            (vo.getCheckPrice() == null ? 0 : vo.getCheckPrice()) * vo.getTimes());
                }
                itemIds.add(vo.getId());
            }
            TaskVo vo = new TaskVo();
            vo.setId(taskId);
            vo.setTaskPrice(taskPrice);
            //根据委托单号月份确定任务单ID
            if (taskCodeMap.get(deptId) != null) {
                TaskProgressVo oldTaskCode = taskCodeMap.get(deptId);
                vo.setCode(oldTaskCode.getCode());
                vo.setTaskCode(oldTaskCode.getTaskCode());
            } else {
                String teamCode = taskMapper.getTeamCode(deptId);
                String entrustmentNo = entrustAddVo.getEntrustmentNo() + "";
                String format = entrustmentNo.substring(2, 6);
                Integer integer = taskMapper.selectMaxNoByCode(teamCode + format);
                Integer code = null;
                if (integer == null) {
                    String currentTime = DateUtil.getTodayString().substring(2, 6);
                    code = Integer.parseInt(format + "001");
                } else {
                    code = integer + 1;
                }
                String codeStr = code + "";
                vo.setCode(codeStr);
                vo.setTaskCode(teamCode + codeStr.substring(0, 4) + "-" + codeStr.substring(4, 7));
            }
            vo.setDeptId(deptId);
            vo.setEntrustmentId(entity.getEntrustmentId());
            vo.setRequiredCompletionTime(entity.getRequiredCompletionTime());
            vo.setOrderTime(entity.getOrderTime());
            vo.setState(1);
            vo.setReportComplete(2);
            SysUserEntity userInfo = ShiroUtils.getUserInfo();
            // 下单人 = 委托单受理人
            vo.setOrderer(entrustAddVo.getBusinessAcceptor());
            // 下单日期 = 受理日期
            vo.setOrderTime(entrustAddVo.getAcceptanceDate());
            vo.setPresentInformation(entity.getPresentInformation());
            // 接单人(授权签字人，报告签发人)
            vo.setReceiver(userInfo.getUserId().toString());
            // 接单时间
            vo.setReceiverTime(new Date());;
            // 团队
            vo.setTeamId(deptId);
            // 设置出报告团队
            vo.setIssueReport("是");
            // 任务单创建时间
            vo.setCreateTime(new Date());
            // 补充人员信息 检测人
            vo.setInspector(sampleItemEntity.getInspector());
            // 记录人
            vo.setRecorder(sampleItemEntity.getRecorder());
            // 复核人
            vo.setReviewer(sampleItemEntity.getReviewer());
            // 报告制作人
            vo.setReportProducer(sampleItemEntity.getReportProducer());
            // 辅助人员
            vo.setAuxiliaryPersonnel(sampleItemEntity.getAuxiliaryPersonnel());
            // 见习生：实习的新手
            vo.setProbationer(sampleItemEntity.getProbationer());
            // 实习生
            vo.setInterns(sampleItemEntity.getInterns());
            // 领样人
            vo.setSampler(entity.getSampler());
            // 领样时间
            vo.setSampleReceivingTime(vo.getOrderTime());
            // 样品状态描述
            vo.setOutwardDescribe(sampleItemEntity.getSampleStateDescription());
            if (StringUtils.isEmpty(vo.getOutwardDescribe())) {
                List<SampleEntity> outwardDescribeList = sampleEntityMapper.getSampleOutwardDescribeList(itemIds);
                if (CollectionUtil.isNotEmpty(outwardDescribeList)) {
                    StringBuffer stringBuffer = new StringBuffer();
                    for (SampleEntity sampleEntityData : outwardDescribeList) {
                        stringBuffer.append(sampleEntityData.getOutwardDescribe());
                        stringBuffer.append("、");
                    }
                    if (StringUtils.isNotEmpty(stringBuffer.toString())) {
                        vo.setOutwardDescribe(stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString());
                    }
                }
            }
            // 流水号任务单id
            vo.setPoolId(poolId);
            vos.add(vo);
            //更新检测项分配的部门和任务单号
            List<CheckItemDeptVo> checkItemDeptVoList1 = Lists.newArrayList();
            for (CheckItemDeptVo checkItemDeptVo : checkItemDeptVoList) {
                if (deptId.equals(checkItemDeptVo.getDeptId())) {
                    checkItemDeptVo.setTaskId(taskId);
                    checkItemDeptVoList1.add(checkItemDeptVo);
                }
            }
            //更新检测项信息
            taskMapper.batchUpdateCheckItem(checkItemDeptVoList1);
            // 2023年9月26日 发布时：样品状态 = 待检
            // 通过委托单id 获取样品信息
            List<Integer> sampleIds = entityMapper.getSampleId(entity.getEntrustmentId());
            if (CollectionUtil.isNotEmpty(sampleIds)) {
                for (Integer sampleId : sampleIds) {
                    // 更新委托状态
                    SampleEntity record = new SampleEntity();
                    record.setId(sampleId);
                    // 样品状态 预收样 = 收样
                    record.setState("0");
                    // update样品信息状态
                    sampleEntityMapper.updateByPrimaryKeySelective(record);
                    // 根据样品id 查询样品流转列表
                    List<SampleCirculationRecord> circulationList = sampleEntityMapper.getRecords(sampleId, 30);
                    if (CollectionUtils.isEmpty(circulationList)) {
                        // 增加样品样品流转状态
                        SampleCirculationRecord sa = new SampleCirculationRecord();
                        sa.setSampleId(sampleId);
                        sa.setStatus("0");
                        sa.setOperatorId(userInfo.getUserId());
                        sa.setOperatorName(userInfo.getName());
                        sa.setTime(new Date());
                        sampleEntityMapper.saveSampleCirculationRecord(sa);
                    } else {
                        Boolean flag = false;
                        for (SampleCirculationRecord sampleCirculationRecord : circulationList) {
                            if (sampleCirculationRecord.getStatus().equals("0")) {
                                flag = true;
                            }
                        }
                        if (!flag) {
                            // 增加样品样品流转状态
                            SampleCirculationRecord sa = new SampleCirculationRecord();
                            sa.setSampleId(sampleId);
                            sa.setStatus("0");
                            sa.setOperatorId(userInfo.getUserId());
                            sa.setOperatorName(userInfo.getName());
                            sa.setTime(new Date());
                            sampleEntityMapper.saveSampleCirculationRecord(sa);
                        }
                    }
                }
            }
            // 记录发布任务单的日志
            StringBuffer stringBuilder1 = new StringBuffer();
            stringBuilder1.append("任务大厅领取日志");
            stringBuilder1.append("任务单id" + vo.getId());
            stringBuilder1.append("任务单号" + vo.getTaskCode());
            stringBuilder1.append("委托单id" + vo.getEntrustmentId());
            stringBuilder1.append("是否出具报告" + vo.getIssueReport());
            stringBuilder1.append("价格" + vo.getTaskPrice());
            stringBuilder1.append("任务单创建时间" + vo.getCreateTime());
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), stringBuilder1.toString(), Const.TASK_FLOW, true);
        }
        //任务单保存
        taskMapper.batchSave(vos);
        try {
            if (taskCodeMap.get(vos.get(0).getDeptId().longValue()) == null) {
                // 任务单领取 使用钉钉 通知相关人员
                receivePushInformationmethod(vos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //更新委托单状态
        taskMapper.updateEntrustById(entity.getEntrustmentId(), 1);
        return true;
    }

    /**
     * 任务单领取 使用钉钉 通知相关人员
     *
     * @param vos
     */
    public void receivePushInformationmethod(List<TaskVo> vos) throws Exception {
        for (TaskVo taskVo : vos) {
            // 检测人
            methodForEachNotice(taskVo, taskVo.getInspector(), 1);
            // 记录人
            methodForEachNotice(taskVo, taskVo.getRecorder(), 2);
            // 复核人
            methodForEachNotice(taskVo, taskVo.getReviewer(), 3);
            // 制作人
            methodForEachNotice(taskVo, taskVo.getReportProducer(), 4);
            // 辅助人员
            if(StringUtils.isNotEmpty(taskVo.getAuxiliaryPersonnel())){
                methodForEachNotice(taskVo, taskVo.getAuxiliaryPersonnel(), 5);
            }
            // 见习生：实习的新手
            if(StringUtils.isNotEmpty(taskVo.getProbationer())){
                methodForEachNotice(taskVo, taskVo.getProbationer(), 6);
            }
            // 实习生
            if(StringUtils.isNotEmpty(taskVo.getInterns())){
                methodForEachNotice(taskVo, taskVo.getInterns(), 7);
            }

        }
    }

    /**
     * 调用方法循环 通知信息
     *
     * @param taskVo
     * @param personnel
     * @param type
     */
    void methodForEachNotice(TaskVo taskVo, String personnel, Integer type) throws Exception {
        // 进行钉钉发布消息操作
        DingNotifyUtils dingNotifyUtils = new DingNotifyUtils();
        String PersonnelType = "";
        switch (type) {
            case 1:
                PersonnelType = "检测人";
                break;
            case 2:
                PersonnelType = "记录人";
                break;
            case 3:
                PersonnelType = "复核人";
                break;
            case 4:
                PersonnelType = "制作人";
                break;
            case 5:
                PersonnelType = "辅助人员";
                break;
            case 6:
                PersonnelType = "见习生";
                break;
            case 7:
                PersonnelType = "实习生";
                break;
            default:
                break;
        }
        // 任务单号
        String taskCode = taskVo.getTaskCode();
        // 发布人 : 取任务单下单人
        String publisher = taskVo.getOrderer();
        // 检测人
        String[] inspectorArray = personnel.split(",");
        // 查询钉钉id
        String dingId = "";
        for (int i = 0; i < inspectorArray.length; i++) {
            String inspectorStr = inspectorArray[i];
            String[] inspectors = inspectorStr.split("&");
            // 获取 任务单下检测人信息 userId
            LambdaQueryWrapper<SysUserEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUserEntity::getUserId, inspectors[1]);
            SysUserEntity userDetails = sysUserDao.selectOne(queryWrapper);
            dingId = userDetails.getDingUserId();
            StringBuffer titleBuffer = new StringBuffer();
            titleBuffer.append("任务大厅中指派您：" + userDetails.getName() + "为" + PersonnelType);
            titleBuffer.append("任务单号为： " + taskCode + " 请及时操作");
            dingNotifyUtils.OAWorkNotice(dingId, titleBuffer.toString(), publisher,null);
        }
    }

    /**
     * TODO: 2024年1月3日  任务生成规则根据签发人所属团队走   = 授权操作人 = 领取人。
     * 规则：人员跨部门可以选。 （新的任务单号 生成时、只有一个单号。）
     * 领取任务单
     *
     * @param list 检测项数据
     */
    public void newAddTask(List<SampleItemEntity> list, Long entrustId, Long teamId, TestTaskPool testTaskPool) {

        // 检测项id集合
        List<Integer> itemIdList = new ArrayList<>();
        for (SampleItemEntity sampleItemEntity : list) {
            itemIdList.addAll(sampleItemEntity.getItemIds());
        }
        // 通过itemIds 查看检测项列表。
        List<SampleItemEntity> itemList = taskPoolMapper.selectItems(null, itemIdList);

        // 任务单id
        long taskId = GenID.getID();
        // 领样人
        String sampler = "";
        // map信息处理 ： key = （ userType） 、value = （set去重后的人员信息）
        HashMap<Integer, Set<TestCheckItemsTaskRel>> itemsTaskMap = new HashMap<>();
        // 补充每组检测项中人员信息
        for (SampleItemEntity sampleItemEntity : list) {
            // 获取每组检测项 对应的 （0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生）
            List<LabelValueVo> inspectorArraysVos = new ArrayList<>();
            List<TestCheckItemsTaskRel> inspectorRels = new ArrayList<>();
            // 调用方法： 针对记录人、复核人、报告制作人信息 方法读取人员信息
            newMethodForPersonnel(sampleItemEntity, inspectorArraysVos, inspectorRels);
            sampleItemEntity.setItemsTaskRels(inspectorRels);

            // 根据 taskId 和检测项集合 条件删除流转信息
            LambdaQueryWrapper<TestCheckItemsTaskRel> queryWrapper12 = new LambdaQueryWrapper<>();
            queryWrapper12.in(TestCheckItemsTaskRel::getItemId, sampleItemEntity.getItemIds());
            testCheckItemsTaskRelMapper.delete(queryWrapper12);

            // 新增检测项对应的操作人员信息。
            newMethodPublishTaskList(entrustId, sampleItemEntity, itemList, taskId, itemsTaskMap);
            if (StringUtils.isNotEmpty(sampleItemEntity.getSampler())) {
                sampler = sampleItemEntity.getSampler().split("-")[0];
            }
        }
        // 补充任务单信息
        SampleItemEntity addSampleItemEntity = new SampleItemEntity();
        for (Integer key : itemsTaskMap.keySet()) {
            //  0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生
            switch (key) {
                case 0:
                    //  0：检测人
                    addSampleItemEntity.setInspector(methodReturnString(itemsTaskMap, key, 0));
                    break;
                case 1:
                    // 1：记录人、
                    addSampleItemEntity.setRecorder(methodReturnString(itemsTaskMap, key, 0));
                    break;
                case 2:
                    // 2：复核人、
                    addSampleItemEntity.setReviewer(methodReturnString(itemsTaskMap, key, 0));
                    break;
                case 3:
                    // 3：报告制作人、
                    addSampleItemEntity.setReportProducer(methodReturnString(itemsTaskMap, key, 0));
                    break;
                case 4:
                    // 4：辅助人员、
                    addSampleItemEntity.setAuxiliaryPersonnel(methodReturnString(itemsTaskMap, key, 0));
                    break;
                case 5:
                    // 5、见习生：
                    addSampleItemEntity.setProbationer(methodReturnString(itemsTaskMap, key, 0));
                    break;
                case 6:
                    // 6、实习生：
                    addSampleItemEntity.setInterns(methodReturnString(itemsTaskMap, key, 0));
                    break;
                default:
                    break;
            }
        }
        // 领样人
        addSampleItemEntity.setSampler(sampler);
        // 任务单id
        addSampleItemEntity.setTaskId(taskId);
        // 科室id
        addSampleItemEntity.setTechnicistId(teamId);
        // 委托单id
        addSampleItemEntity.setEntrustId(entrustId);
        EntrustAddVo entrustAddVo = entityMapper.selectByKeyId(entrustId);
        // 进行新建任务单信息、补充信息
        TaskVo entity = new TaskVo();
        // 委托单信息
        entity.setEntrustmentId(entrustId);
        // 设置折扣
        entity.setDiscount(Double.parseDouble(entrustAddVo.getDiscount()));
        //  流水号任务单id
        addSampleItemEntity.setPoolId(testTaskPool.getId().longValue());
        //计算本单价格
        double taskPrice = 0L;
        for (SampleItemEntity vo : itemList) {
            taskPrice = taskPrice + ((entity.getDiscount() == null ? 0 : entity.getDiscount()) *
                    (vo.getUnitPrice() == null ? 0 : vo.getUnitPrice()) * vo.getTimes());
        }
        addSampleItemEntity.setItemIds(itemIdList);
        // 计算task单价
        addSampleItemEntity.setTaskPrice(taskPrice);
        // 丁连春：任务单完成时间 以委托单下单时间为准
        entity.setRequiredCompletionTime(entrustAddVo.getRequestDate());
        // 任务单下单日期等于委托单受理日期
        entity.setOrderTime(entrustAddVo.getAcceptanceDate());
        // 任务单提供资料等于委托单提供资料
        if (!org.springframework.util.StringUtils.isEmpty(entrustAddVo.getPresentInformation())) {
            entity.setPresentInformation(entrustAddVo.getPresentInformation());
        } else {
            entity.setPresentInformation("--");
        }
        // 领取任务
        addTask(entity, entrustAddVo, addSampleItemEntity);
        // 更新检测项及样品流转状态
        updateItemStatus(addSampleItemEntity, list);
    }

    /**
     * 新增每组检测项数据
     *
     * @param entrustId
     * @param sampleItemEntity
     * @param itemList
     * @param itemsTaskMap     map信息处理 ： key = （ userType） 、value = （set去重后的人员信息）
     */
    void newMethodPublishTaskList(Long entrustId, SampleItemEntity sampleItemEntity, List<SampleItemEntity> itemList,
                                  Long taskId, HashMap<Integer, Set<TestCheckItemsTaskRel>> itemsTaskMap) {
        // map中 key = userType 、value = （key = userId，value = 对象）。
        HashMap<Integer, HashMap<Long, TestCheckItemsTaskRel>> map = new HashMap();
        // 遍历每组检测项分配的itemId数量
        for (int i = 0; i < sampleItemEntity.getItemIds().size(); i++) {
            for (SampleItemEntity sampleItemEntity1 : itemList) {
                Integer itemId = sampleItemEntity.getItemIds().get(i);
                if (sampleItemEntity1.getId().equals(itemId)) {
                    for (TestCheckItemsTaskRel taskRel : sampleItemEntity.getItemsTaskRels()) {
                        // 检测项主键
                        taskRel.setItemId(itemId);
                        // 检测项名称
                        taskRel.setCheckItemName(sampleItemEntity1.getCheckItemName());
                        // 样品名称
                        taskRel.setSampleName(sampleItemEntity1.getSampleName());
                        // 样品编号
                        taskRel.setSampleCode(sampleItemEntity1.getSampleCode());
                        // 样品id
                        taskRel.setSampleId(sampleItemEntity1.getSampleId());
                        // 委托单主键
                        taskRel.setEntrustId(entrustId);
                        // 任务单id
                        taskRel.setTaskId(taskId);
                        // map 统计人员中信息
                        if (map.get(taskRel.getUserType()) == null) {
                            HashMap<Long, TestCheckItemsTaskRel> userCheckItemsTaskRelMap = new HashMap<>();
                            userCheckItemsTaskRelMap.put(Long.valueOf(taskRel.getUserId()), taskRel);
                            map.put(taskRel.getUserType(), userCheckItemsTaskRelMap);
                        } else {
                            // map !=null 带出数据直接存储即可。
                            HashMap<Long, TestCheckItemsTaskRel> userCheckItemsTaskRelMap = map.get(taskRel.getUserType());
                            userCheckItemsTaskRelMap.put(Long.valueOf(taskRel.getUserId()), taskRel);
                            map.put(taskRel.getUserType(), userCheckItemsTaskRelMap);
                        }
                        // 调用方法： 对每组检测项的人员信息进行新增。
                        testCheckItemsTaskRelMapper.insert(taskRel);
                    }
                }
            }
        }
        // 处理map数据
        for (Integer userType : map.keySet()) {
            Set<TestCheckItemsTaskRel> itemsTaskRelSet = new HashSet<>();
            if (itemsTaskMap.get(userType) != null) {
                itemsTaskRelSet.addAll(itemsTaskMap.get(userType));
            }
            HashMap<Long, TestCheckItemsTaskRel> userCheckItemsTaskRelMap = map.get(userType);
            for (Long userId : userCheckItemsTaskRelMap.keySet()) {
                itemsTaskRelSet.add(userCheckItemsTaskRelMap.get(userId));
            }
            itemsTaskMap.put(userType, itemsTaskRelSet);
        }
    }

    /**
     * 新增任务单。
     *
     * @param entity
     * @param sampleItemEntity
     * @param entrustAddVo
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void addTask(TaskVo entity, EntrustAddVo entrustAddVo, SampleItemEntity sampleItemEntity) {
        Long poolId = sampleItemEntity.getPoolId();
        Long taskId = sampleItemEntity.getTaskId();
        Long deptId = sampleItemEntity.getTechnicistId();
        double taskPrice = sampleItemEntity.getTaskPrice();
        //创建任务对象
        List<TaskVo> vos = Lists.newArrayList();
        // 检测项id集合
        List<Integer> itemIds = new ArrayList<>();
        itemIds.addAll(sampleItemEntity.getItemIds());
        //计算本单价格
        TaskVo vo = new TaskVo();
        vo.setId(taskId);
        vo.setTaskPrice(taskPrice);
        String teamCode = taskMapper.getTeamCode(deptId);
        String entrustmentNo = entrustAddVo.getEntrustmentNo() + "";
        String format = entrustmentNo.substring(2, 6);
        Integer integer = taskMapper.selectMaxNoByCode(teamCode + format);
        Integer code = null;
        if (integer == null) {
            code = Integer.parseInt(format + "001");
        } else {
            code = integer + 1;
        }
        String codeStr = code + "";
        vo.setCode(codeStr);
        vo.setTaskCode(teamCode + codeStr.substring(0, 4) + "-" + codeStr.substring(4, 7));
        vo.setDeptId(deptId);
        vo.setEntrustmentId(entity.getEntrustmentId());
        vo.setRequiredCompletionTime(entity.getRequiredCompletionTime());
        vo.setOrderTime(entity.getOrderTime());
        vo.setState(1);
        vo.setReportComplete(2);
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        // 下单人 = 委托单受理人
        vo.setOrderer(entrustAddVo.getBusinessAcceptor());
        // 下单日期 = 受理日期
        vo.setOrderTime(entrustAddVo.getAcceptanceDate());
        vo.setPresentInformation(entity.getPresentInformation());
        // 接单人(授权签字人，报告签发人)
        vo.setReceiver(userInfo.getUserId().toString());
        // 接单时间
        vo.setReceiverTime(new Date());
        // 团队
        vo.setTeamId(deptId);
        // 设置出报告团队
        vo.setIssueReport("是");
        // 任务单创建时间
        vo.setCreateTime(new Date());
        // 补充人员信息 检测人
        vo.setInspector(methodRetrunStrSplit(sampleItemEntity.getInspector(), ","));
        // 记录人
        vo.setRecorder(methodRetrunStrSplit(sampleItemEntity.getRecorder(), ","));
        // 复核人
        vo.setReviewer(methodRetrunStrSplit(sampleItemEntity.getReviewer(), ","));
        // 报告制作人
        vo.setReportProducer(methodRetrunStrSplit(sampleItemEntity.getReportProducer(), ","));
        // 辅助人员
        vo.setAuxiliaryPersonnel(methodRetrunStrSplit(sampleItemEntity.getAuxiliaryPersonnel(), ","));
        // 见习生：实习的新手
        vo.setProbationer(methodRetrunStrSplit(sampleItemEntity.getProbationer(), ","));
        // 实习生
        vo.setInterns(methodRetrunStrSplit(sampleItemEntity.getInterns(), ","));
        // 领样人
        vo.setSampler(sampleItemEntity.getSampler());
        // 领样时间
        vo.setSampleReceivingTime(vo.getOrderTime());
        // 样品状态描述
        vo.setOutwardDescribe(sampleItemEntity.getSampleStateDescription());
        if (StringUtils.isEmpty(vo.getOutwardDescribe())) {
            List<SampleEntity> outwardDescribeList = sampleEntityMapper.getSampleOutwardDescribeList(itemIds);
            if (CollectionUtil.isNotEmpty(outwardDescribeList)) {
                StringBuffer stringBuffer = new StringBuffer();
                for (SampleEntity sampleEntityData : outwardDescribeList) {
                    stringBuffer.append(sampleEntityData.getOutwardDescribe());
                    stringBuffer.append("、");
                }
                if (StringUtils.isNotEmpty(stringBuffer.toString())) {
                    vo.setOutwardDescribe(stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString());
                }
            }
        }
        // 流水号任务单id
        vo.setPoolId(poolId);
        // new任务单
        vo.setTaskListStatus("新任务单");
        vos.add(vo);
        // 记录发布任务单的日志
        StringBuffer stringBuilder1 = new StringBuffer();
        stringBuilder1.append("任务大厅领取日志");
        stringBuilder1.append("任务单id" + vo.getId());
        stringBuilder1.append("任务单号" + vo.getTaskCode());
        stringBuilder1.append("委托单id" + vo.getEntrustmentId());
        stringBuilder1.append("是否出具报告" + vo.getIssueReport());
        stringBuilder1.append("价格" + vo.getTaskPrice());
        stringBuilder1.append("任务单创建时间" + vo.getCreateTime());
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), stringBuilder1.toString(), Const.TASK_FLOW, true);
        //任务单保存
        taskMapper.batchSave(vos);
        try {
            // 任务单领取 使用钉钉 通知相关人员
            receivePushInformationmethod(vos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 实现str 截取
     *
     * @param str
     * @param split
     * @return
     */
    public String methodRetrunStrSplit(String str, String split) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        Set<String> set = new HashSet<>();
        String[] strings = str.split(split);
        for (int i = 0; i < strings.length; i++) {
            set.add(strings[i]);
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (String strSet : set) {
            stringBuffer.append(strSet);
            stringBuffer.append(split);
        }
        return stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString();
    }

    /**
     * 方法： 循环遍历 返回数据人员信息
     *
     * @param itemsTaskMap
     * @param key
     * @param type         =0 保留 name + & + userId 格式，=1 仅保留 userId即可。
     * @return
     */
    public String methodReturnString(HashMap<Integer, Set<TestCheckItemsTaskRel>> itemsTaskMap, Integer key, Integer type) {
        //  0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生
        Set<TestCheckItemsTaskRel> taskRelSet = itemsTaskMap.get(key);
        StringBuffer namebuffer = new StringBuffer();
        for (TestCheckItemsTaskRel taskRel : taskRelSet) {
            if (type == 0) {
                namebuffer.append(taskRel.getUserName() + "&" + taskRel.getUserId());
                namebuffer.append(",");
            }
            if (type == 1) {
                namebuffer.append(taskRel.getUserId());
                namebuffer.append(",");
            }
        }
        return namebuffer.deleteCharAt(namebuffer.length() - 1).toString();
    }

    /**
     * 更新检测项状态及样品流转状态
     *
     * @param addSampleItemEntity
     * @param list
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateItemStatus(SampleItemEntity addSampleItemEntity, List<SampleItemEntity> list) {
        List<Integer> itemIds = new ArrayList<>();
        // key = itemId 、value = bitValue
        HashMap<Integer, Integer> itemMap = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            SampleItemEntity sampleItemEntity = list.get(i);
            for (Integer itemId : sampleItemEntity.getItemIds()) {
                int zhi = 1 + i;
                itemMap.put(itemId, zhi);
                itemIds.add(itemId);
            }
        }
        // 登录人
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        //更新检测项分配的部门和任务单号
        List<CheckItemDeptVo> checkItemDeptVoList1 = Lists.newArrayList();
        for (Integer itemId : itemIds) {
            CheckItemDeptVo checkItemDeptVo = new CheckItemDeptVo();
            checkItemDeptVo.setTaskId(addSampleItemEntity.getTaskId());
            checkItemDeptVo.setId(itemId);
            checkItemDeptVo.setBitValue(itemMap.get(itemId));
            checkItemDeptVo.setDeptId(addSampleItemEntity.getTechnicistId());
            checkItemDeptVoList1.add(checkItemDeptVo);
        }
        //更新检测项信息
        taskMapper.batchUpdateCheckItem(checkItemDeptVoList1);
        // 2023年9月26日 发布时：样品状态 = 待检
        // 通过委托单id 获取样品信息
        List<Integer> sampleIds = entityMapper.getSampleId(addSampleItemEntity.getEntrustId());
        if (CollectionUtil.isNotEmpty(sampleIds)) {
            for (Integer sampleId : sampleIds) {
                // 更新委托状态
                SampleEntity record = new SampleEntity();
                record.setId(sampleId);
                // 样品状态 预收样 = 收样
                record.setState("0");
                // update样品信息状态
                sampleEntityMapper.updateByPrimaryKeySelective(record);
                // 根据样品id 查询样品流转列表
                List<SampleCirculationRecord> circulationList = sampleEntityMapper.getRecords(sampleId, 30);
                if (CollectionUtils.isEmpty(circulationList)) {
                    // 增加样品样品流转状态
                    SampleCirculationRecord sa = new SampleCirculationRecord();
                    sa.setSampleId(sampleId);
                    sa.setStatus("0");
                    sa.setOperatorId(userInfo.getUserId());
                    sa.setOperatorName(userInfo.getName());
                    sa.setTime(new Date());
                    sampleEntityMapper.saveSampleCirculationRecord(sa);
                } else {
                    Boolean flag = false;
                    for (SampleCirculationRecord sampleCirculationRecord : circulationList) {
                        if (sampleCirculationRecord.getStatus().equals("0")) {
                            flag = true;
                        }
                    }
                    if (!flag) {
                        // 增加样品样品流转状态
                        SampleCirculationRecord sa = new SampleCirculationRecord();
                        sa.setSampleId(sampleId);
                        sa.setStatus("0");
                        sa.setOperatorId(userInfo.getUserId());
                        sa.setOperatorName(userInfo.getName());
                        sa.setTime(new Date());
                        sampleEntityMapper.saveSampleCirculationRecord(sa);
                    }
                }
            }
        }
    }

    /**
     * TODO: 2024年1月3日  任务生成规则根据签发人所属团队走   = 授权操作人 = 领取人。
     * 规则：人员跨部门可以选。 （新的任务单号 生成时、只有一个单号。）
     * 领取任务单
     *
     * @param list 检测项数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void newUpdateTask(List<SampleItemEntity> list, Long entrustId, TaskProgressVo taskProgressVo) {

        long taskId = taskProgressVo.getTaskId();
        long teamId = taskProgressVo.getDeptId();

        // 检测项id集合
        List<Integer> itemIdList = new ArrayList<>();
        for (SampleItemEntity sampleItemEntity : list) {
            itemIdList.addAll(sampleItemEntity.getItemIds());
        }
        // 通过itemIds 查看检测项列表。
        List<SampleItemEntity> itemList = taskPoolMapper.selectItems(null, itemIdList);
        // 领样人
        String sampler = "";
        // map信息处理 ： key = （ userType） 、value = （set去重后的人员信息）
        HashMap<Integer, Set<TestCheckItemsTaskRel>> itemsTaskMap = new HashMap<>();
        // 补充每组检测项中人员信息
        for (SampleItemEntity sampleItemEntity : list) {
            // 获取每组检测项 对应的 （0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生）
            List<LabelValueVo> inspectorArraysVos = new ArrayList<>();
            List<TestCheckItemsTaskRel> inspectorRels = new ArrayList<>();
            // 调用方法： 针对记录人、复核人、报告制作人信息 方法读取人员信息
            newMethodForPersonnel(sampleItemEntity, inspectorArraysVos, inspectorRels);
            sampleItemEntity.setItemsTaskRels(inspectorRels);

            // 根据 taskId 和检测项集合 条件删除流转信息
            LambdaQueryWrapper<TestCheckItemsTaskRel> queryWrapper12 = new LambdaQueryWrapper<>();
            queryWrapper12.in(TestCheckItemsTaskRel::getItemId, sampleItemEntity.getItemIds());
            queryWrapper12.or().eq(TestCheckItemsTaskRel::getTaskId, taskId);
            testCheckItemsTaskRelMapper.delete(queryWrapper12);

            // 新增检测项对应的操作人员信息。
            newMethodPublishTaskList(entrustId, sampleItemEntity, itemList, taskId, itemsTaskMap);
            if (StringUtils.isNotEmpty(sampleItemEntity.getSampler())) {
                sampler = sampleItemEntity.getSampler().split("-")[0];
            }
        }
        // 补充任务单信息
        SampleItemEntity addSampleItemEntity = new SampleItemEntity();
        for (Integer key : itemsTaskMap.keySet()) {
            //  0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生
            switch (key) {
                case 0:
                    //  0：检测人
                    addSampleItemEntity.setInspector(methodReturnString(itemsTaskMap, key, 0));
                    break;
                case 1:
                    // 1：记录人、
                    addSampleItemEntity.setRecorder(methodReturnString(itemsTaskMap, key, 0));
                    break;
                case 2:
                    // 2：复核人、
                    addSampleItemEntity.setReviewer(methodReturnString(itemsTaskMap, key, 0));
                    break;
                case 3:
                    // 3：报告制作人、
                    addSampleItemEntity.setReportProducer(methodReturnString(itemsTaskMap, key, 0));
                    break;
                case 4:
                    // 4：辅助人员、
                    addSampleItemEntity.setAuxiliaryPersonnel(methodReturnString(itemsTaskMap, key, 0));
                    break;
                case 5:
                    // 5、见习生：
                    addSampleItemEntity.setProbationer(methodReturnString(itemsTaskMap, key, 0));
                    break;
                case 6:
                    // 6、实习生：
                    addSampleItemEntity.setInterns(methodReturnString(itemsTaskMap, key, 0));
                    break;
                default:
                    break;
            }
        }
        // 领样人
        addSampleItemEntity.setSampler(sampler);
        // 任务单id
        addSampleItemEntity.setTaskId(taskId);
        // 科室id
        addSampleItemEntity.setTechnicistId(teamId);
        // 委托单id
        addSampleItemEntity.setEntrustId(entrustId);
        EntrustAddVo entrustAddVo = entityMapper.selectByKeyId(entrustId);
        // 进行新建任务单信息、补充信息
        TaskVo entity = new TaskVo();
        // 委托单信息
        entity.setEntrustmentId(entrustId);
        // 设置折扣
        entity.setDiscount(Double.parseDouble(entrustAddVo.getDiscount()));
        //计算本单价格
        double taskPrice = 0L;
        for (SampleItemEntity vo : itemList) {
            taskPrice = taskPrice + ((entity.getDiscount() == null ? 0 : entity.getDiscount()) *
                    (vo.getUnitPrice() == null ? 0 : vo.getUnitPrice()) * vo.getTimes());
        }
        addSampleItemEntity.setItemIds(itemIdList);
        // 更新taskId 价格
        TaskTestEntity taskTestEntity = new TaskTestEntity();
        taskTestEntity.setId(taskId);
        taskTestEntity.setTaskPrice(taskPrice);
        // 旧领样人不相等的话  领样人 更新。
        if (!taskProgressVo.getSampler().equals(taskTestEntity.getSampler())) {
            taskTestEntity.setSampler(sampler);
        }
        // 补充人员信息 检测人
        taskTestEntity.setInspector(methodRetrunStrSplit(addSampleItemEntity.getInspector(), ","));
        // 记录人
        taskTestEntity.setRecorder(methodRetrunStrSplit(addSampleItemEntity.getRecorder(), ","));
        // 复核人
        taskTestEntity.setReviewer(methodRetrunStrSplit(addSampleItemEntity.getReviewer(), ","));
        // 报告制作人
        taskTestEntity.setReportProducer(methodRetrunStrSplit(addSampleItemEntity.getReportProducer(), ","));
        // 辅助人员
        taskTestEntity.setAuxiliaryPersonnel(methodRetrunStrSplit(addSampleItemEntity.getAuxiliaryPersonnel(), ","));
        // 见习生：实习的新手
        taskTestEntity.setProbationer(methodRetrunStrSplit(addSampleItemEntity.getProbationer(), ","));
        // 实习生
        taskTestEntity.setInterns(methodRetrunStrSplit(addSampleItemEntity.getInterns(), ","));
        // 样品描述信息
        taskTestEntity.setSampleStateDescription(list.get(0).getSampleStateDescription());
        taskMapper.updateTaskEntity(taskTestEntity);
        // 更新检测项及样品流转状态
        updateItemStatus(addSampleItemEntity, list);
    }

    /**
     * 针对记录人、复核人、报告制作人信息 方法读取人员信息
     *
     * @param sampleItemEntity
     * @param inspectorArraysVos
     * @param inspectorRels
     */
    void newMethodForPersonnel(SampleItemEntity sampleItemEntity, List<LabelValueVo> inspectorArraysVos, List<TestCheckItemsTaskRel> inspectorRels) {
        // 获取每组检测项 对应的 （0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生）
        // 读取记录人数据
        if (StringUtils.isNotEmpty(sampleItemEntity.getInspector())) {
            String[] userArrays = sampleItemEntity.getInspector().split(",");
            for (int i = 0; i < userArrays.length; i++) {
                String[] names = userArrays[i].split("&");
                LabelValueVo labelValueVo = new LabelValueVo();
                labelValueVo.setLabel(names[0]);
                labelValueVo.setValue(Long.parseLong(names[1]));
                labelValueVo.setText("0");
                inspectorArraysVos.add(labelValueVo);
            }
        }
        // 读取记录人数据
        if (StringUtils.isNotEmpty(sampleItemEntity.getRecorder())) {
            String[] userArrays = sampleItemEntity.getRecorder().split(",");
            for (int i = 0; i < userArrays.length; i++) {
                String[] names = userArrays[i].split("&");
                LabelValueVo labelValueVo = new LabelValueVo();
                labelValueVo.setLabel(names[0]);
                labelValueVo.setValue(Long.parseLong(names[1]));
                labelValueVo.setText("1");
                inspectorArraysVos.add(labelValueVo);
            }
        }
        // 读取复核人数据
        if (StringUtils.isNotEmpty(sampleItemEntity.getReviewer())) {
            String[] userArrays = sampleItemEntity.getReviewer().split(",");
            for (int i = 0; i < userArrays.length; i++) {
                String[] names = userArrays[i].split("&");
                LabelValueVo labelValueVo = new LabelValueVo();
                labelValueVo.setLabel(names[0]);
                labelValueVo.setValue(Long.parseLong(names[1]));
                labelValueVo.setText("2");
                inspectorArraysVos.add(labelValueVo);
            }
        }
        // 报告制作人
        if (StringUtils.isNotEmpty(sampleItemEntity.getReportProducer())) {
            String[] userArrays = sampleItemEntity.getReportProducer().split(",");
            for (int i = 0; i < userArrays.length; i++) {
                String[] names = userArrays[i].split("&");
                LabelValueVo labelValueVo = new LabelValueVo();
                labelValueVo.setLabel(names[0]);
                labelValueVo.setValue(Long.parseLong(names[1]));
                labelValueVo.setText("3");
                inspectorArraysVos.add(labelValueVo);
            }
        }
        // 辅助人员
        if (StringUtils.isNotEmpty(sampleItemEntity.getAuxiliaryPersonnel())) {
            String[] userArrays = sampleItemEntity.getAuxiliaryPersonnel().split(",");
            for (int i = 0; i < userArrays.length; i++) {
                String[] names = userArrays[i].split("&");
                LabelValueVo labelValueVo = new LabelValueVo();
                labelValueVo.setLabel(names[0]);
                labelValueVo.setValue(Long.parseLong(names[1]));
                labelValueVo.setText("4");
                inspectorArraysVos.add(labelValueVo);
            }
        }
        // 5、见习生：实习的新手
        if (StringUtils.isNotEmpty(sampleItemEntity.getProbationer())) {
            String[] userArrays = sampleItemEntity.getProbationer().split(",");
            for (int i = 0; i < userArrays.length; i++) {
                String[] names = userArrays[i].split("&");
                LabelValueVo labelValueVo = new LabelValueVo();
                labelValueVo.setLabel(names[0]);
                labelValueVo.setValue(Long.parseLong(names[1]));
                labelValueVo.setText("5");
                inspectorArraysVos.add(labelValueVo);
            }
        }
        // 6、实习生
        if (StringUtils.isNotEmpty(sampleItemEntity.getInterns())) {
            String[] userArrays = sampleItemEntity.getInterns().split(",");
            for (int i = 0; i < userArrays.length; i++) {
                String[] names = userArrays[i].split("&");
                LabelValueVo labelValueVo = new LabelValueVo();
                labelValueVo.setLabel(names[0]);
                labelValueVo.setValue(Long.parseLong(names[1]));
                labelValueVo.setText("6");
                inspectorArraysVos.add(labelValueVo);
            }
        }
        // 循环读取 每组检测项
        for (LabelValueVo labelValueVo : inspectorArraysVos) {
            TestCheckItemsTaskRel data = new TestCheckItemsTaskRel();
            data.setUserName(labelValueVo.getLabel());
            data.setUserId(labelValueVo.getValue().toString());
            data.setUserType(Integer.parseInt(labelValueVo.getText()));
            inspectorRels.add(data);
        }
    }

    @Override
    public Result testDetectionTasks(Long taskId, List<Integer> items, Integer type) {
        TaskTestEntity taskData = taskMapper.selectTaskEntity(taskId);
        if (taskData == null) {
            return ResultUtil.error("任务单不存在");
        }
        if (taskData.getTaskListStatus() == null) {
            return ResultUtil.success("请继续操作");
        }
        // 查询检测对应的信息
        LambdaQueryWrapper<TestCheckItemsTaskRel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TestCheckItemsTaskRel::getItemId, items);
        queryWrapper.eq(TestCheckItemsTaskRel::getUserType, type);
        List<TestCheckItemsTaskRel> list = testCheckItemsTaskRelMapper.selectList(queryWrapper);
        // 登录人
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (CollectionUtil.isNotEmpty(list)) {
            for (TestCheckItemsTaskRel taskRel : list) {
                if (taskRel.getUserId().equals(String.valueOf(userInfo.getUserId()))) {
                    return ResultUtil.success("请继续操作");
                }
            }
        }
        return ResultUtil.error("当前用户不能操作");

    }

    /**
     * 替换化学室单子
     *
     * @param taskOrderWorkingHoursList
     */
//    @Transactional(rollbackFor = Exception.class)
//    public void replaceTaskOrderWorkingHours(List<TestTaskOrderWorkingHours> taskOrderWorkingHoursList) {
//
//        BigDecimal workingHours = new BigDecimal("0");
//        BigDecimal proportion = new BigDecimal("0");
//        for (TestTaskOrderWorkingHours data : taskOrderWorkingHoursList) {
//
//            data.setAddOperator("杨波&1654681210551117");
//            data.setSource("杨波");
//            // 当前数据为 签发人 && 当前人 不是 杨波 则新增数据
//            if (data.getDetectionType().equals("签发人") && data.getUserId() != 1654681210551117L) {
//                // 新增数据
//                TestTaskOrderWorkingHours taskOrderWorkingHours = new TestTaskOrderWorkingHours(data);
//
//                // 0、
//                taskOrderWorkingHours.setUserName("杨波");
//                taskOrderWorkingHours.setUserId(1654681210551117L);
//                taskOrderWorkingHours.setDetectionType("签发人");
//                taskOrderWorkingHours.setId(null);
//                // 调用方法： 对每组检测项的人员信息进行新增。
//                testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);
//                workingHours = new BigDecimal(data.getWorkingHours());
//                proportion = new BigDecimal(data.getProportion());
//                // 更新
//                data.setProportion("0");
//                data.setWorkingHours("0");
//            }
//            // 更新
//            testTaskOrderWorkingHoursMapper.updateByPrimaryKeySelective(data);
//        }
//        // 判断杨波在 则新增 不在 则更细
//    }
    @Transactional(rollbackFor = Exception.class)
    public void addTaskOrderWorkingHours(TestTaskOrderWorkingHours taskOrderWorkingHours) {

        taskOrderWorkingHours.setSource("代海燕");
        taskOrderWorkingHours.setAddOperator("代海燕&1650006443416157");


        // 0、
        taskOrderWorkingHours.setUserName("代海燕");
        taskOrderWorkingHours.setUserId(1650006443416157L);
        taskOrderWorkingHours.setDetectionType("签发人");
        taskOrderWorkingHours.setProportion("15");
        taskOrderWorkingHours.setId(null);
        // 调用方法： 对每组检测项的人员信息进行新增。
        testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);
        // 1、
        taskOrderWorkingHours.setUserName("检测人员");
        taskOrderWorkingHours.setUserId(1654681160256116L);
        taskOrderWorkingHours.setDetectionType("孙朋骄");
        taskOrderWorkingHours.setProportion("12.2");
        taskOrderWorkingHours.setId(null);
        // 调用方法： 对每组检测项的人员信息进行新增。
        testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);
        // 2、
        taskOrderWorkingHours.setUserName("林海娟");
        taskOrderWorkingHours.setUserId(4575892253571035L);
        taskOrderWorkingHours.setDetectionType("检测人员、记录人员");
        taskOrderWorkingHours.setProportion("12.2");
        taskOrderWorkingHours.setId(null);
        // 调用方法： 对每组检测项的人员信息进行新增。
        testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);

        // 3、
        taskOrderWorkingHours.setUserName("孙含玉");
        taskOrderWorkingHours.setUserId(1654680782486113L);
        taskOrderWorkingHours.setDetectionType("检测人员");
        taskOrderWorkingHours.setProportion("12.2");
        taskOrderWorkingHours.setId(null);
        // 调用方法： 对每组检测项的人员信息进行新增。
        testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);
        // 4、
        taskOrderWorkingHours.setUserName("申永伟");
        taskOrderWorkingHours.setUserId(1654682129126105L);
        taskOrderWorkingHours.setDetectionType("复核人");
        taskOrderWorkingHours.setProportion("14");
        taskOrderWorkingHours.setId(null);
        // 调用方法： 对每组检测项的人员信息进行新增。
        testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);
        // 5、
        taskOrderWorkingHours.setUserName("马瑞玲");
        taskOrderWorkingHours.setUserId(1652404746060112L);
        taskOrderWorkingHours.setDetectionType("检测人员、报告制作人");
        taskOrderWorkingHours.setProportion("12.2");
        taskOrderWorkingHours.setId(null);
        // 调用方法： 对每组检测项的人员信息进行新增。
        testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);
        // 6、
        taskOrderWorkingHours.setUserName("焦凯");
        taskOrderWorkingHours.setUserId(1654682057588104L);
        taskOrderWorkingHours.setDetectionType("辅助人员");
        taskOrderWorkingHours.setProportion("10.0");
        taskOrderWorkingHours.setId(null);
        // 调用方法： 对每组检测项的人员信息进行新增。
        testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);

        // 6、
        taskOrderWorkingHours.setUserName("焦凯");
        taskOrderWorkingHours.setUserId(1654680716455112L);
        taskOrderWorkingHours.setDetectionType("检测人员");
        taskOrderWorkingHours.setProportion("12.2");
        taskOrderWorkingHours.setId(null);
        // 调用方法： 对每组检测项的人员信息进行新增。
        testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);
    }

    /**
     * 整理map 数据
     *
     * @param taskOrderWorkingHours
     */
    @Transactional(rollbackFor = Exception.class)
    public List<TestTaskOrderWorkingHours> addTaskOrderWorkingHours1(TestTaskOrderWorkingHours taskOrderWorkingHours) {

        List<TestTaskOrderWorkingHours> zongTaskWorkingHorusList = new ArrayList<>();

/*//        // 0、
        taskOrderWorkingHours.setUserName("杨波");
        taskOrderWorkingHours.setUserId(1654681210551117L);
        taskOrderWorkingHours.setDetectionType("签发人");
        taskOrderWorkingHours.setProportion("15");
        taskOrderWorkingHours.setId(null);
        TestTaskOrderWorkingHours data0 = new TestTaskOrderWorkingHours(taskOrderWorkingHours);
        zongTaskWorkingHorusList.add(data0);

//        // 1、
        taskOrderWorkingHours.setUserName("刘秋芳");
        taskOrderWorkingHours.setUserId(1654681099998115L);
        taskOrderWorkingHours.setDetectionType("检测人员、记录人员");
        taskOrderWorkingHours.setProportion("14.5");
        taskOrderWorkingHours.setId(null);
        TestTaskOrderWorkingHours data1 = new TestTaskOrderWorkingHours(taskOrderWorkingHours);
        zongTaskWorkingHorusList.add(data1);

//        // 2、
        taskOrderWorkingHours.setUserName("于胜楠");
        taskOrderWorkingHours.setUserId(4739625864645518L);
        taskOrderWorkingHours.setDetectionType("报告制作人");
        taskOrderWorkingHours.setProportion("14");
        taskOrderWorkingHours.setId(null);
        TestTaskOrderWorkingHours data2 = new TestTaskOrderWorkingHours(taskOrderWorkingHours);
        zongTaskWorkingHorusList.add(data2);

//        // 3、
        taskOrderWorkingHours.setUserName("王巧利");
        taskOrderWorkingHours.setUserId(4573649232290487L);
        taskOrderWorkingHours.setDetectionType("检测人员");
        taskOrderWorkingHours.setProportion("14.5");
        taskOrderWorkingHours.setId(null);
        TestTaskOrderWorkingHours data3 = new TestTaskOrderWorkingHours(taskOrderWorkingHours);
        zongTaskWorkingHorusList.add(data3);

//        // 4、
        taskOrderWorkingHours.setUserName("高贯奇");
        taskOrderWorkingHours.setUserId(1652365586151107L);
        taskOrderWorkingHours.setDetectionType("检测人员");
        taskOrderWorkingHours.setProportion("14");
        taskOrderWorkingHours.setId(null);
        TestTaskOrderWorkingHours data4 = new TestTaskOrderWorkingHours(taskOrderWorkingHours);
        zongTaskWorkingHorusList.add(data4);

//        // 5、
        taskOrderWorkingHours.setUserName("崔路伟");
        taskOrderWorkingHours.setUserId(1654682006912103L);
        taskOrderWorkingHours.setDetectionType("辅助人员");
        taskOrderWorkingHours.setProportion("14");
        taskOrderWorkingHours.setId(null);
        TestTaskOrderWorkingHours data5 = new TestTaskOrderWorkingHours(taskOrderWorkingHours);
        zongTaskWorkingHorusList.add(data5);

//        // 6、
        taskOrderWorkingHours.setUserName("邢洁");
        taskOrderWorkingHours.setUserId(1654682170405106L);
        taskOrderWorkingHours.setDetectionType("复核人");
        taskOrderWorkingHours.setProportion("14");
        taskOrderWorkingHours.setId(null);
        TestTaskOrderWorkingHours data6 = new TestTaskOrderWorkingHours(taskOrderWorkingHours);
        zongTaskWorkingHorusList.add(data6);*/


//        韩笑团队 // 0、
        taskOrderWorkingHours.setUserName("韩笑");
        taskOrderWorkingHours.setUserId(4588250852745216L);
        taskOrderWorkingHours.setDetectionType("签发人");
        taskOrderWorkingHours.setProportion("11.11");
        taskOrderWorkingHours.setId(null);
        TestTaskOrderWorkingHours data0 = new TestTaskOrderWorkingHours(taskOrderWorkingHours);
        zongTaskWorkingHorusList.add(data0);

//        // 1、
        taskOrderWorkingHours.setUserName("卢俊龙");
        taskOrderWorkingHours.setUserId(1652360733506103L);
        taskOrderWorkingHours.setDetectionType("复核人");
        taskOrderWorkingHours.setProportion("18.52");
        taskOrderWorkingHours.setId(null);
        TestTaskOrderWorkingHours data1 = new TestTaskOrderWorkingHours(taskOrderWorkingHours);
        zongTaskWorkingHorusList.add(data1);

//        // 2、
        taskOrderWorkingHours.setUserName("王亚玲");
        taskOrderWorkingHours.setUserId(1652365523132106L);
        taskOrderWorkingHours.setDetectionType("报告制作人");
        taskOrderWorkingHours.setProportion("17");
        taskOrderWorkingHours.setId(null);
        TestTaskOrderWorkingHours data2 = new TestTaskOrderWorkingHours(taskOrderWorkingHours);
        zongTaskWorkingHorusList.add(data2);

//        // 3、
        taskOrderWorkingHours.setUserName("雷梦飞");
        taskOrderWorkingHours.setUserId(1654682241625107L);
        taskOrderWorkingHours.setDetectionType("检测人员");
        taskOrderWorkingHours.setProportion("16.09");
        taskOrderWorkingHours.setId(null);
        TestTaskOrderWorkingHours data3 = new TestTaskOrderWorkingHours(taskOrderWorkingHours);
        zongTaskWorkingHorusList.add(data3);

//        // 4、
        taskOrderWorkingHours.setUserName("周朋可");
        taskOrderWorkingHours.setUserId(1654683761046119L);
        taskOrderWorkingHours.setDetectionType("记录人");
        taskOrderWorkingHours.setProportion("13.66");
        taskOrderWorkingHours.setId(null);
        TestTaskOrderWorkingHours data4 = new TestTaskOrderWorkingHours(taskOrderWorkingHours);
        zongTaskWorkingHorusList.add(data4);

//        // 5、
        taskOrderWorkingHours.setUserName("张朋涛");
        taskOrderWorkingHours.setUserId(1654683026922108L);
        taskOrderWorkingHours.setDetectionType("报告制作");
        taskOrderWorkingHours.setProportion("11.81");
        taskOrderWorkingHours.setId(null);
        TestTaskOrderWorkingHours data5 = new TestTaskOrderWorkingHours(taskOrderWorkingHours);
        zongTaskWorkingHorusList.add(data5);

//        // 6、
        taskOrderWorkingHours.setUserName("刘子浩");
        taskOrderWorkingHours.setUserId(4689827516827022L);
        taskOrderWorkingHours.setDetectionType("辅助人员");
        taskOrderWorkingHours.setProportion("11.81");
        taskOrderWorkingHours.setId(null);
        TestTaskOrderWorkingHours data6 = new TestTaskOrderWorkingHours(taskOrderWorkingHours);
        zongTaskWorkingHorusList.add(data6);

        return zongTaskWorkingHorusList;

//        // 0、
//        taskOrderWorkingHours.setUserName("孙含玉");
//        taskOrderWorkingHours.setUserId(1654680782486113L);
//        taskOrderWorkingHours.setDetectionType("检测人员");
//        taskOrderWorkingHours.setProportion("12.5");
//        taskOrderWorkingHours.setId(null);
//        // 调用方法： 对每组检测项的人员信息进行新增。
//        testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);
//        // 1、
//        taskOrderWorkingHours.setUserName("李小华");
//        taskOrderWorkingHours.setUserId(1654680716455112L);
//        taskOrderWorkingHours.setDetectionType("复核人");
//        taskOrderWorkingHours.setProportion("12.5");
//        taskOrderWorkingHours.setId(null);
//        // 调用方法： 对每组检测项的人员信息进行新增。
//        testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);
//        // 2、
//        taskOrderWorkingHours.setUserName("严建行");
//        taskOrderWorkingHours.setUserId(1655455410514153L);
//        taskOrderWorkingHours.setDetectionType("检测人员");
//        taskOrderWorkingHours.setProportion("12.5");
//        taskOrderWorkingHours.setId(null);
//        // 调用方法： 对每组检测项的人员信息进行新增。
//        testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);
//        // 3、
//        taskOrderWorkingHours.setUserName("杨永祥");
//        taskOrderWorkingHours.setUserId(1655455951944156L);
//        taskOrderWorkingHours.setDetectionType("检测人员");
//        taskOrderWorkingHours.setProportion("12.5");
//        taskOrderWorkingHours.setId(null);
//        // 调用方法： 对每组检测项的人员信息进行新增。
//        testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);
//        // 4、
//        taskOrderWorkingHours.setUserName("刘昊天");
//        taskOrderWorkingHours.setUserId(4689457230074278L);
//        taskOrderWorkingHours.setDetectionType("辅助人员");
//        taskOrderWorkingHours.setProportion("8");
//        taskOrderWorkingHours.setId(null);
//        // 调用方法： 对每组检测项的人员信息进行新增。
//        testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);

/*        // 0、
        taskOrderWorkingHours.setUserName("陈红超");
        taskOrderWorkingHours.setUserId(1656403981037103L);
        taskOrderWorkingHours.setDetectionType("检测人员");
        taskOrderWorkingHours.setProportion("0");
        taskOrderWorkingHours.setWorkingHours("0");
        taskOrderWorkingHours.setId(null);
        // 调用方法： 对每组检测项的人员信息进行新增。
        testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);
        // 1、
        taskOrderWorkingHours.setUserName("云莹莹");
        taskOrderWorkingHours.setUserId(1655369344860109L);
        taskOrderWorkingHours.setDetectionType("检测人员");
        taskOrderWorkingHours.setProportion("0");
        taskOrderWorkingHours.setWorkingHours("0");
        taskOrderWorkingHours.setId(null);
        // 调用方法： 对每组检测项的人员信息进行新增。
        testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);*/
        // 2、
//        taskOrderWorkingHours.setUserName("李晓洋");
//        taskOrderWorkingHours.setUserId(1654684210974125L);
//        taskOrderWorkingHours.setDetectionType("检测人员");
//        taskOrderWorkingHours.setProportion("0");
//        taskOrderWorkingHours.setWorkingHours("0");
//        taskOrderWorkingHours.setId(null);
//        // 调用方法： 对每组检测项的人员信息进行新增。
//        testTaskOrderWorkingHoursMapper.insert(taskOrderWorkingHours);

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result informationSubstitution(String taskCode) {

        // 获取 任务单信息：
        TaskVo taskDetailInfo = taskMapper.selectTaskOneDetails(taskCode);
        if (taskDetailInfo == null) {
            System.out.println("查询失败 " + " 不存在 " + taskCode);
            return ResultUtil.error("查询失败 " + " 不存在 " + null);
        }
        // 获取 任务单工时信息：
        LambdaQueryWrapper<TestTaskOrderWorkingHours> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestTaskOrderWorkingHours::getTaskId, taskDetailInfo.getId());
        List<TestTaskOrderWorkingHours> latestWorkingHoursList = testTaskOrderWorkingHoursMapper.selectList(queryWrapper);
        Map<String, TestTaskOrderWorkingHours> taskWorkingHorusMap = new HashMap<>();

        if (CollectionUtil.isEmpty(latestWorkingHoursList)) {
            return null;
        }


        // 整理 当前任务单所属工时信息
        for (TestTaskOrderWorkingHours data : latestWorkingHoursList) {
            taskWorkingHorusMap.put(data.getUserName(), data);
        }


//        // 获取一条数据
        TestTaskOrderWorkingHours taskOrderWorkingHours = latestWorkingHoursList.get(0);
        TestTaskOrderWorkingHours data66 = new TestTaskOrderWorkingHours(taskOrderWorkingHours);
//        // 整理map 数据 : 获取当前任务单 标准工时信息
        List<TestTaskOrderWorkingHours> list1 = addTaskOrderWorkingHours1(data66);
//
        // 进行遍历比较 map
        for (TestTaskOrderWorkingHours data : list1) {
            if (data != null && taskWorkingHorusMap.get(data.getUserName()) != null) {
                TestTaskOrderWorkingHours data1 = taskWorkingHorusMap.get(data.getUserName());
                // 包含当前用户 工时不相等 则进行更新操作
                if (!data1.getProportion().equals(data.getProportion())) {
                    LambdaUpdateWrapper<TestTaskOrderWorkingHours> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.set(TestTaskOrderWorkingHours::getProportion, data.getProportion());
                    updateWrapper.eq(TestTaskOrderWorkingHours::getId, data1.getId());
                    testTaskOrderWorkingHoursMapper.update(null, updateWrapper);
                }
            } else {
                // 数据为空 则进行新增即可
                // 调用方法： 对每组检测项的人员信息进行新增。
                testTaskOrderWorkingHoursMapper.insert(data);
            }
        }

//        taskOrderWorkingHours.setUserName("李路遥");
//        taskOrderWorkingHours.setUserId(1652407272842124L);
//        taskOrderWorkingHours.setDetectionType("签发人");
//        taskOrderWorkingHours.setProportion("0");
//        taskOrderWorkingHours.setWorkingHours("0");
//        taskOrderWorkingHours.setId(null);
//        TestTaskOrderWorkingHours data0 = new TestTaskOrderWorkingHours(taskOrderWorkingHours);
//        if (taskWorkingHorusMap.get(data0.getUserName()) == null) {
//            // 调用方法： 对每组检测项的人员信息进行新增。
//            testTaskOrderWorkingHoursMapper.insert(data0);
//        }
//        taskOrderWorkingHours.setUserName("胡金龙");
//        taskOrderWorkingHours.setUserId(1652405368958116L);
//        taskOrderWorkingHours.setDetectionType("签发人");
//        taskOrderWorkingHours.setProportion("0");
//        taskOrderWorkingHours.setWorkingHours("0");
//        taskOrderWorkingHours.setId(null);
//        TestTaskOrderWorkingHours data1 = new TestTaskOrderWorkingHours(taskOrderWorkingHours);
//        if (taskWorkingHorusMap.get(data1.getUserName()) == null) {
//            // 调用方法： 对每组检测项的人员信息进行新增。
//            testTaskOrderWorkingHoursMapper.insert(data1);
//        }
//                taskOrderWorkingHours.setUserName("杨波");
//        taskOrderWorkingHours.setUserId(1654681210551117L);
//        taskOrderWorkingHours.setDetectionType("签发人");
//        taskOrderWorkingHours.setProportion("0");
//        taskOrderWorkingHours.setWorkingHours("0");
//        taskOrderWorkingHours.setId(null);
//        TestTaskOrderWorkingHours data3 = new TestTaskOrderWorkingHours(taskOrderWorkingHours);
//        if (taskWorkingHorusMap.get(data3.getUserName()) == null) {
//            // 调用方法： 对每组检测项的人员信息进行新增。
//            testTaskOrderWorkingHoursMapper.insert(data3);
//        }
        return null;

/*        // 获取 任务单详情：
        TaskTestEntity taskTestData = taskMapper.selectTaskEntity(taskDetailInfo.getId());

        // 根据 taskId
        // 更新taskId 价格
        TaskTestEntity taskTestEntity = new TaskTestEntity();


        if (StringUtils.isNotEmpty(taskTestData.getWorkingHoursId())) {
            taskTestEntity.setWorkingHoursId(taskTestData.getWorkingHoursId());
        }


        // 任务单替换：检测人：孙朋骄&1654681160256116,孙含玉&1654680782486113,
        // 李小华&1654680716455112,马瑞玲&1652404746060112,林海娟&4575892253571035

        //记录：林海娟&4575892253571035

        //复核：申永伟&1654682129126105

        //制作：马瑞玲&1652404746060112

        //辅助人员：焦凯&1654682057588104


        taskTestEntity.setId(taskTestData.getId());
        taskTestEntity.setTaskPrice(taskTestData.getTaskPrice());
        taskTestEntity.setSampler(taskTestData.getSampler());

        // 补充人员信息 检测人
        taskTestEntity.setInspector("孙朋骄&1654681160256116,孙含玉&1654680782486113,李小华&1654680716455112,马瑞玲&1652404746060112,林海娟&4575892253571035");
//        taskTestEntity.setInspector("刘秋芳&1654681160256116,王巧利&1654680782486113,高贯奇&1654680716455112");
        // 记录人
        taskTestEntity.setRecorder("林海娟&4575892253571035");
        // 复核人
        taskTestEntity.setReviewer("申永伟&1654682129126105");
        // 报告制作人
        taskTestEntity.setReportProducer("马瑞玲&1652404746060112");
        // 辅助人员
        taskTestEntity.setAuxiliaryPersonnel("焦凯&1654682057588104");
//        if (!taskDetailInfo.getReceiver().equals("1654681210551117")) {
//            return null;
//        }
//        // 签发人
//        taskDetailInfo.setReceiver("1654681210551117");
        *//**
         * 试验刘秋芳，王巧利，高贯奇，记录刘秋芳，王巧利，复核，邢洁，制作于胜楠，辅助崔路伟，签发杨波&1654681210551117
         *//*
        // 补充人员信息 检测人
//        taskTestEntity.setInspector("刘秋芳&1654681099998115,王巧利&4573649232290487,高贯奇&1652365586151107");
//        // 记录人
//        taskTestEntity.setRecorder("刘秋芳&1654681099998115,王巧利&4573649232290487");
//        // 复核人
//        taskTestEntity.setReviewer("邢洁&1654682170405106");
//        // 报告制作人
//        taskTestEntity.setReportProducer("于胜楠&4739625864645518");
//        // 辅助人员
//        taskTestEntity.setAuxiliaryPersonnel("崔路伟&1654682006912103");

        taskMapper.updateTaskEntity(taskTestEntity);


        LambdaQueryWrapper<TestCheckItemsTaskRel> queryWrapper12 = new LambdaQueryWrapper<>();
        queryWrapper12.or().eq(TestCheckItemsTaskRel::getTaskId, taskDetailInfo.getId());
        queryWrapper12.last("GROUP BY item_id");
        // 获取检测项信息
        List<TestCheckItemsTaskRel> itemList = testCheckItemsTaskRelMapper.selectList(queryWrapper12);

        // 进行删除
        LambdaQueryWrapper<TestCheckItemsTaskRel> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(TestCheckItemsTaskRel::getTaskId, taskDetailInfo.getId());
        testCheckItemsTaskRelMapper.delete(deleteWrapper);

        if (CollectionUtil.isNotEmpty(itemList)) {


            // 0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生

            for (TestCheckItemsTaskRel taskRel : itemList) {
                // 进行新增
                String[] inspectorArrays = taskTestEntity.getInspector().split(",");
                for (int i = 0; i < inspectorArrays.length; i++) {
                    String[] inspectorSet = inspectorArrays[i].split("&");
                    taskRel.setUserType(0);
                    taskRel.setUserName(inspectorSet[0]);
                    taskRel.setUserId(inspectorSet[1]);
                    taskRel.setId(null);
                    // 调用方法： 对每组检测项的人员信息进行新增。
                    testCheckItemsTaskRelMapper.insert(taskRel);
                }
                // 进行新增 记录人
                String[] recorderArrays = taskTestEntity.getRecorder().split(",");
                for (int i = 0; i < recorderArrays.length; i++) {
                    String[] inspectorSet = recorderArrays[i].split("&");
                    taskRel.setUserType(1);
                    taskRel.setUserName(inspectorSet[0]);
                    taskRel.setUserId(inspectorSet[1]);
                    taskRel.setId(null);
                    // 调用方法： 对每组检测项的人员信息进行新增。
                    testCheckItemsTaskRelMapper.insert(taskRel);
                }

                // 进行新增 复核人
                String[] receiverArrays = taskTestEntity.getReviewer().split("&");
                taskRel.setUserType(2);
                taskRel.setUserName(receiverArrays[0]);
                taskRel.setUserId(receiverArrays[1]);
                taskRel.setId(null);
                // 调用方法： 对每组检测项的人员信息进行新增。
                testCheckItemsTaskRelMapper.insert(taskRel);

                // 进行新增 报告制作人
                String[] reportProducerArrays = taskTestEntity.getReportProducer().split("&");
                taskRel.setUserType(3);
                taskRel.setUserName(reportProducerArrays[0]);
                taskRel.setUserId(reportProducerArrays[1]);
                taskRel.setId(null);
                // 调用方法： 对每组检测项的人员信息进行新增。
                testCheckItemsTaskRelMapper.insert(taskRel);

                // 进行新增 辅助人
                String[] auxiliaryPersonnelArrays = taskTestEntity.getAuxiliaryPersonnel().split("&");
                taskRel.setUserType(4);
                taskRel.setUserName(auxiliaryPersonnelArrays[0]);
                taskRel.setUserId(auxiliaryPersonnelArrays[1]);
                taskRel.setId(null);
                // 调用方法： 对每组检测项的人员信息进行新增。
                testCheckItemsTaskRelMapper.insert(taskRel);

            }
        }

        // 获取 任务单工时信息：
        LambdaQueryWrapper<TestTaskOrderWorkingHours> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestTaskOrderWorkingHours::getTaskId, taskDetailInfo.getId());
        List<TestTaskOrderWorkingHours> latestWorkingHoursList = testTaskOrderWorkingHoursMapper.selectList(queryWrapper);


        if (CollectionUtil.isEmpty(latestWorkingHoursList)) {
            return null;
        }
        // 执行 替换任务单授权签字人
//        replaceTaskOrderWorkingHours(latestWorkingHoursList);
//        // 获取一条数据
        TestTaskOrderWorkingHours taskOrderWorkingHours = latestWorkingHoursList.get(0);
//        // 其余执行删除
        testTaskOrderWorkingHoursMapper.delete(queryWrapper);
////        // 执行新增
        addTaskOrderWorkingHours(taskOrderWorkingHours);*/

//        return null;
    }
}
