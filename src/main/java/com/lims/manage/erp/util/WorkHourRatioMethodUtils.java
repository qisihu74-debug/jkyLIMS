package com.lims.manage.erp.util;

import cn.hutool.core.collection.CollectionUtil;
import com.lims.manage.erp.entity.TestCheckItemsTaskRel;
import com.lims.manage.erp.entity.TestInitDataEntity;
import com.lims.manage.erp.entity.TestItemOrderWorkingHours;
import com.lims.manage.erp.entity.TestTaskOrderWorkingHours;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: DLC
 * @Date: 2024/1/11 15:25
 */
public class WorkHourRatioMethodUtils {
    /**
     * 动态计算 工时信息
     *
     * @param sqlBasisList              检测类型比例信息
     * @param itemList                  集合工时人员信息
     * @param itemOrderWorkingHoursList 通过任务单获取检测项工时信息
     * @return
     */
    public List<TestTaskOrderWorkingHours> searchingLoop(List<TestInitDataEntity> sqlBasisList, List<TestCheckItemsTaskRel> itemList, List<TestItemOrderWorkingHours> itemOrderWorkingHoursList) {
//        List<TestInitDataEntity> updateSqlBasisList = sqlBasisList.stream().collect(Collectors.toList());
//        // 初始化默认人员比例。
//        for (int i = 0; i < strings.length; i++) {
//            TestInitDataEntity dataEntity = updateSqlBasisList.get(i);
//            dataEntity.setRemark(strings[i]);
//            updateSqlBasisList.set(i, dataEntity);
//        }
        // 分组中比例信息: key =序号，value = 值
        HashMap<Integer, TestInitDataEntity> map = new HashMap<>();
        for (int i = 0; i < sqlBasisList.size(); i++) {
            map.put(i, sqlBasisList.get(i));
        }
//        List<TestCheckItemsTaskRel> itemList = bitValueMap.get(key);
        // 检测项去重
        Set<Integer> itemSet = new HashSet<>();
        for (TestCheckItemsTaskRel taskRel : itemList) {
            itemSet.add(taskRel.getItemId());
        }
        // 根据检测项信息 生成 对应的工时、获取实际委托中组数。
        List<TestItemOrderWorkingHours> itemWorkingHoursList = new ArrayList<>();
        for (Integer itemId : itemSet) {
            for (TestItemOrderWorkingHours itemOrderWorkingHours : itemOrderWorkingHoursList) {
                if (itemOrderWorkingHours.getItemId().equals(itemId)) {
                    itemWorkingHoursList.add(itemOrderWorkingHours);
                }
            }
        }
        // 工时总信息
        String sum = "";
        for (TestItemOrderWorkingHours itemOrderWorkingHours : itemWorkingHoursList) {
            Integer times = itemOrderWorkingHours.getTimes();
            Double workingHours = 0.0;
            if (StringUtils.isNotEmpty(itemOrderWorkingHours.getWorkingHours())) {
                workingHours = Double.valueOf(itemOrderWorkingHours.getWorkingHours());
            }
            BigDecimal he = BigDecimal.valueOf(times * workingHours).setScale(4, BigDecimal.ROUND_HALF_UP);
            if (StringUtils.isEmpty(sum)) {
                sum = String.valueOf(he);
            } else {
                // sum 不为空
                BigDecimal sumbig = new BigDecimal(sum);
                sum = String.valueOf(sumbig.add(he).setScale(4, BigDecimal.ROUND_HALF_UP));
            }
        }
        // 每个人的所属比例及工时信息。
        // 工具类：工时业务处理
        List<TestTaskOrderWorkingHours> list = new ArrayList<>();
        list = workHourRatioMethod(itemList, map, sum);
//        if (CollectionUtil.isNotEmpty(list)) {
//            bitTaskRelList.put(key, list);
//        }
        return list;
    }


