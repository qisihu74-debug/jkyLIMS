package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.config.HkConfig;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.*;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.HKDoorLaboratoryInstrumentRelService;
import com.lims.manage.erp.service.HkDoorService;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.HkUtils;
import com.lims.manage.erp.vo.LabelValueVo;
import org.springframework.beans.factory.annotation.Autowired;
import com.lims.manage.erp.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2024-09-14 11:16
 * @Copyright © 河南交科院
 */
@Service
public class HkDoorServiceImpl extends ServiceImpl<HkDoorDao, HkDoor> implements HkDoorService {
    @Autowired
    private HkDoorDao hkDoorDao;
    @Autowired
    private HkConfig hkConfig;
    @Autowired
    private HKPersonDoorProvisionalAuthorityRelEntityMapper authorityRelEntityMapper;
    @Autowired
    private HKDoorLaboratoryInstrumentRelEntityMapper hkDoorLaboratoryInstrumentRelEntityMapper;
    @Autowired
    private HKDoorLaboratoryRelEntityMapper hkDoorLaboratoryRelEntityMapper;
    @Autowired
    private HKPersonUserRelEntityMapper hkPersonUserRelEntityMapper;
    @Autowired
    private HKPersonDoorProvisionalAuthorityRelEntityMapper hkPersonDoorProvisionalAuthorityRelEntityMapper;
    @Autowired
    private HKDoorLaboratoryInstrumentRelService hkDoorLaboratoryInstrumentRelService;
    @Autowired
    private HkCameraDao hkCameraDao;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private DeviceEntityMapper deviceEntityMapper;
    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private TestEntrustedTaskRelDao testEntrustedTaskRelDao;

    @Override
    public PageInfo<HkDoor> doorList(Integer pageNum, Integer pageSize, String name, String position, String state) {
        PageHelper.clearPage();
        PageHelper.startPage(pageNum, pageSize);
        List<HkDoor> list = hkDoorDao.doorList(name, position, state);
        PageInfo<HkDoor> pageInfo = new PageInfo(list);
        return pageInfo;
    }

    @Override
    public  Map<String, Object> doorDetails(DoorDetailReq doorDetailReq) {
        Map<String, Object> map = HkUtils.doorEvents(hkConfig.getDoorEvents(), doorDetailReq);
        return map;
    }

