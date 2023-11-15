package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.*;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.TestTaskPoolService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.*;

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
        // 3、 查看检测项及所属类型：
        //      （0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生）
        // 3.1：通过委托单id 查看检测项列表。
        List<SampleItemEntity> itemList = taskPoolMapper.selectItems(entrustId);
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
                        methodForEachTaskHallDetails(sampleItemEntity, itemsTaskRels1);
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
                // 遍历检测项数据 存放至 样品中
                if (CollectionUtil.isNotEmpty(itemList)) {
                    for (SampleItemEntity sampleItemEntity : itemList) {
                        if (sampleItemEntity.getSampleId().equals(sampleEntity.getId())) {
                            sampleItemEntities.add(sampleItemEntity);
                        }
                    }
                }
                sampleEntity.setSampleCheckItem(sampleItemEntities);
                // 样品外观描述 不为null
                sampleEntity.setOutwardDescribe(sampleEntity.getOutwardDescribe() == null ? "-" : sampleEntity.getOutwardDescribe());
            }
        }
        JSONObject jsonObject = new JSONObject();
        // 流水号任务单信息
        jsonObject.put("testTaskPool", detailedData);
        // 样品信息
        jsonObject.put("samples", sampleList);
        // 领样人
        if (CollectionUtil.isNotEmpty(taskProgressVos)) {
            jsonObject.put("sampler", taskProgressVos.get(0).getSampler());
        } else {
            jsonObject.put("sampler", null);
        }
        return ResultUtil.success(jsonObject);
    }

    private void methodForEachTaskHallDetails(SampleItemEntity sampleItemEntity, List<TestCheckItemsTaskRel> itemsTaskRels) {
        // （0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生）
        // 检测人
        StringBuffer inspectorBuffer = new StringBuffer();
        // 记录人
        StringBuffer recorderBuffer = new StringBuffer();
        // 复核人
        StringBuffer reviewerBuffer = new StringBuffer();
        // 报告制作人
        StringBuffer reportProducerBuffer = new StringBuffer();
        // 辅助人员
        StringBuffer auxiliaryPersonnelBuffer = new StringBuffer();
        // 见习生：实习的新手
        StringBuffer probationerBuffer = new StringBuffer();
        // 实习生
        StringBuffer internsBuffer = new StringBuffer();
        for (TestCheckItemsTaskRel testCheckItemsTaskRel : itemsTaskRels) {
            switch (testCheckItemsTaskRel.getUserType()) {
                case 0:
                    inspectorBuffer.append(testCheckItemsTaskRel.getUserName() + "&" + testCheckItemsTaskRel.getUserId() + ",");
                    break;
                case 1:
                    recorderBuffer.append(testCheckItemsTaskRel.getUserName() + "&" + testCheckItemsTaskRel.getUserId() + ",");
                    break;
                case 2:
                    reviewerBuffer.append(testCheckItemsTaskRel.getUserName() + "&" + testCheckItemsTaskRel.getUserId() + ",");
                    break;
                case 3:
                    reportProducerBuffer.append(testCheckItemsTaskRel.getUserName() + "&" + testCheckItemsTaskRel.getUserId() + ",");
                    break;
                case 4:
                    auxiliaryPersonnelBuffer.append(testCheckItemsTaskRel.getUserName() + "&" + testCheckItemsTaskRel.getUserId() + ",");
                    break;
                case 5:
                    probationerBuffer.append(testCheckItemsTaskRel.getUserName() + "&" + testCheckItemsTaskRel.getUserId() + ",");
                    break;
                case 6:
                    internsBuffer.append(testCheckItemsTaskRel.getUserName() + "&" + testCheckItemsTaskRel.getUserId() + ",");
                    break;
                default:
                    break;
            }
        }
        if (inspectorBuffer.length() >= 1) {
            sampleItemEntity.setInspector(inspectorBuffer.deleteCharAt(inspectorBuffer.length() - 1).toString());
        }
        if (recorderBuffer.length() >= 1) {
            sampleItemEntity.setRecorder(recorderBuffer.deleteCharAt(recorderBuffer.length() - 1).toString());
        }
        if (reviewerBuffer.length() >= 1) {
            sampleItemEntity.setReviewer(reviewerBuffer.deleteCharAt(reviewerBuffer.length() - 1).toString());
        }
        if (reportProducerBuffer.length() >= 1) {
            sampleItemEntity.setReportProducer(reportProducerBuffer.deleteCharAt(reportProducerBuffer.length() - 1).toString());
        }
        if (auxiliaryPersonnelBuffer.length() >= 1) {
            sampleItemEntity.setAuxiliaryPersonnel(auxiliaryPersonnelBuffer.deleteCharAt(auxiliaryPersonnelBuffer.length() - 1).toString());
        }
        if (probationerBuffer.length() >= 1) {
            sampleItemEntity.setProbationer(probationerBuffer.deleteCharAt(probationerBuffer.length() - 1).toString());
        }
        if (internsBuffer.length() >= 1) {
            sampleItemEntity.setInterns(internsBuffer.deleteCharAt(internsBuffer.length() - 1).toString());
        }
    }

    /**
     * 任务大厅 领取任务单
     *
     * @param list
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Result addTaskCollection(List<SampleItemEntity> list) {
        // 通过检测项主键 获取 委托单id
        Long entrustId = taskPoolMapper.selectEntrustmentId(list.get(0).getItemIds().get(0));
        if (entrustId == null) {
            return ResultUtil.error("数据异常、委托单不存在");
        }
        // 登录人
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        // 查询当前登录人 是否为 授权角色
        List<SysRoleEntity> roleList = sysRoleDao.selectSysRoleByUserId(userInfo.getUserId());
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
        // 通过委托单id 查询旧任务列表
        List<TaskProgressVo> taskProgressVos = taskMapper.getTaskStateByEntrustId(entrustId);
        // 通过委托单id 查看检测项列表。
        List<SampleItemEntity> itemList = taskPoolMapper.selectItems(entrustId);
        // 通过委托单 获取流水任务单详情
        LambdaQueryWrapper<TestTaskPool> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestTaskPool::getEntrustmentId, entrustId);
        TestTaskPool testTaskPool = taskPoolMapper.selectOne(queryWrapper);
        if (testTaskPool == null) {
            return ResultUtil.error("领取失败： 当前流水号任务单不存在");
        }
        // 任务领取-循环验证 数据 msg = null 正常执行、！= null 抛出。
        Result msg = loopValidation(list, taskProgressVos);
        if (msg != null) {
            return msg;
        }
        // 获取每组检测项 对应的 （0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生）
        // 根据科室id 及 委托单主键 查询任务单是否存在？
        //                         1、任务单存在 查看状态=试验开始 则返回错误信息 状态=未开始试验的话，最后删除任务单操作
        //                         2、 任务单不存在的话，创建任务单即可。
        //调用方法： 补充检测项信息并发布任务单
        // key = deptId value = 旧单号
        Map<Long, TaskProgressVo> taskCodeMap = new HashMap<>();
        // 1.1：任务单列表存在的话
        if (CollectionUtil.isNotEmpty(taskProgressVos)) {
            // 删除任务单号
            for (TaskProgressVo taskProgressVo : taskProgressVos) {
                // 根据部门id 保留旧任务单号
                taskCodeMap.put(taskProgressVo.getDeptId().longValue(), taskProgressVo);
                taskMapper.deleteTaskById(taskProgressVo.getTaskId());
            }
        }
        // 根据 委托单id 条件删除流转信息
        LambdaQueryWrapper<TestCheckItemsTaskRel> queryWrapper12 = new LambdaQueryWrapper<>();
        queryWrapper12.eq(TestCheckItemsTaskRel::getEntrustId, entrustId);
        testCheckItemsTaskRelMapper.delete(queryWrapper12);
        // 2、进行录入任务单信息
        for (SampleItemEntity sampleItemEntity : list) {
            String sampler = sampleItemEntity.getSampler().split("-")[0];
            methodPublishTaskList(entrustId, sampler, sampleItemEntity, itemList, testTaskPool.getId().longValue(), taskCodeMap);
        }
        // 调用方法 进行 更新流水号任务单信息
        methodUpdateTaskPool(entrustId, testTaskPool, userInfo);
        return ResultUtil.success("领取成功");
    }

    /**
     * 任务领取-循环验证 数据
     *
     * @param list            任务单领取数据值
     * @param taskProgressVos 通过委托单id 查询旧任务列表
     * @return = null 正常执行，否则抛出异常值。
     */
    public Result loopValidation(List<SampleItemEntity> list, List<TaskProgressVo> taskProgressVos) {
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
        for (TaskProgressVo taskProgressVo : taskProgressVos) {
            if (taskProgressVo.getState() != 144) {
                // 任务单状态： 状态0未抢单，1.已抢单，2已领样,3实验中，4实验完成，5原始记录已上传，6.原始记录已符合，
                if (taskProgressVo.getState() >= 3) {
                    // 1.2 查看状态=试验开始 则返回错误信息
                    return ResultUtil.error("领取失败： 当前任务单 " + taskProgressVo.getTaskCode() + "已开始试验");
                }
            }
        }
        // 参与效验的数据--------------------------------- ↑↑↑↑ ------------------------
        return null;
    }

    void methodUpdateTaskPool(Long entrustId, TestTaskPool testTaskPool, SysUserEntity userInfo) {
        // 领取成功后： 补充流水号任务单信息
        // 通过委托单id 查询任务列表
        List<TaskProgressVo> taskProgress2Vos = taskMapper.getTaskStateByEntrustId(entrustId);
        StringBuffer taskCodeBuffer = new StringBuffer();
        //计算本单价格
        double taskPrice = 0L;
        for (TaskProgressVo taskProgressVo : taskProgress2Vos) {
            // 获取任务单号
            taskCodeBuffer.append(taskProgressVo.getTaskCode() + "&" + taskProgressVo.getTaskId() + ",");
            // 获取价格
            taskPrice += taskProgressVo.getTaskPrice();
        }
        // 任务单编号
        testTaskPool.setTaskCode(taskCodeBuffer.deleteCharAt(taskCodeBuffer.length() - 1).toString());
        // 本单费用
        testTaskPool.setPrice(String.valueOf(taskPrice));
        // 领取人id
        testTaskPool.setReceiveId(userInfo.getUserId());
        // 领取人（当前登陆人员）
        testTaskPool.setReceiveName(userInfo.getName());
        // 领取时间
        testTaskPool.setReceiveDate(new Date());
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
    public Boolean distributionTask412(TaskVo entity, SampleItemEntity sampleItemEntity, Long poolId, Long taskId, EntrustAddVo entrustAddVo, Map<Long, TaskProgressVo> taskCodeMap) {
        List<Long> deptIds = Lists.newArrayList();
        List<CheckItemDeptVo> checkItemDeptVoList = entity.getCheckItemDeptVoList();
        for (CheckItemDeptVo vo : checkItemDeptVoList) {
            if (!deptIds.contains(vo.getDeptId())) {
                deptIds.add(vo.getDeptId());
            }
        }
        //创建任务对象
        List<TaskVo> vos = Lists.newArrayList();
        for (Long deptId : deptIds) {
            //计算本单价格
            double taskPrice = 0L;
            for (CheckItemDeptVo vo : checkItemDeptVoList) {
                if (deptId.equals(vo.getDeptId())) {
                    taskPrice = taskPrice + ((entity.getDiscount() == null ? 0 : entity.getDiscount()) *
                            (vo.getCheckPrice() == null ? 0 : vo.getCheckPrice()) * vo.getTimes());
                }
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
            dingNotifyUtils.OAWorkNotice(dingId, titleBuffer.toString(), publisher);
        }
    }
}