    /**
     * 返回 求每个人的所占比例及获取工时信息
     *
     * @param itemList 任务大厅 每组 检测项分配人员信息。
     * @param map      人员比例
     * @param sum      分组工时比例。
     */
    public List<TestTaskOrderWorkingHours> workHourRatioMethod(List<TestCheckItemsTaskRel> itemList, HashMap<Integer, TestInitDataEntity> map, String sum) {
        // key = userId 、 value = 比例信息。
        // 使用 map 进行 数据统计 key = userId , value = 参数
        Map<Long, TestTaskOrderWorkingHours> mapData = new HashMap<>();
        // 人员比例
        List<TestInitDataEntity> basisList = new ArrayList<>();
        for (int i = 0; i < map.size(); i++) {
            basisList.add(i, map.get(i));
        }

        /**
         * 调整比例 ： 辅助人包括报告制作和辅助人员 : 两者存在 比例/2
         */
        methodAdjustingThePproportion(itemList, basisList);

        /**
         * 获取分组下人员占比信息
         */
        methodObtainProportionInformation(mapData, itemList, basisList);
        System.out.println("获取分组下人员占比信息");
        List<TestTaskOrderWorkingHours> list = new ArrayList<>();
        // 返回 工时信息及比例
        if (CollectionUtil.isNotEmpty(mapData.keySet())) {
            for (Long userId : mapData.keySet()) {
                TestTaskOrderWorkingHours data = mapData.get(userId);
                // 比例 保留四位小数。
                BigDecimal proportion = BigDecimal.valueOf(Double.valueOf(data.getProportion()) / 100).setScale(4, BigDecimal.ROUND_HALF_UP);
                // 分组工时
                data.setTotalWorkingHours(sum);
                BigDecimal zhi1 = BigDecimal.valueOf(Double.valueOf(data.getTotalWorkingHours())).setScale(4, BigDecimal.ROUND_HALF_UP);
                //当前人的工时 = 比例 * zhi 保留四位小数。
                BigDecimal he = proportion.multiply(zhi1).setScale(4, BigDecimal.ROUND_HALF_UP);
                data.setWorkingHours(String.valueOf(he));
                mapData.put(userId, data);
                list.add(data);
            }
            return list;
        }
        return list;
    }

    /**
     * 私有方法：
     * 调整比例 ： 辅助人包括报告制作和辅助人员 : 两者存在 比例/2
     *
     * @param itemList
     * @param basisList
     */
    public void methodAdjustingThePproportion(List<TestCheckItemsTaskRel> itemList, List<TestInitDataEntity> basisList) {
        // 报告制作人员
        String reportProducer = null;
        // 辅助人员
        String auxiliaryPersonnelperson = null;
        for (TestCheckItemsTaskRel taskRel : itemList) {
            if (taskRel.getUserType() == 3) {
                reportProducer = "报告制作人存在";
            }
            if (taskRel.getUserType() == 4) {
                auxiliaryPersonnelperson = "辅助人员存在";
            }
        }

        // 辅助人包括报告制作和辅助人员 : 两者存在 比例/2
        if (StringUtils.isNotEmpty(reportProducer) && StringUtils.isNotEmpty(auxiliaryPersonnelperson)) {
            // 任务单中 包含报告制作人与辅助人员 不为空 则平摊比例
            TestInitDataEntity reportProducerData = basisList.get(3);
            int zhi1 = Integer.parseInt(reportProducerData.getRemark()) / 2;
            reportProducerData.setRemark(String.valueOf(zhi1));
            basisList.set(3, reportProducerData);
            TestInitDataEntity auxiliaryPersonnel = basisList.get(4);
            int zhi2 = Integer.parseInt(auxiliaryPersonnel.getRemark()) / 2;
            auxiliaryPersonnel.setRemark(String.valueOf(zhi2));
            basisList.set(4, auxiliaryPersonnel);
        } else {
            // 辅助人员 工时 = 0
            TestInitDataEntity auxiliaryPersonnel = basisList.get(4);
            auxiliaryPersonnel.setRemark("0");
            basisList.set(4, auxiliaryPersonnel);
        }
    }

    /**
     * 获取分组下人员占比信息
     *
     * @param mapData
     * @param itemList
     * @param basisList
     */
    public void methodObtainProportionInformation(Map<Long, TestTaskOrderWorkingHours> mapData, List<TestCheckItemsTaskRel> itemList, List<TestInitDataEntity> basisList) {
        // itemList 中包含  0：检测人、1：记录人、2、复核人、3、报告制作人、4、辅助人员、5、见习生：实习的新手、6、实习生
        // basisList 中包含：0 检测人员	25 。1、记录人员	25。2 复核人	20。3 报告制作人	10。 4 辅助人员	10 。 5 签发人	20。
        for (int i = 0; i < basisList.size() - 1; i++) {
            // 截取list中数据
            String inspector = methodOrganizeData(i, itemList);
            if (StringUtils.isNotEmpty(inspector)) {
                methodSubstr(i, inspector, mapData, null, basisList);
            }
        }
    }

