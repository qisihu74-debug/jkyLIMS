package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.aspose.cells.Cells;
import com.aspose.cells.Worksheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.DeviceEntity;
import com.lims.manage.erp.entity.InstrumentRecordEntity;
import com.lims.manage.erp.entity.InstrumentUseGroup;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestInstrument;
import com.lims.manage.erp.mapper.InstrumentRecordEntityMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.AppTestInstrumentService;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.service.TestInstrumentService;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.naming.ldap.HasControls;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Author: DLC
 * @Date: 2023/4/3 15:09
 */
@Slf4j
@RestController
@RequestMapping("/app/testInstrument/")
public class AppTestInstrumentController {

    @Resource
    AppTestInstrumentService appTestInstrumentService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TestInstrumentService testInstrumentService;
    @Autowired
    private InstrumentRecordEntityMapper instrumentRecordEntityMapper;

    /**
     * 新增检测任务列表 (根据检测人id 返回待任务单检测列表)
     *
     * @param search
     * @return
     */
    @RequestMapping("detectionTaskList")
    public Result detectionTaskList(String search, Long instrumentId) {
        // 当前任务单列表 == null ，调用检测任务列表
        if (CollectionUtils.isEmpty(appTestInstrumentService.taskList(search, instrumentId))) {
            // 验证登录人信息 和部门 存入
            SysUserEntity userInfo = ShiroUtils.getUserInfo();
            return ResultUtil.success(appTestInstrumentService.detectionTaskList(search, userInfo.getUserId()));
        }
        // 非空 返回空集合
        return ResultUtil.error("当前任务列表未全部结束");
    }

    /**
     * 当前任务列表 (根据设备id 返回列表)
     *
     * @param search
     * @param instrumentId
     * @return
     */
    @RequestMapping("taskList")
    public Result taskList(String search,Long instrumentId) {
        return ResultUtil.success(appTestInstrumentService.taskList(search, instrumentId));
    }