    /**
     * 编辑门禁与实验室id 进行关联
     *
     * @param hkDoorLaboratoryRelEntity
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result editDoorLaboratoryRel(HKDoorLaboratoryRelEntity hkDoorLaboratoryRelEntity) {
        // 先删除 再新增关系
        LambdaQueryWrapper<HKDoorLaboratoryRelEntity> deteWapper = new LambdaQueryWrapper<>();
        deteWapper.eq(HKDoorLaboratoryRelEntity::getIndexCode, hkDoorLaboratoryRelEntity.getIndexCode());
        hkDoorLaboratoryRelEntityMapper.delete(deteWapper);

        // 新增
        hkDoorLaboratoryRelEntityMapper.insert(hkDoorLaboratoryRelEntity);

        return ResultUtil.success("操作成功");
    }

    /**
     * 编辑人员与userid 进行关联
     *
     * @param hkPersonUserRelEntity
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result editPersonUserRel(HKPersonUserRelEntity hkPersonUserRelEntity) {

        if (hkPersonUserRelEntity == null) {
            return ResultUtil.error("缺少必填参数");
        }

        if (StringUtils.isEmpty(hkPersonUserRelEntity.getPersonId())) {
            return ResultUtil.error("缺少必填参数");
        }
        if (hkPersonUserRelEntity.getUserId() == null) {
            return ResultUtil.error("缺少必填参数");
        }

        LambdaQueryWrapper<HKPersonUserRelEntity> perrsonWrapper = new LambdaQueryWrapper<>();
        perrsonWrapper.eq(HKPersonUserRelEntity::getUserId, hkPersonUserRelEntity.getUserId());
        perrsonWrapper.notIn(HKPersonUserRelEntity::getPersonId, hkPersonUserRelEntity.getPersonId());
        List<HKPersonUserRelEntity> userlist = hkPersonUserRelEntityMapper.selectList(perrsonWrapper);
        if (CollectionUtil.isNotEmpty(userlist)) {
            // 遍历数据 进行比较
            for (HKPersonUserRelEntity data : userlist) {
                if (!data.getPersonId().equals(hkPersonUserRelEntity.getPersonId())) {
                    // 抛出
                    return ResultUtil.error("用户已绑定");
                }
            }
        }


        // 先删除 再新增关系
        LambdaQueryWrapper<HKPersonUserRelEntity> deteWapper = new LambdaQueryWrapper<>();
        deteWapper.eq(HKPersonUserRelEntity::getPersonId, hkPersonUserRelEntity.getPersonId());
        hkPersonUserRelEntityMapper.delete(deteWapper);

        // 新增
        hkPersonUserRelEntityMapper.insert(hkPersonUserRelEntity);

        return ResultUtil.success("操作成功");
    }

    /**
     * 进行监控与试验室和仪器关系授权
     *
     * @param indexCode
     * @param testLaboratoryId
     * @param ids
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result impowerDoorLaboratoryInstruments(String indexCode, Integer testLaboratoryId, Integer[] ids) {

        if (StringUtils.isEmpty(indexCode)) {
            return ResultUtil.error("缺少必填参数");
        }
        if (testLaboratoryId == null) {
            return ResultUtil.error("缺少必填参数");
        }
        if (ids == null || ids.length == 0) {
            return ResultUtil.error("仪器不能为空");
        }


        // 查询监控是否存在
        LambdaQueryWrapper<CameraInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CameraInfo::getIndexCode, indexCode);
        List<CameraInfo> cameraInfoList = hkCameraDao.selectList(queryWrapper);
        if (CollectionUtil.isEmpty(cameraInfoList)) {
            return ResultUtil.error("监控标识不存在");
        }

        // 删除绑定关系
        LambdaQueryWrapper<HKCameraLaboratoryInstrumentRelEntity> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(HKCameraLaboratoryInstrumentRelEntity::getCamera, indexCode);
        hkDoorLaboratoryInstrumentRelService.remove(deleteWrapper);

        HKCameraLaboratoryInstrumentRelEntity data = new HKCameraLaboratoryInstrumentRelEntity();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < ids.length; i++) {
            stringBuffer.append(ids[i]);
            stringBuffer.append(",");
        }
        data.setCamera(indexCode);
        data.setTestLaboratoryId(testLaboratoryId);
        if (StringUtils.isNotEmpty(stringBuffer.toString())) {
            data.setTestInstrumentId(stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString());
        }
        hkDoorLaboratoryInstrumentRelService.save(data);
        return ResultUtil.success("操作成功");
    }


    @Override
    public Map<String, Object> pictures(String svrIndexCode, String picUri) {
        return HkUtils.doorPictures(hkConfig.getDoorPictures(),svrIndexCode,picUri);
    }

    @Override
    public Boolean temporaryVisit(Integer id) {
        HKPersonDoorProvisionalAuthorityRelEntity byId = authorityRelEntityMapper.selectById(id);
        if (byId == null){
            return false;
        }
        //组装数据
        HkDoorReq hkDoorReq = new HkDoorReq();
        hkDoorReq.setStartTime(byId.getStartTime());
        hkDoorReq.setEndTime(byId.getEndTime());
        List<PersonDoorReq> personDatas = Lists.newArrayList();
        PersonDoorReq personDoorReq = new PersonDoorReq();
        List<String> personId = Lists.newArrayList();
        personId.add(byId.getPersonId());
        personDoorReq.setIndexCodes(personId);
        personDatas.add(personDoorReq);
        hkDoorReq.setPersonDatas(personDatas);
        List<ResourceInfo> resourceInfos = Lists.newArrayList();
        String indexCode = byId.getIndexCode();
        if (StringUtils.isEmpty(indexCode)){
            return false;
        }
        String[] split = indexCode.split(",");
        for (String s:split){
            ResourceInfo resourceInfo = new ResourceInfo();
            resourceInfo.setResourceIndexCode(s);
            resourceInfos.add(resourceInfo);
        }
        hkDoorReq.setResourceInfos(resourceInfos);
        //授权对象
        HkGrantDoorReq hkGrantDoorReq = new HkGrantDoorReq();
        hkGrantDoorReq.setResourceInfos(resourceInfos);
        //人员门禁设备绑定
        Map<String, Object> map = HkUtils.personBandDoor(hkConfig.getPersonBandDoor(), hkDoorReq);
        if (map != null){
            String msg = map.get("msg").toString();
            if ("success".equals(msg)){
                //权限下发
                Map<String, Object> grant = HkUtils.personGrant(hkConfig.getGrant(), hkGrantDoorReq);
                if (grant != null) {
                    String msg1 = map.get("msg").toString();
                    if ("success".equals(msg1)) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }else {
                return false;
            }
        }else {
            return false;
        }
    }

    @Override
    public Boolean cancelVisit(String id) {
        HKPersonDoorProvisionalAuthorityRelEntity byId = authorityRelEntityMapper.selectById(id);
        if (byId == null){
            return false;
        }
        HkDoorReq hkDoorReq = new HkDoorReq();
        List<PersonDoorReq> personDatas = Lists.newArrayList();
        PersonDoorReq personDoorReq = new PersonDoorReq();
        List<String> personId = Lists.newArrayList();
        personId.add(byId.getPersonId());
        personDoorReq.setIndexCodes(personId);
        personDatas.add(personDoorReq);
        hkDoorReq.setPersonDatas(personDatas);
        List<ResourceInfo> resourceInfos = Lists.newArrayList();
        String indexCode = byId.getIndexCode();
        if (StringUtils.isEmpty(indexCode)){
            return false;
        }
        String[] split = indexCode.split(",");
        for (String s:split){
            ResourceInfo resourceInfo = new ResourceInfo();
            resourceInfo.setResourceIndexCode(s);
            resourceInfos.add(resourceInfo);
        }
        hkDoorReq.setResourceInfos(resourceInfos);
        Map<String, Object> map = HkUtils.cancleBandDoor(hkConfig.getCancelBandDoor(), hkDoorReq);
        if (map != null){
            String msg = map.get("msg").toString();
            if ("success".equals(msg)){
                return true;
            }else {
                return false;
            }
        }else {
            return false;
        }
    }

    @Override
    public Map<String, Object> doorState(List<String> indexCodes) {
        DoorStateReq doorStateReq = new DoorStateReq();
        doorStateReq.setDoorIndexCodes(indexCodes);
        return HkUtils.doorState(hkConfig.getDoorState(),doorStateReq);
    }

    /**
     * 临时访问列表
     *
     * @param personId
     * @return
     */
    @Override
    public Result getTemporaryAccessList(String personId) {
        if (StringUtils.isEmpty(personId)) {
            return ResultUtil.error("缺少必填参数");
        }
        LambdaQueryWrapper<HKPersonDoorProvisionalAuthorityRelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HKPersonDoorProvisionalAuthorityRelEntity::getPersonId, personId);
        queryWrapper.orderByDesc(HKPersonDoorProvisionalAuthorityRelEntity::getCreateTime);
        List<HKPersonDoorProvisionalAuthorityRelEntity> list = hkPersonDoorProvisionalAuthorityRelEntityMapper.selectList(queryWrapper);
        // 进行startTime 与 endTime 进行转换
        if (CollectionUtil.isNotEmpty(list)) {
            for (HKPersonDoorProvisionalAuthorityRelEntity data : list) {
                // 进行转换 2024-09-10T11:30:08.000+08:00 转成 年月日 时分秒
                if (StringUtils.isNotEmpty(data.getStartTime())) {
                    data.setStartTime(DateUtil.getDateStrFromISO8601Timestamp(data.getStartTime()));
                }
                if (StringUtils.isNotEmpty(data.getEndTime())) {
                    data.setEndTime(DateUtil.getDateStrFromISO8601Timestamp(data.getEndTime()));
                }
            }
        }
        return ResultUtil.success(list);
    }

    /**
     * 新增：临时访问
     *
     * @param data
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result addtemporaryVisit(HKPersonDoorProvisionalAuthorityRelEntity data) {

        if (data == null) {
            return ResultUtil.error("缺少必填参数");
        }
        if (StringUtils.isEmpty(data.getLaboratoryMessage())) {
            return ResultUtil.error("缺少必填参数");
        }

        // 进行转换： 年月日 时分秒 转成 UTC：东八区存放
        if (StringUtils.isNotEmpty(data.getStartTime())) {
            data.setStartTime(DateUtil.getISO8601TimestampFromDateStr(data.getStartTime()));
        }
        if (StringUtils.isNotEmpty(data.getEndTime())) {
            data.setEndTime(DateUtil.getISO8601TimestampFromDateStr(data.getEndTime()));
        }
        data.setCreateTime(new Date());
        data.setState(0);

        // 通过实验室id 获取门禁信息
        LambdaQueryWrapper<HKDoorLaboratoryRelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HKDoorLaboratoryRelEntity::getTestLaboratoryId, data.getLaboratoryMessage());

        List<HKDoorLaboratoryRelEntity> list = hkDoorLaboratoryRelEntityMapper.selectList(queryWrapper);

        if (CollectionUtil.isNotEmpty(list)) {
            StringBuffer stringBuffer = new StringBuffer();
            for (HKDoorLaboratoryRelEntity doorLaboratoryRelEntity : list) {
                stringBuffer.append(doorLaboratoryRelEntity.getIndexCode() + ",");
            }
            data.setIndexCode(stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString());
        }

        hkPersonDoorProvisionalAuthorityRelEntityMapper.insert(data);
        // 授权
        Boolean msg = temporaryVisit(data.getId());

        if (!msg) {
            int i = 1 / 0;
        }

        return ResultUtil.success("操作成功");
    }

    /**
     * 通过实验室id 获取门禁列表
     *
     * @param testLaboratoryId
     * @return
     */
    @Override
    public Result getAccessControlStatusList(String testLaboratoryId) {

        if (StringUtils.isEmpty(testLaboratoryId)) {
            return ResultUtil.error("缺少必填参数");
        }

        LambdaQueryWrapper<HKDoorLaboratoryRelEntity> doorLaboratoryRelWrapper = new LambdaQueryWrapper<>();
        doorLaboratoryRelWrapper.eq(HKDoorLaboratoryRelEntity::getTestLaboratoryId, testLaboratoryId);

        List<HKDoorLaboratoryRelEntity> list = hkDoorLaboratoryRelEntityMapper.selectList(doorLaboratoryRelWrapper);
        if (CollectionUtil.isEmpty(list)) {
            return ResultUtil.success(null);
        }

        List<String> indexCodes = list.stream().map(HKDoorLaboratoryRelEntity -> HKDoorLaboratoryRelEntity.getIndexCode()).collect(Collectors.toList());

        // 获取 门禁信息
        LambdaQueryWrapper<HkDoor> doorWrapper = new LambdaQueryWrapper<>();
        doorWrapper.select(HkDoor::getName, HkDoor::getIndexCode);
        doorWrapper.in(HkDoor::getIndexCode, indexCodes);
        List<HkDoor> doorList = this.baseMapper.selectList(doorWrapper);

        // 通过门禁集合 获取对应状态
        Map<String, Object> objectMap = doorState(indexCodes);
        Map<String, Object> authDoorMap = (Map<String, Object>) objectMap.get("data");
        List<Map<String, Object>> hkDoorList = (List<Map<String, Object>>) authDoorMap.get("authDoorList");

        // 处理业务数据
        if (CollectionUtil.isNotEmpty(hkDoorList)) {
            for (Map<String, Object> map : hkDoorList) {
                String doorIndexCode = map.get("doorIndexCode").toString();
                Integer doorState = Integer.parseInt(map.get("doorState").toString());
                for (HkDoor hkDoor : doorList) {
                    if (doorIndexCode.equals(hkDoor.getIndexCode())) {
                        hkDoor.setDoorState(doorState);
                    }
                }
            }
        }

        return ResultUtil.success(doorList);

    }

    // 根据任务单详情 进行获取 对应检测人、记录人、复核人、报告制作人、辅助人员
    public static Map<String, Object> getUserIds(TaskTestEntity taskTestEntity) {

        Map<String, Object> map = new HashMap<>();

        // 获取 人员集合
        Set<Long> userIDs = new HashSet<>();

        // 检测人
        if (org.apache.commons.lang.StringUtils.isNotEmpty(taskTestEntity.getInspector())) {
            String[] split = taskTestEntity.getInspector().split(",");
            if (split != null) {
                for (String jcr : split) {
                    userIDs.add(Long.valueOf(jcr.split("&")[1]));
                }
            }
        }

        // 记录人
        if (org.apache.commons.lang.StringUtils.isNotEmpty(taskTestEntity.getRecorder())) {
            String[] split = taskTestEntity.getRecorder().split(",");
            if (split != null) {
                for (String jcr : split) {
                    userIDs.add(Long.valueOf(jcr.split("&")[1]));
                }
            }
        }

        // 复核人
        if (org.apache.commons.lang.StringUtils.isNotEmpty(taskTestEntity.getReviewer())) {
            String[] split = taskTestEntity.getReviewer().split(",");
            if (split != null) {
                for (String jcr : split) {
                    userIDs.add(Long.valueOf(jcr.split("&")[1]));
                }
            }
        }

        // 报告制作人
        if (org.apache.commons.lang.StringUtils.isNotEmpty(taskTestEntity.getReportProducer())) {
            String[] split = taskTestEntity.getReportProducer().split(",");
            if (split != null) {
                for (String jcr : split) {
                    userIDs.add(Long.valueOf(jcr.split("&")[1]));
                }
            }
        }

        // 辅助人员
        if (org.apache.commons.lang.StringUtils.isNotEmpty(taskTestEntity.getAuxiliaryPersonnel())) {
            String[] split = taskTestEntity.getAuxiliaryPersonnel().split(",");
            if (split != null) {
                for (String jcr : split) {
                    userIDs.add(Long.valueOf(jcr.split("&")[1]));
                }
            }
        }

        // 见习生：实习的新手
        if (org.apache.commons.lang.StringUtils.isNotEmpty(taskTestEntity.getProbationer())) {
            String[] split = taskTestEntity.getProbationer().split(",");
            if (split != null) {
                for (String jcr : split) {
                    userIDs.add(Long.valueOf(jcr.split("&")[1]));
                }
            }
        }

        // 实习生
        if (org.apache.commons.lang.StringUtils.isNotEmpty(taskTestEntity.getInterns())) {
            String[] split = taskTestEntity.getInterns().split(",");
            if (split != null) {
                for (String jcr : split) {
                    userIDs.add(Long.valueOf(jcr.split("&")[1]));
                }
            }
        }

        map.put("userIDs", userIDs);
        return map;
    }

    /**
     * 任务单与人员授权
     *
     * @param taskId
     * @return
     */
    @Override
    public Result taskListAuthorization(String taskId) {

        // 获取任务单信息：
        TaskTestEntity taskTestEntity = taskMapper.selectTaskEntity(Long.parseLong(taskId));

        if (taskTestEntity == null) {
            return ResultUtil.error("任务单为空");
        }

        // 获取最终报告 签发完成 则抛出信息
        ReportRecordEntity recordEntity = reportMapper.getDetailByEntrustId(taskTestEntity.getEntrustmentId());
        if (recordEntity != null) {
            if (recordEntity.getState() != null && Integer.parseInt(recordEntity.getState()) >= 6) {
                return ResultUtil.error("操作失败 报告单已签发完成");
            }
        }
        // 获取 检测人、记录人
        Set<Long> userIDs = new HashSet<>();
        Map<String, Object> map = getUserIds(taskTestEntity);
        userIDs = (Set<Long>) map.get("userIDs");


        // 获取 对应的 personIds
        LambdaQueryWrapper<HKPersonUserRelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(HKPersonUserRelEntity::getUserId, userIDs);
        List<HKPersonUserRelEntity> personEntities = hkPersonUserRelEntityMapper.selectList(queryWrapper);

        if (CollectionUtil.isEmpty(personEntities)) {
            return ResultUtil.error("检测人记录人 无人员绑定");
        }

        // 人员集合
        List<String> personIds = personEntities.stream().map(HKPersonUserRelEntity::getPersonId).collect(Collectors.toList());


        // 获取 门禁集合

        // 通过任务单id 获取 检测项绑定的仪器
        List<Integer> instrumentIds = taskMapper.getDistinctInstrumentIds(Long.parseLong(taskId));
        if (CollectionUtil.isEmpty(instrumentIds)) {
            return ResultUtil.error("任务单下 无仪器绑定");
        }

        // 2、获取 仪器的 实验室id信息
        LambdaQueryWrapper<DeviceEntity> deviceWrapper = new LambdaQueryWrapper<>();
        deviceWrapper.in(DeviceEntity::getId, instrumentIds);
        deviceWrapper.isNotNull(DeviceEntity::getLaboratoryId);
        List<DeviceEntity> deviceEntities = deviceEntityMapper.selectList(deviceWrapper);
        if (CollectionUtil.isEmpty(deviceEntities)) {
            return ResultUtil.error("仪器下 无实验室绑定");
        }
        List<Integer> laboratoryIds = deviceEntities.stream().map(DeviceEntity::getLaboratoryId).collect(Collectors.toList());

        // 3、通过实验室 获取 对应的门禁
        LambdaQueryWrapper<HKDoorLaboratoryRelEntity> doorLaboratoryWrapper = new LambdaQueryWrapper<>();
        doorLaboratoryWrapper.in(HKDoorLaboratoryRelEntity::getTestLaboratoryId, laboratoryIds);
        List<HKDoorLaboratoryRelEntity> hkDoorLaboratoryRelEntities = hkDoorLaboratoryRelEntityMapper.selectList(doorLaboratoryWrapper);

        if (CollectionUtil.isEmpty(hkDoorLaboratoryRelEntities)) {
            return ResultUtil.error("实验室下 无门禁绑定");
        }

        // 门禁集合
        List<String> indexCodes = hkDoorLaboratoryRelEntities.stream().map(HKDoorLaboratoryRelEntity::getIndexCode).collect(Collectors.toList());
        // 获取任务单 最终报告流转日期 进行 下发即可
        LambdaQueryWrapper<TestEntrustedTaskRelEntity> entityWrapper = new LambdaQueryWrapper<>();
        entityWrapper.eq(TestEntrustedTaskRelEntity::getEntrustId, taskTestEntity.getEntrustmentId());
        // 获取 最终报告流转信息
        entityWrapper.eq(TestEntrustedTaskRelEntity::getType, 0);
        entityWrapper.orderByAsc(TestEntrustedTaskRelEntity::getId);
        List<TestEntrustedTaskRelEntity> entrustedTaskRelEntities = testEntrustedTaskRelDao.selectList(entityWrapper);
        if (CollectionUtil.isEmpty(entrustedTaskRelEntities)) {
            return ResultUtil.error("任务单下 无最终报告流转时间");
        }
        TestEntrustedTaskRelEntity taskRelEntity = entrustedTaskRelEntities.get(0);
        Map<String, String> timeCyclemap = DateUtil.returnTimeCycle(taskRelEntity.getTaskFlowDate());

        String startISO8601Time = timeCyclemap.get("startTime");
        String endISO8601Time = timeCyclemap.get("endTime");

        String startTime = DateUtil.getDateStrFromISO8601Timestamp(startISO8601Time);
        String endTime = DateUtil.getDateStrFromISO8601Timestamp(endISO8601Time);

        // 进行比较: endTime < startTime
        if (DateUtil.timeMinuteFormat(endTime).before(DateUtil.timeMinuteFormat(startTime))) {
            return ResultUtil.error("授权失败-流转时间异常 " + endTime);
        }

        // 执行： 任务单授权门禁权限(门禁和人员绑定-下发权限)
        HkUtils.taskGrantDoor(hkConfig.getPersonBandDoor(), hkConfig.getGrant(), personIds, indexCodes, timeCyclemap);

        return ResultUtil.success("操作成功");

    }


}
