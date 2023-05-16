package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.InstrumentRecordEntityMapper;
import com.lims.manage.erp.mapper.SampleEntityMapper;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TestDetectionDao;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.TestDetectionService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: DLC
 * @Date: 2021/12/15 15:28
 */
@Service
public class TestDetectionImpl implements TestDetectionService {
    @Autowired
    TestDetectionDao testDetectionDao;
    @Autowired
    TaskMapper taskMapper;
    @Autowired
    private LogManagerService logManagerService;
    @Autowired
    private InstrumentRecordEntityMapper instrumentRecordEntityMapper;
    @Autowired
    SampleEntityMapper sampleEntityMapper;


    @Override
    public List<TestInstrumentEntity> getTheInstrument(Integer escRelId,Integer checkItemId) {
        return testDetectionDao.selectTheInstrument(escRelId,checkItemId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Boolean postStartTest(SampleItemInstrumentVo data) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String stringDate = format.format(data.getStartTime());
        if (stringDate.equals("1970-01-01")) {
            data.setStartTime(new Date());
        }
        if (data.getStartTime() == null) {
            data.setStartTime(new Date());
        }
        for (SampleItemInstrumentEntity sampleItemInstrumentEntity : data.getItemInstrumentEntityList()) {
            // 检测项 开始时间更新
            sampleItemInstrumentEntity.setStartTime(data.getStartTime());
            // 判断 test_entrusted_sample_checkitem_rel 中 start_time 是否为空
            SampleItemInstrumentEntity sampleItemInstrumentEntity1 = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(sampleItemInstrumentEntity.getItemId());
            if (sampleItemInstrumentEntity1.getStartTime() == null || sampleItemInstrumentEntity1.getState() == 0 || sampleItemInstrumentEntity1.getState() == 4) {
                // 检测项 状态 =1 检测中
                sampleItemInstrumentEntity.setState(1);
                //记录日志
                StringBuilder stringBuilder1 = new StringBuilder();
                stringBuilder1.append(" 检测项id"+sampleItemInstrumentEntity1.getItemId());
                stringBuilder1.append(" 检测项名称:"+sampleItemInstrumentEntity1.getCheckItemName());
                stringBuilder1.append(" 检测项状态:"+sampleItemInstrumentEntity.getState());
                stringBuilder1.append(" 检测项开始时间:");
                if(!StringUtils.isEmpty(sampleItemInstrumentEntity.getStartTime())){
                    stringBuilder1.append(new Timestamp(sampleItemInstrumentEntity.getStartTime().getTime()));
                }
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "试验检测-开始检测\n\t"+stringBuilder1.toString(), Const.TASK_TEST, true);
                testDetectionDao.updateSampleItemInstrumentEntity(sampleItemInstrumentEntity);
            }
        }
        // 根据 任务单id  开始检测时间 判定是否为空
        TaskTestEntity taskTestEntity = taskMapper.getTaskOrders(data.getTaskId());
        if (taskTestEntity.getStartDetectionTime() == null) {
            taskTestEntity.setId(data.getTaskId());
            // 任务单状态 == 实验中
            taskTestEntity.setState(3);
            //任务单 开始试验时间 年月日
            taskTestEntity.setStartDetectionTime(new Date(System.currentTimeMillis()));
            //记录日志
            StringBuilder stringBuilder1 = new StringBuilder();
            stringBuilder1.append(" 任务单id"+taskTestEntity.getId());
            stringBuilder1.append(" 任务单编号:"+taskTestEntity.getCode());
            stringBuilder1.append(" 任务单状态:"+taskTestEntity.getState());
            stringBuilder1.append(" 任务单开始时间:"+ new Timestamp(taskTestEntity.getStartDetectionTime().getTime()));
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "试验检测-任务单开始检测\n\t"+stringBuilder1.toString(), Const.TASK_TEST, true);
            taskMapper.updateTestTask(taskTestEntity);
            // 根据任务单主键 获取委托单主键
            EntrustEntity entrustEntity = taskMapper.getEntrustBaseInfo(taskTestEntity.getId());
            if (entrustEntity != null && entrustEntity.getState() < 3) {
                taskMapper.updateEntrustById(entrustEntity.getId(), 3);
            }
        }
        return true;
    }

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public Boolean postSelectInstrument(InstrumentEntity instrumentEntity) {
//        //记录日志
//        StringBuilder stringBuilder1 = new StringBuilder();
//        stringBuilder1.append(" 检测项id"+instrumentEntity.getItemId());
//        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "试验检测-删除设备仪器\n\t"+stringBuilder1.toString(), Const.TASK_TEST, true);
//        // 新增前 删除存留信息
//        testDetectionDao.deleteInstrument(instrumentEntity.getItemId());
//        // 检测项下 仪器表 新增
//        TestChItemInstrumentMiddleEntity testChItemInstrumentMiddleEntity = new TestChItemInstrumentMiddleEntity();
//        testChItemInstrumentMiddleEntity.setSidItem(instrumentEntity.getItemId());
//        testChItemInstrumentMiddleEntity.setStartTime(new Date());
//        if (CollectionUtils.isNotEmpty(instrumentEntity.getIds())) {
//            for (Integer id : instrumentEntity.getIds()) {
//                testChItemInstrumentMiddleEntity.setIntrusmentId(id);
//                testDetectionDao.addItemInstrumentMiddleRel(testChItemInstrumentMiddleEntity);
//            }
//        }
//        //记录日志
//        StringBuilder stringBuilder2 = new StringBuilder();
//        stringBuilder2.append(" 检测项id："+instrumentEntity.getItemId());
//        stringBuilder2.append(" 设备仪器id：");
//        if(CollectionUtils.isNotEmpty(instrumentEntity.getIds())){
//            for (Integer id : instrumentEntity.getIds()) {
//                stringBuilder2.append(""+id);
//            }
//        }
//        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "试验检测-检测项主键下 仪器表新增设备仪器\n\t"+stringBuilder2.toString(), Const.TASK_TEST, true);
//        return true;
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean postSelectInstrument1130(InstrumentEntity instrumentEntity) {
        //记录删除之前设备日志
        StringBuilder stringBuilder1 = new StringBuilder();
        stringBuilder1.append(" 检测项id"+instrumentEntity.getItemId());
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "试验检测-删除设备仪器\n\t"+stringBuilder1, Const.TASK_TEST, true);
        //删除之前设备关联关系信息
        testDetectionDao.deleteInstrument(instrumentEntity.getItemId());
        //删除之前设备使用记录
        instrumentRecordEntityMapper.deleteByEscRelId(instrumentEntity.getItemId());
        //增加检测项设备关联关系，设备的使用记录
        TestChItemInstrumentMiddleEntity testChItemInstrumentMiddleEntity = new TestChItemInstrumentMiddleEntity();
        testChItemInstrumentMiddleEntity.setSidItem(instrumentEntity.getItemId());
        testChItemInstrumentMiddleEntity.setStartTime(new Date());
        if (CollectionUtils.isNotEmpty(instrumentEntity.getRecords())) {
            for (InstrumentRecordEntity recordEntity : instrumentEntity.getRecords()) {
                testChItemInstrumentMiddleEntity.setIntrusmentId(recordEntity.getInstrumentId().intValue());
                //关联关系
                testDetectionDao.addItemInstrumentMiddleRel(testChItemInstrumentMiddleEntity);
                recordEntity.setId(GenID.getID());
                recordEntity.setEscRelId(instrumentEntity.getItemId().longValue());
                recordEntity.setBeforeStatus("正常");
                recordEntity.setAfterStatus("正常");
                recordEntity.setType("试验使用");
                recordEntity.setTime(new Date());
//                System.out.println("测试："+recordEntity.toString());
                //使用记录
                instrumentRecordEntityMapper.insert(recordEntity);
            }
        }
        //记录本次日志
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append(" 检测项id："+instrumentEntity.getItemId());
        stringBuilder2.append(" 设备仪器id：");
        if(CollectionUtils.isNotEmpty(instrumentEntity.getRecords())){
            for (InstrumentRecordEntity entity : instrumentEntity.getRecords()) {
                stringBuilder2.append(""+entity.getInstrumentId());
            }
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "试验检测-检测项主键下 仪器表新增设备仪器\n\t"+stringBuilder2.toString(), Const.TASK_TEST, true);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean postSelectInstrument0328(InstrumentEntity instrumentEntity) {
        //记录删除之前设备日志
        StringBuilder stringBuilder1 = new StringBuilder();
        stringBuilder1.append(" 检测项id"+instrumentEntity.getItemId());
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "试验检测-删除设备仪器\n\t"+stringBuilder1, Const.TASK_TEST, true);
        //删除之前设备关联关系信息
        testDetectionDao.deleteInstrument(instrumentEntity.getItemId());
        //删除之前设备使用记录
        instrumentRecordEntityMapper.deleteByEscRelId(instrumentEntity.getItemId());
        //增加检测项设备关联关系，设备的使用记录
        TestChItemInstrumentMiddleEntity testChItemInstrumentMiddleEntity = new TestChItemInstrumentMiddleEntity();
        testChItemInstrumentMiddleEntity.setSidItem(instrumentEntity.getItemId());
        testChItemInstrumentMiddleEntity.setStartTime(new Date());
        if (CollectionUtils.isNotEmpty(instrumentEntity.getRecords())) {
            for (InstrumentRecordEntity recordEntity : instrumentEntity.getRecords()) {
                testChItemInstrumentMiddleEntity.setIntrusmentId(recordEntity.getInstrumentId().intValue());
                //关联关系
                testDetectionDao.addItemInstrumentMiddleRel(testChItemInstrumentMiddleEntity);
                recordEntity.setId(GenID.getID());
                recordEntity.setEscRelId(instrumentEntity.getItemId().longValue());
                recordEntity.setBeforeStatus("正常");
                recordEntity.setAfterStatus("正常");
                recordEntity.setType("试验使用");
                recordEntity.setTime(new Date());
//                System.out.println("测试："+recordEntity.toString());
                //使用记录
                instrumentRecordEntityMapper.insert(recordEntity);
            }
        }
        //记录本次日志
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append(" 检测项id："+instrumentEntity.getItemId());
        stringBuilder2.append(" 设备仪器id：");
        if(CollectionUtils.isNotEmpty(instrumentEntity.getRecords())){
            for (InstrumentRecordEntity entity : instrumentEntity.getRecords()) {
                stringBuilder2.append(""+entity.getInstrumentId());
            }
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "试验检测-检测项主键下 仪器表新增设备仪器\n\t"+stringBuilder2.toString(), Const.TASK_TEST, true);
        return true;
    }

    @Override
    public Boolean VerifyTheLogin(Long userId, Long taskId) {
        TaskTestEntity data = taskMapper.getTaskOrders(taskId);
        if (data.getInspector() != null) {
            String[] strings2 = data.getInspector().split(",");
            for (int i = 0; i < strings2.length; i++) {
                String[] strings3 = strings2[i].split("&");
                if(userId.equals(Long.parseLong(strings3[1]))){
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public Boolean reviewTheLogin(Long userId, Long taskId) {
        TaskTestEntity data = taskMapper.getTaskOrders(taskId);
        if (data.getReviewer() != null) {
            String[] strings2 = data.getReviewer().split(",");
            for (int i = 0; i < strings2.length; i++) {
                String[] strings3 = strings2[i].split("&");
                if(userId.equals(Long.parseLong(strings3[1]))){
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Boolean postEndTest(SampleItemInstrumentVo data) {
        // 获取业务受理人id
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String stringDate = format.format(data.getEndTime());
        if (stringDate.equals("1970-01-01")) {
            data.setEndTime(new Date());
        }
        if (data.getEndTime() == null) {
            data.setEndTime(new Date());
        }
        // 遍历检测项 判断状态
        for (SampleItemInstrumentEntity sampleItemInstrumentEntity : data.getItemInstrumentEntityList()) {
            // 检测项 结束时间更新
            sampleItemInstrumentEntity.setEndTime(data.getEndTime());
            // 试验完成
            sampleItemInstrumentEntity.setState(2);
            // 检测结论
            sampleItemInstrumentEntity.setResult(data.getResult());
            //记录日志
            StringBuilder stringBuilder1 = new StringBuilder();
            stringBuilder1.append(" 检测项id："+sampleItemInstrumentEntity.getItemId());
            stringBuilder1.append(" 检测项状态："+sampleItemInstrumentEntity.getState());
            stringBuilder1.append(" 检测结论："+sampleItemInstrumentEntity.getResult());
            stringBuilder1.append(" 检测结束时间：");
            if(!StringUtils.isEmpty(sampleItemInstrumentEntity.getEndTime())){
                stringBuilder1.append(new Timestamp(sampleItemInstrumentEntity.getEndTime().getTime()));
            }
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "试验检测-结束试验\n\t"+stringBuilder1.toString(), Const.TASK_TEST, true);
            testDetectionDao.updateSampleItemInstrumentEntity(sampleItemInstrumentEntity);
            // 存放 仪器的使用记录
            // 根据检测项 主键 获取 仪器id
            List<TestChItemInstrumentMiddleEntity> getCollection = testDetectionDao.getInstrumentCollection(sampleItemInstrumentEntity.getItemId());
            if (CollectionUtils.isNotEmpty(getCollection)) {
                TestChItemInstrumentMiddleEntity testChItemInstrumentMiddleEntity = new TestChItemInstrumentMiddleEntity();
                // 依据检测项主键 统一 更新。
                testChItemInstrumentMiddleEntity.setEndTime(data.getEndTime());
                testChItemInstrumentMiddleEntity.setSidItem(sampleItemInstrumentEntity.getItemId());
                testDetectionDao.updateItemInstrumentMiddleRel(testChItemInstrumentMiddleEntity);
                //记录日志
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append(" 检测项id："+sampleItemInstrumentEntity.getItemId());
                stringBuilder2.append(" 设备仪器结束时间：");
                if(!StringUtils.isEmpty(testChItemInstrumentMiddleEntity.getEndTime())){
                    stringBuilder2.append(new Timestamp(testChItemInstrumentMiddleEntity.getEndTime().getTime()));
                }
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "试验检测-检测项主键下 仪器表结束时间\n\t"+stringBuilder2.toString(), Const.TASK_TEST, true);
            }
        }
        // 获取委托单下 所有检测项 end_time!=NULL、则更新样品状态
        if(CollectionUtils.isNotEmpty(data.getItemInstrumentEntityList())){
            boolean bitStatus = true;
            List<SampleItemEntity> list = testDetectionDao.selectItemList(data.getItemInstrumentEntityList().get(0).getItemId());
            Set<Integer> setIds = new HashSet<>();
            for(SampleItemEntity itemEntity : list){
                    if(itemEntity.getEndTime()==null){
                        bitStatus = false;
                    }
                setIds.add(itemEntity.getSampleId());
                }
                if(bitStatus){
                    List<Integer> sampleIds = new ArrayList<>();
                    sampleIds.addAll(setIds);
                    List<Integer> ids = sampleEntityMapper.sampleStausDisint(sampleIds);
                    // if ids==null 新增数据
                    if(CollectionUtils.isEmpty(ids)){
                        // 根据委托单id 批量更新样品状态。
                        testDetectionDao.batchSampleState(list.get(0).getEntrustId());
                        for(Integer sampleId :setIds){
                            // 增加样品样品流转状态
                            SampleCirculationRecord sa = new SampleCirculationRecord();
                            sa.setSampleId(sampleId);
                            sa.setStatus("2");
                            sa.setOperatorId(userInfo.getUserId());
                            sa.setOperatorName(userInfo.getName());
                            sa.setTime(new Date());
                            sampleEntityMapper.saveSampleCirculationRecord(sa);
                        }
                    }
                    // if ids != null update数据
                    if(CollectionUtils.isNotEmpty(ids)){
                        // 更新样品流转状态
                        sampleEntityMapper.updateStausDisint(userInfo.getUserId(),userInfo.getName(),new Date(),ids);
                    }
                }

        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized Boolean JudgmentTaskDetail(TaskDetailInfoVo dataGather, Long TaskId) {
        for (SampleDetailVo sampleDetailVo : dataGather.getSampleDetailList()) {
            for (CheckItemInfoVo checkItemInfoVo : sampleDetailVo.getCheckItemInfoList()) {
                SampleItemInstrumentEntity dataDisplay = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(checkItemInfoVo.getItemId());
               // 检测项未 全部开检 则任务单无法结束试验
                if (dataDisplay.getState() != null && dataDisplay.getState()<2) {
                    return false;
                }
            }
        }
        // 更新任务单状态
        TaskTestEntity taskTestEntity = new TaskTestEntity();
        taskTestEntity.setId(TaskId);
        // 任务单 == 4 试验完成
        taskTestEntity.setState(4);
        taskTestEntity.setEndDetectionTime(new Date(System.currentTimeMillis()));
        //记录日志
        StringBuilder stringBuilder1 = new StringBuilder();
        stringBuilder1.append(" 任务单id："+taskTestEntity.getId());
        stringBuilder1.append(" 任务单状态："+taskTestEntity.getState());
        stringBuilder1.append(" 任务结束时间：");
        if(!StringUtils.isEmpty(taskTestEntity.getEndDetectionTime())){
            stringBuilder1.append(new Timestamp(taskTestEntity.getEndDetectionTime().getTime()));
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "试验检测-任务单结束试验\n\t"+stringBuilder1.toString(), Const.TASK_TEST, true);
        taskMapper.updateTestTask(taskTestEntity);
        // 根据任务单主键 获取委托单主键
        EntrustEntity entrustEntity = taskMapper.getEntrustBaseInfo(taskTestEntity.getId());
        if (entrustEntity != null && entrustEntity.getState() < 4) {
            taskMapper.updateEntrustById(entrustEntity.getId(), 4);
        }
        return true;
    }

    /**
     * 返回信息
     *
     * @param dataGather
     * @param TaskId
     * @param itemId
     * @return
     */
    @Override
    public Boolean getTestDetails(TaskDetailInfoVo dataGather, Long TaskId, Integer itemId) {
        SampleItemInstrumentEntity CheckItemDetail = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(itemId);
        System.out.println(CheckItemDetail);
        return null;
    }

    @Override
    public Boolean Postreview(Integer itemId) {
        // 依据检测项 主键
        SampleItemInstrumentEntity CheckItemDetail = testDetectionDao.getTestEntrustedSampleCheckitemRelDetail(itemId);
        if (CheckItemDetail.getState() != 2) {
            // 说明未复核
            SampleItemInstrumentEntity sampleItemInstrumentEntity = new SampleItemInstrumentEntity();
            sampleItemInstrumentEntity.setState(2);
            sampleItemInstrumentEntity.setItemId(itemId);
            //记录日志
            StringBuilder stringBuilder1 = new StringBuilder();
            stringBuilder1.append(" 检测项id："+sampleItemInstrumentEntity.getItemId());
            stringBuilder1.append(" 检测项状态："+sampleItemInstrumentEntity.getState());
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "试验检测-检测项复核\n\t"+stringBuilder1.toString(), Const.TASK_TEST, true);
            testDetectionDao.updateSampleItemInstrumentEntity(sampleItemInstrumentEntity);
            return true;
        }
        return false;
    }

    @Override
    public DeviceUseTimeVo checkDeviceUseTime(DeviceUseTimeVo vo) {
        DeviceUseTimeVo result = new DeviceUseTimeVo();
        //校验时间是否冲突
        List<InstrumentRecordEntity> instrumentRecordVos = instrumentRecordEntityMapper.checkTime(vo);
        if(CollectionUtils.isEmpty(instrumentRecordVos)){//不冲突
            result.setFlag(true);
        }else{//冲突
            //本次仪器需用时间（秒）
            long diffSecond = (vo.getEndTime().getTime() - vo.getStartTime().getTime())/1000;
            List<InstrumentRecordVo> instrumentRecords = instrumentRecordEntityMapper.getInstrumentRecordByTime(vo);
            if(!CollectionUtils.isEmpty(instrumentRecords)){
                for (int i = 0; i < instrumentRecords.size(); i++) {
                    InstrumentRecordVo nearRecord = instrumentRecords.get(i);
                    //最接近本次开始时间的使用记录结束时间
                    Date lastEndTime = nearRecord.getEndTime();
                    //判断是否是最后一条记录
                    if(i != instrumentRecords.size() -1){
                        //不是最后一条记录，查询下条记录的开始时间，判断中间的时间间隔能否满足
                        InstrumentRecordVo nextRecord = instrumentRecords.get(i + 1);
                        //可用时间间隔（秒）
                        long timeInterval = (nextRecord.getStartTime().getTime() - lastEndTime.getTime()) / 1000;
                        //可用时间间隔>=本次仪器需用时间
                        if(timeInterval >= diffSecond){
                            result.setStartTime(lastEndTime);
                            long l = lastEndTime.getTime() + diffSecond * 1000;
                            Date endTime = new Date(l);
                            result.setEndTime(endTime);
                            break;
                        }
                    }else{
                        //是最后一条记录，直接在结束时间后加上本次所需时间
                        result.setStartTime(lastEndTime);
                        long l = lastEndTime.getTime() + diffSecond * 1000;
                        Date endTime = new Date(l);
                        result.setEndTime(endTime);
                    }
                }
            }
            result.setFlag(false);
            result.setInstrumentRecords(instrumentRecords);
        }
        return result;
    }

    @Override
    public String compareItemTime(SampleItemInstrumentVo data) {
        // 检测项结束时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String stringDate = format.format(data.getEndTime());
        if (stringDate.equals("1970-01-01")) {
            data.setEndTime(new Date());
        }
        if (data.getEndTime() == null) {
            data.setEndTime(new Date());
        }
        List<Integer> list = new ArrayList<>();
        // 效验检测项 结束时间 与 开始时间 是否合理
        for(SampleItemInstrumentEntity sampleItemInstrumentEntity : data.getItemInstrumentEntityList()){
            list.add(sampleItemInstrumentEntity.getItemId());
        }
        // 查询检测项集合
        List<SampleItemInstrumentEntity> itemList = testDetectionDao.getTestEntrustedSampleCheckitemRelDetailList(list);
        for(SampleItemInstrumentEntity sampleItemInstrumentEntity : itemList){
            if(sampleItemInstrumentEntity != null && sampleItemInstrumentEntity.getStartTime() != null){
                // 检测项结束时间 （结束日期是否在开始日期之前。）
                if(data.getEndTime().before(sampleItemInstrumentEntity.getStartTime())){
                    String startTime =  format.format(sampleItemInstrumentEntity.getStartTime());
                    String endTime =  format.format(data.getEndTime());
                    // 结束日期 != 开始日期
                    if(!startTime.equals(endTime)){
                        return "检测项名:"+sampleItemInstrumentEntity.getCheckItemName()+" 开始时间:"+startTime
                                +" 结束时间："+endTime+"有误，请重新选择";
                    }
                }
            }
        }
        return null;
    }

}
