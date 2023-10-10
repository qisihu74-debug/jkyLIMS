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
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
        System.out.println("任务大厅----展示详情数据");
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
            }
        }
        JSONObject jsonObject = new JSONObject();
        // 流水号任务单信息
        jsonObject.put("testTaskPool", detailedData);
        // 样品信息
        jsonObject.put("samples", sampleList);
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

//    /**
//     * 任务大厅 领取任务单
//     *
//     * @param list
//     * @return
//     */
//    @Override
//    public Result addTaskCollection(List<SampleItemEntity> list) {
//        // 检测人员信息
//        Map<String, Long> userMap = new HashMap<>();
//        // 获取每组检测项 对应的 （0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生）
//        for (SampleItemEntity testCheckItemsTaskRel1 : list) {
//            // 读取检测项数据 生成对应的任务单。
//            for (SampleItemEntity testCheckItemsTaskRel2 : list) {
//                // 查询 检测人所代表的科室信息
//                if (testCheckItemsTaskRel1.getUserType().equals(0)) {
//                    // 检测人员 = key（检测项主键 + "&" + 检测人userId） , value(检测人userId)
//                    userMap.put(testCheckItemsTaskRel1.getItemId() + "&" + testCheckItemsTaskRel1.getUserId(), Long.valueOf(testCheckItemsTaskRel1.getUserId()));
//                }
//                //
//                if (userMap != null) {
//                    //
//                    for (String key : userMap.keySet()) {
//                        // 通过检测人 获取所属团队信息
//                        List<Long> userIds = new ArrayList<>();
//                        userIds.add(userMap.get(key));
//                    }
//                    // 根据人员id 查询所属科室
//
//                }
//                // 通过委托单id 及 任务单号 查看任务单是否存在，是否是试验状态。
//                // 检测项主键相等的情况下
//                if (testCheckItemsTaskRel1.getItemId().equals(testCheckItemsTaskRel2.getItemId())) {
//
//                }
//            }
//        }
//        return null;
//    }

    /**
     * 任务大厅 领取任务单
     *
     * @param list
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addTaskCollection(List<SampleItemEntity> list) {
        // 委托单id 查询任务列表
        Long entrustId = 4656446206622436L;
        List<TaskProgressVo> taskProgressVos = taskMapper.getTaskStateByEntrustId(entrustId);
        // 通过委托单id 查看检测项列表。
        List<SampleItemEntity> itemList = taskPoolMapper.selectItems(entrustId);
        // 1、效验检测人在不在检测科室 2、检测人不能在多个科室 3、补充每组检测项中人员信息
        for (SampleItemEntity sampleItemEntity : list) {
            // 获取每组检测项 对应的 （0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生）
            List<LabelValueVo> inspectorArraysVos = new ArrayList<>();
            List<TestCheckItemsTaskRel> inspectorRels = new ArrayList<>();
            List<Long> userIds = new ArrayList<>();
            // 检测人名
            StringBuffer nameStr = new StringBuffer();
            // 读取检测项人数据 生成对应的任务单。
            if (StringUtils.isNotEmpty(sampleItemEntity.getInspector())) {
                String[] inspectorArrays = sampleItemEntity.getInspector().split(",");
                for (int i = 0; i < inspectorArrays.length; i++) {
                    String[] names = inspectorArrays[i].split("&");
                    LabelValueVo labelValueVo = new LabelValueVo();
                    labelValueVo.setLabel(names[0]);
                    labelValueVo.setValue(Long.parseLong(names[1]));
                    labelValueVo.setText("0");
                    inspectorArraysVos.add(labelValueVo);
                    userIds.add(Long.parseLong(names[1]));
                    nameStr.append(names[0] + " ");
                }
                // 读取检测人信息 是否在同一科室。
                List<Long> detectorsCollection = teamMapper.getUsersByTechnicist(userIds);
                if (CollectionUtils.isEmpty(detectorsCollection)) {
                    return ResultUtil.error("领取失败：" + nameStr + " 检测人不在科室");
                }
                if (detectorsCollection.size() > 2) {
                    return ResultUtil.error("领取失败：" + nameStr + " 检测人所在科室不同");
                }
                // 每组检测项指定科室。
                sampleItemEntity.setTechnicistId(detectorsCollection.get(0));
            } else {
                return ResultUtil.error("领取失败：检测人信息不能为空");
            }
        }

        // 进行录入任务单信息
        for (SampleItemEntity sampleItemEntity : list) {
            // 获取每组检测项 对应的 （0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生）
            List<LabelValueVo> labelValueVos = new ArrayList<>();
            List<Long> userIds = new ArrayList<>();
            // 检测人名
            StringBuffer nameStr = new StringBuffer();
            // 读取检测项人数据 生成对应的任务单。
            String[] userArrays = sampleItemEntity.getInspector().split(",");
            for (int i = 0; i < userArrays.length; i++) {
                String[] names = userArrays[i].split("&");
                LabelValueVo labelValueVo = new LabelValueVo();
                labelValueVo.setLabel(names[0]);
                labelValueVo.setValue(Long.parseLong(names[1]));
                labelValueVos.add(labelValueVo);
                userIds.add(Long.parseLong(names[1]));
                nameStr.append(names[0] + " ");
            }
            // 根据科室id 及 委托单主键 查询任务单是否存在？
            //                         1、任务单存在 查看状态=试验开始 则返回错误信息 状态=未开始试验的话，进行更新任务单操作
            //                         2、 任务单不存在的话，创建任务单即可。
            // 1.1：任务单列表存在的话
            if (CollectionUtil.isNotEmpty(taskProgressVos)) {
                for (TaskProgressVo taskProgressVo : taskProgressVos) {
                    if (taskProgressVo.getState() != 144) {
                        // 任务单状态： 状态0未抢单，1.已抢单，2已领样,3实验中，4实验完成，5原始记录已上传，6.原始记录已符合，
                        if (taskProgressVo.getState() >= 3) {
                            // 1.2 查看状态=试验开始 则返回错误信息
                            return ResultUtil.error("领取失败： 当前任务单 " + taskProgressVo.getTaskCode() + "已开始试验");
                        }
                    }
                }
                // = false 说明 任务单没有匹配的 需要新增 任务单
                Boolean flag = false;
                for (TaskProgressVo taskProgressVo : taskProgressVos) {
                    // 1.3: 任务单存在 进行更新任务单操作
                    if (taskProgressVo.getDeptId().equals(sampleItemEntity.getTechnicistId())) {
                        // 更新任务单信息。
                        TaskTestEntity taskTestEntity = new TaskTestEntity();
                        // 任务单id
                        taskTestEntity.setId(taskProgressVo.getTaskId());
                        taskTestEntity.setState(taskProgressVo.getState());
                        // 检测人

                        // 记录人

                        // 复核人

                        // 报告制作人

                        // 辅助人员

                        // 见习生：实习的新手

                        // 实习生

                        // 任务单价格 根据委托单id 统计价格即可。

                        taskMapper.updateTestTask(taskTestEntity);
                        flag = true;
                    }
                }
                if (!flag) {
                    // 新增任务单
                    TaskVo entity = new TaskVo();
                    // 补充信息
                    entity.setEntrustmentId(entrustId);
                    // 补充检测项信息
                    List<CheckItemDeptVo> checkItemDeptVoList = new ArrayList<>();
                    entity.setCheckItemDeptVoList(checkItemDeptVoList);


                }
            }
        }

        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean distributionTask412(TaskVo entity) {
        List<Long> deptIds = Lists.newArrayList();
        List<CheckItemDeptVo> checkItemDeptVoList = entity.getCheckItemDeptVoList();
        for (CheckItemDeptVo vo : checkItemDeptVoList) {
            if (!deptIds.contains(vo.getDeptId())) {
                deptIds.add(vo.getDeptId());
            }
        }
        EntrustAddVo entrustAddVo = entityMapper.selectByKeyId(entity.getEntrustmentId());
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
            long id = GenID.getID();
            vo.setId(id);
            vo.setTaskPrice(taskPrice);
            //根据委托单号月份确定任务单ID
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
            vo.setDeptId(deptId);
            vo.setCode(codeStr);
            vo.setTaskCode(teamCode + codeStr.substring(0, 4) + "-" + codeStr.substring(4, 7));
            vo.setEntrustmentId(entity.getEntrustmentId());
            vo.setRequiredCompletionTime(entity.getRequiredCompletionTime());
            vo.setOrderTime(entity.getOrderTime());
            vo.setState(0);
            vo.setReportComplete(2);
            SysUserEntity userInfo = ShiroUtils.getUserInfo();
            vo.setOrderer(userInfo.getName());
            vo.setPresentInformation(entity.getPresentInformation());
            if (!CollectionUtils.isEmpty(entity.getDeptIds())) {
                if (entity.getDeptIds().contains(deptId)) {
                    vo.setIssueReport("是");
                } else {
                    vo.setIssueReport("否");
                }
            }
            // 任务单创建时间
            vo.setCreateTime(new Date());
            vos.add(vo);
            //更新检测项分配的部门和任务单号
            List<CheckItemDeptVo> checkItemDeptVoList1 = Lists.newArrayList();
            for (CheckItemDeptVo checkItemDeptVo : checkItemDeptVoList) {
                if (deptId.equals(checkItemDeptVo.getDeptId())) {
                    checkItemDeptVo.setTaskId(id);
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
            stringBuilder1.append("任务单发布日志");
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
        //更新委托单状态
        taskMapper.updateEntrustById(entity.getEntrustmentId(), 1);
        // 处理任务流转信息 通过委托单id 和 传入信息 !=taskRelEntities.isEmpty()
        if (!CollectionUtils.isEmpty(entity.getTaskRelEntities())) {
            // 补充发布人ID和姓名
            SysUserEntity userEntity = ShiroUtils.getUserInfo();
            List<TestEntrustedTaskRelEntity> TaskRelEntities = entity.getTaskRelEntities();
            for (TestEntrustedTaskRelEntity taskdata : TaskRelEntities) {
                taskdata.setUserId(userEntity.getUserId());
                taskdata.setAddressName(userEntity.getName());
                taskdata.setCreateDate(new Date());
            }
            entrustService.methodDistributionOfFlow(entity.getEntrustmentId(), TaskRelEntities);
        }
        return true;
    }
}