    /**
     * 整理检测类型数据
     *
     * @param type
     * @param itemList
     * @return
     */
    public String methodOrganizeData(Integer type, List<TestCheckItemsTaskRel> itemList) {
        // 检测人员
        StringBuffer inspector = new StringBuffer();
        // 获取 type类型去重后数据
        HashMap<Long, TestCheckItemsTaskRel> map = new HashMap<>();
        for (TestCheckItemsTaskRel taskRel : itemList) {
            if (taskRel.getUserType() == type) {
                map.put(Long.valueOf(taskRel.getUserId()), taskRel);
            }
        }
        if (CollectionUtil.isNotEmpty(map.keySet())) {
            for (Long userId : map.keySet()) {
                TestCheckItemsTaskRel taskRel = map.get(userId);
                inspector.append(taskRel.getUserName() + "&" + taskRel.getUserId());
                inspector.append(",");
            }
            return inspector.deleteCharAt(inspector.length() - 1).toString();
        }
        return null;
    }

    /**
     * 调用方法 去处理 签名信息进行截取
     *
     * @param type                 签名类型 0=检测人员：、 1=记录人员：、2=复核人：、3=报告制作人：
     * @param signatureInformation 签名信息
     * @param mapData              使用 map 进行 数据统计 key = userId , value = 参数
     * @param taskId               任务单id
     */
    public void methodSubstr(Integer type, String signatureInformation, Map<Long, TestTaskOrderWorkingHours> mapData, Long taskId, List<TestInitDataEntity> basisList) {
        String typeStr = basisList.get(type).getName();
        // key = 类型 value = 比例值
        Map<String, Integer> map = new HashMap<>();
        for (TestInitDataEntity dataEntity : basisList) {
            map.put(dataEntity.getName(), Integer.parseInt(dataEntity.getRemark()));
        }
        // 检测人员
        String[] inspectorarrays = signatureInformation.split(",");
        int listSize = inspectorarrays.length;
        for (int i = 0; i < inspectorarrays.length; i++) {
            String[] inspectorStr = inspectorarrays[i].split("&");
            if (mapData.get(Long.parseLong(inspectorStr[1])) == null) {
                TestTaskOrderWorkingHours data = new TestTaskOrderWorkingHours();
                data.setUserName(inspectorStr[0]);
                data.setUserId(Long.parseLong(inspectorStr[1]));
                // 设置比例
                if (map.get(typeStr) != null) {
                    Double value = Double.valueOf(map.get(typeStr));
                    BigDecimal zhi1 = BigDecimal.valueOf(value / listSize).setScale(2, BigDecimal.ROUND_HALF_UP);
                    data.setProportion(String.valueOf(zhi1));
                } else {
                    data.setProportion("0");
                }
                data.setDetectionType(typeStr);
                data.setTaskId(taskId);
                mapData.put(data.getUserId(), data);
            } else {
                TestTaskOrderWorkingHours data = mapData.get(Long.parseLong(inspectorStr[1]));
                data.setUserName(inspectorStr[0]);
                data.setUserId(Long.parseLong(inspectorStr[1]));
                // 设置比例
                if (map.get(typeStr) != null) {
                    Double value1 = Double.valueOf(map.get(typeStr));
                    BigDecimal zhi1 = BigDecimal.valueOf(value1 / listSize).setScale(2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal value2 = BigDecimal.valueOf(Double.parseDouble(data.getProportion())).setScale(2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal sum = zhi1.add(value2);
                    data.setProportion(String.valueOf(sum));
                } else {
                    Double value1 = Double.valueOf(data.getProportion());
                    data.setProportion(String.valueOf(value1));
                }
                data.setDetectionType(data.getDetectionType() + "、" + typeStr);
                data.setTaskId(taskId);
                mapData.put(data.getUserId(), data);
            }
        }
    }

}
