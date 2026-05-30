package com.lims.manage.erp.util;

import cn.hutool.core.collection.CollectionUtil;
import com.lims.manage.erp.entity.SampleItemEntity;
import com.lims.manage.erp.entity.TestCheckItemTeamRel;
import com.lims.manage.erp.entity.TestCheckItemsTaskRel;
import com.lims.manage.erp.service.impl.TestTaskPoolServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * 执行任务单详情展示业务处理
 *
 * @Author: DLC
 * @Date: 2024/8/23 10:44
 */
public class TaskListToolSortUtils {

    /**
     * 检测项集合 与 检测项团队 优先级处理
     *
     * @param itemList
     * @param teamRelList
     */
    public static void itemAndTeamProcessing(List<SampleItemEntity> itemList, List<TestCheckItemTeamRel> teamRelList, Long teamId) {

        // key = checkItemId、value = 所属检测项团队
        Map<Integer, List<TestCheckItemTeamRel>> itemlistMap = new HashMap<>();
        for (TestCheckItemTeamRel checkItemTeamRel : teamRelList) {
//                 当前检测项与检测项模版数据比较时 存在
            if (itemlistMap.get(checkItemTeamRel.getCheckItemId()) == null) {
                List<TestCheckItemTeamRel> itemEntities = new ArrayList<>();
                itemEntities.add(checkItemTeamRel);
                itemlistMap.put(checkItemTeamRel.getCheckItemId(), itemEntities);
            } else {
                List<TestCheckItemTeamRel> itemEntities = itemlistMap.get(checkItemTeamRel.getCheckItemId());
                itemEntities.add(checkItemTeamRel);
                itemlistMap.put(checkItemTeamRel.getCheckItemId(), itemEntities);
            }
        }

        // 委托单下 检测项列表 遍历
        for (SampleItemEntity sampleItemEntity : itemList) {

            // 当前检测项 存在团队 进行处理
            List<TestCheckItemTeamRel> itemTeamRels = itemlistMap.get(sampleItemEntity.getCheckItemId().intValue());
            if (CollectionUtil.isNotEmpty(itemTeamRels)) {
                // 当前检测项id 所属团队只有一个
                if (itemTeamRels.size() == 1) {
                    // 条数唯一：部门id与所属部门一致
                    if (teamId.equals(itemTeamRels.get(0).getTeamId().longValue())) {
                        sampleItemEntity.setPriority("1");
                        sampleItemEntity.setTeamName(itemTeamRels.get(0).getTeamName() != null ? itemTeamRels.get(0).getTeamName() : "本团队");
                    } else {
                        sampleItemEntity.setPriority(null);
                        sampleItemEntity.setTeamName(itemTeamRels.get(0).getTeamName() != null ? itemTeamRels.get(0).getTeamName() : "本团队");
                    }
                } else if (sampleItemEntity.getPriority() == null && sampleItemEntity.getTechnicistId() != null && sampleItemEntity.getTechnicistId().equals(teamId)) {
                    sampleItemEntity.setPriority("1");
                    sampleItemEntity.setTeamName("本团队");
                } else {

                    boolean foundMatchingTeam = false;
                    // 多个情况下 进行排序优先操作
                    for (TestCheckItemTeamRel teamRel : itemTeamRels) {

                        // 设置检测团队优先级 ： 当前团队与检测项团队相等 && 优先级不为空 优先级 = 1
                        if (teamId.equals(teamRel.getTeamId().longValue()) && (teamRel.getPriority() != null && teamRel.getPriority().equals("1"))) {
                            sampleItemEntity.setPriority("1");
                            sampleItemEntity.setTeamName(teamRel.getTeamName() != null ? teamRel.getTeamName() : "本团队");
                            foundMatchingTeam = true;
                            // 符合条件 跳出本次循环
                            continue;
                        }
                    }
                    if (!foundMatchingTeam) {
                        for (TestCheckItemTeamRel teamRel : itemTeamRels) {
                            if (teamRel.getPriority() != null && teamRel.getPriority().equals("1") && sampleItemEntity.getTeamName() == null) {
                                sampleItemEntity.setTeamName(teamRel.getTeamName());
                            }
                        }
                    }
                }
            }
        }
    }

    public static void methodForEachTaskHallDetails(SampleItemEntity
                                                            sampleItemEntity, List<TestCheckItemsTaskRel> itemsTaskRels) {
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
     * 任务单id 不为空 返回对应检测人员信息
     *
     * @param itemList
     * @param teamId
     * @param itemsTaskRels
     */
    public static void checkItemPersonnelInformation(List<SampleItemEntity> itemList, Long
            teamId, List<TestCheckItemsTaskRel> itemsTaskRels) {

        // 委托单下 检测项列表 遍历
        for (SampleItemEntity sampleItemEntity : itemList) {
            // 检测项已领取，不是当前团队 则标记为空
            if (sampleItemEntity.getTaskId() != null && !sampleItemEntity.getTechnicistId().equals(teamId)) {
                sampleItemEntity.setPriority(null);
                sampleItemEntity.setTeamName("其他团队已领取");
            }
            List<TestCheckItemsTaskRel> itemsTaskRels1 = new ArrayList<>();
            // 检测人员信息展示
            if (CollectionUtil.isNotEmpty(itemsTaskRels)) {
                for (TestCheckItemsTaskRel testCheckItemsTaskRel : itemsTaskRels) {
                    // 进行每组检测项 与 检测项指派人员的集成。
                    if (testCheckItemsTaskRel.getItemId().equals(sampleItemEntity.getId()) && (sampleItemEntity.getTaskId() != null && sampleItemEntity.getTechnicistId().equals(teamId))) {
                        //TODO: 假如： 检测项重复使用 需要通过任务单进行区分。暂时未过滤
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


}