    /**
     * 任务单详情（检测项复核通过不展示）
     *
     * @param taskIds
     * @return
     */
    @RequestMapping("taskDetails")
    @ResponseBody
    public Result taskDetails(@RequestParam List<Long> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }
        List<TaskDetailInfoVo> list = new ArrayList<>();
        for (Long taskId : taskIds) {
            // 根据 taskId 展示详情
            TaskDetailInfoVo taskDetails = taskService.getTaskDetailInfoTwo(taskId, null);
            if (taskDetails != null) {
                // 遍历样品
                if (!CollectionUtils.isEmpty(taskDetails.getSampleDetailList())) {
                    for (SampleDetailVo sampleDetailVo : taskDetails.getSampleDetailList()) {
                        // 处理 检测项待复核 0：待检，1：检测中，2：待复核，3 ：通过，4：驳回
                        if (!CollectionUtils.isEmpty(sampleDetailVo.getCheckItemInfoList())) {
                            Iterator<CheckItemInfoVo> it = sampleDetailVo.getCheckItemInfoList().iterator();
                            while (it.hasNext()) {
                                CheckItemInfoVo checkItemVo = it.next();
                                if (checkItemVo.getEndTime() != null && checkItemVo.getState() == 3) {
                                    it.remove();
                                }
                            }
                        }
                    }
                }
            }
            list.add(taskDetails);
        }
        return ResultUtil.success("查询任务详情成功！", list);
    }

    /**
     * 返回团队人员信息列表
     *
     * @return
     */
    @RequestMapping("returnPersonList")
    public Result returnPersonList() {
        // 验证登录人信息 和部门 存入
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        return ResultUtil.success(appTestInstrumentService.returnPersonList(userInfo.getUserId()));
    }

    /**
     * 开始试验
     * @param instrumentVo
     * @return
     */
    @RequestMapping("startToTestOld")
    public Result startToTest(@RequestBody InstrumentVo instrumentVo) {
        if (instrumentVo == null) {
            return ResultUtil.error("参数不能为空");
        }
        if (CollectionUtils.isEmpty(instrumentVo.getCheckItemInfoList())) {
            return ResultUtil.error("参数不能为空");
        }
        return appTestInstrumentService.startToTest(instrumentVo);
    }

    /**
     * 新APP开始试验
     * @param instrumentVo
     * @return
     */
    @RequestMapping("startToTest")
    public Result startToTestNew(@RequestBody InstrumentParamVo instrumentVo) {
//        if (instrumentVo == null || instrumentVo.getInstrumentVoList().get(0).getRecordType() == null) {
//            return ResultUtil.error("参数不能为空");
//        }
        if(instrumentVo.getInsertType() != null){//插单
            return appTestInstrumentService.startToTestNewInsert(instrumentVo);
        }else{//正常记录
            if(instrumentVo.getIsShow() != null && instrumentVo.getIsShow().equals(0)){//生成记录
                return appTestInstrumentService.startToTestNew(instrumentVo);
            }else{//不生成记录
                return appTestInstrumentService.startToTestNewNo(instrumentVo);
            }
        }
    }

    /**
     * 创建队伍
     * @param instrumentVo
     * @return
     */
    @RequestMapping("createGroup")
    public Result createGroup(@RequestBody InstrumentParamVo instrumentVo) {
        if (instrumentVo == null) {
            return ResultUtil.error("参数不能为空");
        }
        return appTestInstrumentService.createGroup(instrumentVo);
    }

    /**
     * 退出队伍
     * @param group
     * @return
     */
    @RequestMapping("deleteGroup")
    public Result deleteGroup(@RequestBody InstrumentUseGroup group) {
        return appTestInstrumentService.deleteGroup(group);
    }

    /**
     * 结束试验
     *
     * @param instrumentVo
     * @return
     */
    @RequestMapping("endToTestOld")
    public Result endToTest(@RequestBody InstrumentVo instrumentVo) {
        if (instrumentVo == null) {
            return ResultUtil.error("参数不能为空");
        }
        if (CollectionUtils.isEmpty(instrumentVo.getInstrumentRecordListVos())) {
            return ResultUtil.error("参数不能为空");
        }
        return appTestInstrumentService.endToTest(instrumentVo, 2);
    }

    /**
     * 新版APP结束试验
     * @param instrumentVo
     * @return
     */
    @RequestMapping("endToTest")
    public Result endToTestNew(@RequestBody InstrumentVo instrumentVo) {
        if (instrumentVo == null) {
            return ResultUtil.error("参数不能为空");
        }
        return appTestInstrumentService.endToTestNew(instrumentVo);
    }

    /**
     * 结束复核
     *
     * @param instrumentVo
     * @return
     */
    @RequestMapping("closingReview")
    public Result closingReview(@RequestBody InstrumentVo instrumentVo) {
        if (instrumentVo == null) {
            return ResultUtil.error("参数不能为空");
        }
        if (CollectionUtils.isEmpty(instrumentVo.getInstrumentRecordListVos())) {
            return ResultUtil.error("参数不能为空");
        }
        return ResultUtil.success(appTestInstrumentService.endToTest(instrumentVo, 2));
    }

    /**
     * 根据设备id查询设备详细信息
     *
     * @param instrumentId
     * @return
     */
    @RequestMapping("getDetails")
    public Result InstrumentDetails(Long instrumentId) {
        if (instrumentId == null || instrumentId.equals("")) {
            return ResultUtil.error("参数为空");
        }
        return ResultUtil.success(appTestInstrumentService.InstrumentDetails(instrumentId));
    }

    @RequestMapping("getDetailsNew")
    public Result getDetailsNew(Long instrumentId) {
        if (instrumentId == null || instrumentId.equals("")) {
            return ResultUtil.error("参数为空");
        }
        return ResultUtil.success(appTestInstrumentService.getDetailsNew(instrumentId));
    }

    /**
     * 查询指定时间段内可用时间
     * @param instrumentVo
     * @return
     */
    @RequestMapping("getInstrumentUseTime")
    public Result getInstrumentUseTime(@RequestBody InstrumentVo instrumentVo) {
        if (instrumentVo == null || instrumentVo.getStartTime() == null
                || instrumentVo.getEndTime() == null || instrumentVo.getId() == null) {
            return ResultUtil.error("参数为空！");
        }
        return ResultUtil.success(appTestInstrumentService.getInstrumentUseTime(instrumentVo));
    }

    /**
     * 根据记录id返回记录详细信息
     *
     * @param recordId
     * @return
     */
    @RequestMapping("getRecordDetails")
    public Result getRecordDetails(Long recordId) {
        if (recordId == null || recordId.equals("")) {
            return ResultUtil.error("参数为空");
        }
        return ResultUtil.success(appTestInstrumentService.getRecordDetails(recordId));
    }

    /**
     * 导入设备使用记录
     * @param file
     * @return
     */
    @PostMapping("importRecord")
    public Result importRecord(MultipartFile file) throws Exception{
        Map<String,String> successList = new HashMap<>();
        Map<String,String> failList = new HashMap<>();
        //读取excel
        com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook("D:\\Users\\Administrator\\Desktop\\使用记录.xlsx");
        Worksheet worksheet = workbook.getWorksheets().get(0);
        Cells cells = worksheet.getCells();
        //遍历excel，根据编号查询
        int num = 2;
        List<InstrumentRecordEntity> list = Lists.newArrayList();
        while (num <= 424) {
            InstrumentRecordEntity recordEntity = new InstrumentRecordEntity();
            String deviceCodeIndex = "A" + num;
            String taskCodeIndex = "B" + num;
            String itemNameIndex = "C" + num;
            String wdIndex = "D" + num;
            String sdIndex = "E" + num;
            String useStateIndex = "F" + num;
            String beforeStateIndex = "G" + num;
            String userIndex = "H" + num;
            String starTimeIndex = "I" + num;
            String endTimeIndex = "J" + num;
            //根据设备编号查询设备id
            LambdaQueryWrapper<TestInstrument> queryWrapper = new LambdaQueryWrapper<>();
            String value = cells.get(deviceCodeIndex).getValue().toString();
            queryWrapper.eq(TestInstrument::getCode, value);
            List<TestInstrument> testInstruments = testInstrumentService.list(queryWrapper);
            if (org.apache.commons.collections4.CollectionUtils.isEmpty(testInstruments)){
                failList.put(num+":"+cells.get(taskCodeIndex).getValue().toString(),"设备不存在");
                num ++;
                continue;
            }
            recordEntity.setId(GenID.getID());// 记录id
            recordEntity.setInstrumentId(Long.parseLong(testInstruments.get(0).getId() + ""));// 仪器id
            //根据任务单号和参数名称获取检测项的id
            List<Integer> itemsIds = taskService.selectList(cells.get(taskCodeIndex).getValue().toString(),cells.get(itemNameIndex).getValue().toString());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(itemsIds)){
                failList.put(num+":"+cells.get(taskCodeIndex).getValue().toString(),"任务参数未匹配上:");
                num ++;
                continue;
            }
            recordEntity.setEscRelId(Long.parseLong(itemsIds.get(0)+""));// 检测项主键
            recordEntity.setType("试验使用");// 类型：试验使用
            recordEntity.setStartTime(DateUtil.timeMinuteFormat(cells.get(starTimeIndex).getValue().toString()));// 开始时间
            recordEntity.setEndTime(DateUtil.timeMinuteFormat(cells.get(endTimeIndex).getValue().toString()));
            recordEntity.setTemperature(cells.get(wdIndex).getValue().toString());// 温度
            recordEntity.setHumidity(cells.get(sdIndex).getValue().toString());// 湿度
            recordEntity.setBeforeStatus(cells.get(useStateIndex).getValue().toString());// 使用前状态
            recordEntity.setAfterStatus(cells.get(beforeStateIndex).getValue().toString());// 使用后状态
            recordEntity.setUser(cells.get(userIndex).getValue().toString());// 操作人
            recordEntity.setTime(new Date());
            //根据任务单号获取任务id
            Long taskId = taskService.getIdByCode(cells.get(taskCodeIndex).getValue().toString());
            if (taskId == null){
                failList.put(num+":"+cells.get(taskCodeIndex).getValue().toString(),"任务单号不存在");
                num ++;
                continue;
            }
            recordEntity.setTaskId(taskId);//任务id
            recordEntity.setTaskCode(cells.get(taskCodeIndex).getValue().toString());//任务单号
            recordEntity.setParallel(1);//并行数量
            list.add(recordEntity);
            successList.put(num+":"+cells.get(taskCodeIndex).getValue().toString(),"成功");
            log.info("设备使用记录index:{}",num);
            num = num+1;
        }
        log.info("仪器设备使用记录导入失败记录条数:{}", failList.size());
        log.info("仪器设备使用记录导入失败记录:{}", JSON.toJSONString(failList));
        log.info("仪器设备使用记录导入成功记录条数:{}", successList.size());
        log.info("仪器设备使用记录导入成功记录:{}", JSON.toJSONString(successList));
        instrumentRecordEntityMapper.batchInsert(list);
        return ResultUtil.success("导入成功");
    }

    /**
     * 导入设备使用记录
     * @return
     */
    @PostMapping("importRecord1")
    public Result importRecord1() throws Exception{
        Map<String,String> successList = new HashMap<>();
        Map<String,String> failList = new HashMap<>();
        //读取excel
        com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook("D:\\Users\\Administrator\\Desktop\\使用记录.xlsx");
        Worksheet worksheet = workbook.getWorksheets().get(0);
        Cells cells = worksheet.getCells();
        //遍历excel，根据编号查询
        int num = 2;
        List<InstrumentRecordEntity> list = Lists.newArrayList();
        List<Integer> integerList = Arrays.asList(421
                ,302
                ,310
                ,285
                ,304
                ,423
                ,291
                ,289
                ,296
                ,309
                ,6
                ,419
                ,294
                ,288
                ,301
                ,286
                ,306
                ,420
                ,290
                ,299
                ,292
                ,297
                ,422
                ,308
                ,303
                ,307
                ,295
                ,424
                ,287
                ,300
                ,298
                ,305
                ,311
                ,293);
        while (num <= 424) {
            if (integerList.contains(num)){
                InstrumentRecordEntity recordEntity = new InstrumentRecordEntity();
                String deviceCodeIndex = "A" + num;
                String taskCodeIndex = "B" + num;
                String itemNameIndex = "C" + num;
                String wdIndex = "D" + num;
                String sdIndex = "E" + num;
                String useStateIndex = "F" + num;
                String beforeStateIndex = "G" + num;
                String userIndex = "H" + num;
                String starTimeIndex = "I" + num;
                String endTimeIndex = "J" + num;
                //根据设备编号查询设备id
                LambdaQueryWrapper<TestInstrument> queryWrapper = new LambdaQueryWrapper<>();
                String value = cells.get(deviceCodeIndex).getValue().toString();
                queryWrapper.eq(TestInstrument::getCode, value);
                List<TestInstrument> testInstruments = testInstrumentService.list(queryWrapper);
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(testInstruments)){
                    recordEntity.setInstrumentId(Long.parseLong(testInstruments.get(0).getId() + ""));// 仪器id
                }
                recordEntity.setId(GenID.getID());// 记录id
                //根据任务单号和参数名称获取检测项的id
                List<Integer> itemsIds = taskService.selectList1(cells.get(taskCodeIndex).getValue().toString());
                if (org.apache.commons.collections.CollectionUtils.isNotEmpty(itemsIds)){
                    //判断参数名cells.get(itemNameIndex).getValue().toString()

                    recordEntity.setEscRelId(Long.parseLong(itemsIds.get(0)+""));// 检测项主键
                }
                recordEntity.setType("试验使用");// 类型：试验使用
                recordEntity.setStartTime(DateUtil.timeMinuteFormat(cells.get(starTimeIndex).getValue().toString()));// 开始时间
                recordEntity.setEndTime(DateUtil.timeMinuteFormat(cells.get(endTimeIndex).getValue().toString()));
                recordEntity.setTemperature(cells.get(wdIndex).getValue().toString());// 温度
                recordEntity.setHumidity(cells.get(sdIndex).getValue().toString());// 湿度
                recordEntity.setBeforeStatus(cells.get(useStateIndex).getValue().toString());// 使用前状态
                recordEntity.setAfterStatus(cells.get(beforeStateIndex).getValue().toString());// 使用后状态
                recordEntity.setUser(cells.get(userIndex).getValue().toString());// 操作人
                recordEntity.setTime(new Date());
                //根据任务单号获取任务id
                Long taskId = taskService.getIdByCode(cells.get(taskCodeIndex).getValue().toString());
                if (taskId != null){
                    recordEntity.setTaskId(taskId);//任务id
                }
                recordEntity.setTaskCode(cells.get(taskCodeIndex).getValue().toString());//任务单号
                recordEntity.setParallel(1);//并行数量
                list.add(recordEntity);
                successList.put(num+":"+cells.get(taskCodeIndex).getValue().toString(),"成功");
                log.info("设备使用记录index:{}",num);
                num = num+1;
            }else {
                num++;
            }
        }
        log.info("待插入的使用记录:{}",JSON.toJSONString(list));
        //instrumentRecordEntityMapper.batchInsert(list);
        return ResultUtil.success("导入成功");
    }
}
