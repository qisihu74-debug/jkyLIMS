package com.lims.manage.erp.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.config.HkConfig;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.HKPersonDoorProvisionalAuthorityRelEntityMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.HkCameraService;
import com.lims.manage.erp.service.HkDoorService;
import com.lims.manage.erp.service.HkPersonService;
import com.lims.manage.erp.util.HkUtils;
import com.lims.manage.erp.util.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.controller
 * @desc
 * @date 2024-09-13 15:13
 * @Copyright © 河南交科院
 */
@RestController
@RequestMapping("hk")
public class HkController {
    @Autowired
    private HkConfig hkConfig;
    @Autowired
    private HkPersonService hkPersonService;
    @Autowired
    private HkDoorService hkDoorService;
    @Autowired
    private HkCameraService hkCameraService;
    @Autowired
    private HKPersonDoorProvisionalAuthorityRelEntityMapper hkPersonDoorProvisionalAuthorityRelEntityMapper;

    /**
     * 同步海康人员信息
     * @return
     */
    @GetMapping("pullPerson")
    public Result pullPerson(){
        Map<String, Object> map = HkUtils.personList(hkConfig.getPerson());
        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(map));
        String data = jsonObject.get("data").toString();
        if (StringUtils.isNotEmpty(data)){
            JSONObject jsonObject1 = JSONObject.parseObject(data);
            if (jsonObject1 != null){
                String toString = jsonObject1.get("list").toString();
                List<HkPerson> list = JSON.parseArray(toString, HkPerson.class);
                if (CollectionUtil.isNotEmpty(list)){
                    hkPersonService.saveOrUpdateBatch(list);
                    return ResultUtil.success("同步海康人员成功",null);
                }
            }
        }
        return ResultUtil.error("同步失败",null);
    }

    /**
     * 同步海康门禁设备信息
     * @return
     */
    @GetMapping("pullDoor")
    public Result pullDoor(){
        Map<String, Object> map = HkUtils.doorSearch(hkConfig.getDoorSearch());
        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(map));
        String data = jsonObject.get("data").toString();
        if (StringUtils.isNotEmpty(data)){
            JSONObject jsonObject1 = JSONObject.parseObject(data);
            if (jsonObject1 != null){
                String toString = jsonObject1.get("list").toString();
                List<HkDoor> list = JSON.parseArray(toString, HkDoor.class);
                if (CollectionUtil.isNotEmpty(list)){
                    hkDoorService.saveOrUpdateBatch(list);
                    return ResultUtil.success("同步海康门禁成功",null);
                }
            }
        }
        return ResultUtil.error("同步失败",null);
    }

    /**
     * 同步海康摄像头设备信息
     * @return
     */
    @GetMapping("pullCamera")
    public Result pullCamera(){
        Map<String, Object> map = HkUtils.cameraSearch(hkConfig.getCameraSearch());
        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(map));
        String data = jsonObject.get("data").toString();
        if (StringUtils.isNotEmpty(data)){
            JSONObject jsonObject1 = JSONObject.parseObject(data);
            if (jsonObject1 != null){
                String toString = jsonObject1.get("list").toString();
                List<CameraInfo> list = JSON.parseArray(toString, CameraInfo.class);
                if (CollectionUtil.isNotEmpty(list)){
                    hkCameraService.saveOrUpdateBatch(list);
                    return ResultUtil.success("同步海康摄像头成功",null);
                }
            }
        }
        return ResultUtil.error("同步失败",null);
    }

    /**
     * 获取监控点预览取流URLv2
     * @param cameraIndexCode
     * @return
     */
    @GetMapping("camerasPreviewURLs")
    public Result camerasPreviewURLs(String cameraIndexCode){
        if (StringUtils.isEmpty(cameraIndexCode)){
            return ResultUtil.error("缺少参数");
        }
        Map<String, Object> map = HkUtils.camerasPreviewURLs(hkConfig.getVideoPreviewURLs(), cameraIndexCode);
        return ResultUtil.success(map);
    }

    /**
     * 视频回放
     * @param cameraIndexCode
     * @param startTime
     * @param endTime
     * @return
     */
    @GetMapping("playbackURLs")
    public Result playbackURLs(String cameraIndexCode,String startTime,String endTime){
        if (StringUtils.isEmpty(cameraIndexCode)){
            return ResultUtil.error("缺少参数");
        }
        if (StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)){
            return ResultUtil.error("请选择查看监控视频的时间范围");
        }
        Map<String, Object> map = HkUtils.playbackURLs(hkConfig.getVideoPlaybackURLs(), cameraIndexCode,startTime,endTime);
        return ResultUtil.success(map);
    }

    /**
     * 海康人员门禁绑定发送
     * @param hkDoorReq
     * @return
     */
    @PostMapping("personBandDoor")
    public Result personBandDoor(){
        HkDoorReq hkDoorReq = new HkDoorReq();
        hkDoorReq.setStartTime("2024-10-28T17:30:08.000+08:00");
        hkDoorReq.setEndTime("2024-12-26T17:30:08.000+08:00");
        List<PersonDoorReq> personDatas = Lists.newArrayList();
        PersonDoorReq personDoorReq = new PersonDoorReq();
        List<String> stringList = Lists.newArrayList();
//        stringList.add("075f15376c7f4432ac78ce2d57ce0740");//院长
//        stringList.add("1c9b8024c0da420597e07491979c0a75");//许部长
//        stringList.add("a3ec6e5b1f8f4183afd55ba71d1fe44e");//赵文军
        stringList.add("d2892d4254da4d209eda49c4efadf798");//本人
//        stringList.add("5aa6c61cea064fd4a79c95072449d209");//石小于
//        stringList.add("4fb2c350f8144a9f80e4f21b64ceae1c");//王琰
        personDoorReq.setIndexCodes(stringList);
        personDatas.add(personDoorReq);
        hkDoorReq.setPersonDatas(personDatas);
        //查询所有门禁
        LambdaQueryWrapper<HkDoor> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(HkDoor::getName,"实验楼505人脸");
        List<HkDoor> list = hkDoorService.list(queryWrapper);
        List<ResourceInfo> reqResourceInfos = Lists.newArrayList();
        for (HkDoor hkDoor :list){
            ResourceInfo resourceInfo = new ResourceInfo();
            resourceInfo.setResourceIndexCode(hkDoor.getIndexCode());
            reqResourceInfos.add(resourceInfo);
        }
        hkDoorReq.setResourceInfos(reqResourceInfos);
        if (CollectionUtils.isEmpty(hkDoorReq.getPersonDatas())){
            return ResultUtil.error("请选择绑定的人员");
        }
        if (CollectionUtils.isEmpty(hkDoorReq.getResourceInfos())){
            return ResultUtil.error("请选择绑定的门禁");
        }
        Map<String, Object> map = HkUtils.personBandDoor(hkConfig.getPersonBandDoor(),hkDoorReq);
        if (map != null){
            String msg = map.get("msg").toString();
            if ("success".equals(msg)){
                return ResultUtil.success("海康人员门禁绑定成功",JSON.toJSONString(map));
            }else {
                return ResultUtil.error("海康人员门禁绑定失败");
            }
        }else {
            return ResultUtil.error("海康人员门禁绑定失败");
        }
    }

    /**
     * 海康门禁授权下发发送
     * @param hkGrantDoorReq
     * @return
     */
    @PostMapping("personGrant")
    public Result personGrant(){
        HkGrantDoorReq hkGrantDoorReq = new HkGrantDoorReq();
        hkGrantDoorReq.setTaskType(4);
        List<ResourceInfo> reqResourceInfos = Lists.newArrayList();
        LambdaQueryWrapper<HkDoor> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like(HkDoor::getName,"实验楼1")
                .or()
                .like(HkDoor::getName,"实验楼2")
                .or()
                .like(HkDoor::getName,"实验楼3")
                .or()
                .like(HkDoor::getName,"实验楼511人脸识别")
                .or()
                .like(HkDoor::getName,"实验楼4");
        List<HkDoor> list = hkDoorService.list(queryWrapper);

        List<HkDoor> list1 = Lists.newArrayList();
        List<HkDoor> list2 = Lists.newArrayList();
        for (int i=0;i<list.size();i++){
            if (i<=99){
                list1.add(list.get(i));
            }else {
                list2.add(list.get(i));
            }
        }

        for (HkDoor hkDoor :list1){
            ResourceInfo resourceInfo = new ResourceInfo();
            resourceInfo.setResourceIndexCode(hkDoor.getIndexCode());
            resourceInfo.setResourceType("door");
            reqResourceInfos.add(resourceInfo);
        }
        hkGrantDoorReq.setResourceInfos(reqResourceInfos);

        if (CollectionUtils.isEmpty(hkGrantDoorReq.getResourceInfos())){
            return ResultUtil.error("请选择下发权限的门禁");
        }
        Map<String, Object> map = HkUtils.personGrant(hkConfig.getGrant(),hkGrantDoorReq);
        if (map != null) {
            String msg = map.get("msg").toString();
            if ("success".equals(msg)) {
                return ResultUtil.success("人员下发门禁权限成功", JSON.toJSONString(map));
            } else {
                return ResultUtil.error("人员下发门禁权限失败");
            }
        } else {
            return ResultUtil.error("人员下发门禁权限失败");
        }
    }

    /**
     * 海康人员列表查询
     * @param pageNum
     * @param pageSize
     * @param name
     * @param mobile
     * @param state
     * @return
     */
    @GetMapping("personList")
    public Result personList(Integer pageNum,Integer pageSize,String name,String mobile,String state){
        if (pageNum == null || pageSize == null){
            return ResultUtil.error("缺少分页参数");
        }
        PageInfo<HkPerson> pageInfo = hkPersonService.personList(pageNum,pageSize,name,mobile,state);
        return ResultUtil.success(pageInfo);
    }

    /**
     * 海康人员列表查询
     * @param pageNum
     * @param pageSize
     * @param name
     * @param position
     * @param state
     * @return
     */
    @GetMapping("doorList")
    public Result doorList(Integer pageNum,Integer pageSize,String name,String position,String state){
        if (pageNum == null || pageSize == null){
            return ResultUtil.error("缺少分页参数");
        }
        PageInfo<HkDoor> pageInfo = hkDoorService.doorList(pageNum,pageSize,name,position,state);
        return ResultUtil.success(pageInfo);
    }

    /**
     * 海康摄像头监控列表查询
     *
     * @param pageNum
     * @param pageSize
     * @param name
     * @param position
     * @param state
     * @return
     */
    @GetMapping("cameraList")
    public Result cameraList(Integer pageNum, Integer pageSize, String name, String position, String state) {
        if (pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少分页参数");
        }
        PageInfo<HkDoor> pageInfo = hkCameraService.cameraList(pageNum, pageSize, name, position, state);
        return ResultUtil.success(pageInfo);
    }


    /**
     * 监控详情列表
     *
     * @param pageNum
     * @param pageSize
     * @param indexCode
     * @param taskCode
     * @param timeCycle
     * @param user
     * @return
     */
    @GetMapping("cameraDetailsList")
    public Result cameraDetailsList(Integer pageNum, Integer pageSize, String indexCode, String taskCode, String timeCycle, String user) {

        if (pageNum == null || pageSize == null) {
            return ResultUtil.error("缺少分页参数");
        }
        return hkCameraService.cameraDetailsList(pageNum, pageSize, indexCode, taskCode, timeCycle, user);
    }


    /**
     * 查询门禁点出入事件详情记录
     *
     * @param doorDetailReq
     * @return
     */
    @PostMapping("doorDetails")
    public Result doorDetails(@RequestBody DoorDetailReq doorDetailReq) {
        if (doorDetailReq.getPageNo() == null || doorDetailReq.getPageSize() == null) {
            return ResultUtil.error("缺少分页参数");
        }
        Map<String, Object> map = hkDoorService.doorDetails(doorDetailReq);
        return ResultUtil.success(map);
    }

    /**
     * 获取门禁事件的图片
     * @param svrIndexCode
     * @param picUri
     * @return
     */
    @GetMapping("pictures")
    public Result pictures(String svrIndexCode,String picUri){
        if (StringUtils.isEmpty(svrIndexCode) || StringUtils.isEmpty(picUri)){
            return ResultUtil.error("门禁设备上没有打开上传功能");
        }
        return ResultUtil.success(hkDoorService.pictures(svrIndexCode,picUri));
    }

    /**
     * 编辑门禁与实验室id 进行关联
     *
     * @param hkDoorLaboratoryRelEntity
     * @return
     */
    @PostMapping("editDoorLaboratoryRel")
    public Result editDoorLaboratoryRel(@RequestBody HKDoorLaboratoryRelEntity hkDoorLaboratoryRelEntity) {

        return hkDoorService.editDoorLaboratoryRel(hkDoorLaboratoryRelEntity);
    }


    /**
     * 编辑人员与userid 进行关联
     *
     * @param hkPersonUserRelEntity
     * @return
     */
    @PostMapping("editPersonUserRel")
    public Result editPersonUserRel(@RequestBody HKPersonUserRelEntity hkPersonUserRelEntity) {

        return hkDoorService.editPersonUserRel(hkPersonUserRelEntity);
    }

    /**
     * 进行监控与试验室和仪器关系授权
     *
     * @param camera
     * @param testLaboratoryId
     * @param ids
     * @return
     */
    @GetMapping("/impowerCameraaboratoryInstruments")
    public Result impowerCameraLaboratoryInstruments(String camera, Integer testLaboratoryId, Integer ids[]) {

        return hkDoorService.impowerDoorLaboratoryInstruments(camera, testLaboratoryId, ids);
    }

    /**
     * 临时授权
     * @param id
     * @return
     */
    @GetMapping("temporaryVisit")
    public Result temporaryVisit(Integer id){
        if (id == null){
            return ResultUtil.error("缺少参数");
        }
        Boolean aBoolean = hkDoorService.temporaryVisit(id);
        if (aBoolean){
            return ResultUtil.success("临时授权成功",null);
        }else {
            return ResultUtil.error("临时授权失败");
        }
    }

    /**
     *取消临时授权
     * @param id
     * @return
     */
    @GetMapping("cancelVisit")
    public Result cancelVisit(String id){
        if (id == null){
            return ResultUtil.error("缺少参数");
        }
        Boolean aBoolean = hkDoorService.cancelVisit(id);
        if (aBoolean){
            //移除数据
            hkPersonDoorProvisionalAuthorityRelEntityMapper.deleteById(id);
            return ResultUtil.success("取消临时授权成功",null);
        }else {
            return ResultUtil.error("取消临时授权失败");
        }
    }

    /**
     * 测试权限取消
     */
    @GetMapping("testCancelVisit")
    public void testCancelVisit(){
        HkDoorReq hkDoorReq = new HkDoorReq();
        List<PersonDoorReq> personDatas = Lists.newArrayList();
        PersonDoorReq personDoorReq = new PersonDoorReq();
        List<String> personId = Lists.newArrayList();
        personId.add("075f15376c7f4432ac78ce2d57ce0740");
        personId.add("1c9b8024c0da420597e07491979c0a75");
        personId.add("5aa6c61cea064fd4a79c95072449d209");
        personId.add("4fb2c350f8144a9f80e4f21b64ceae1c");
        personDoorReq.setIndexCodes(personId);
        personDatas.add(personDoorReq);
        hkDoorReq.setPersonDatas(personDatas);
        LambdaQueryWrapper<HkDoor> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like(HkDoor::getName,"实验楼5")
                .or()
                .like(HkDoor::getName,"实验楼6");
        List<HkDoor> list = hkDoorService.list(queryWrapper);
        List<ResourceInfo> resourceInfos = Lists.newArrayList();
        for (HkDoor hkDoor :list){
            ResourceInfo resourceInfo = new ResourceInfo();
            resourceInfo.setResourceIndexCode(hkDoor.getIndexCode());
            resourceInfos.add(resourceInfo);
        }

        hkDoorReq.setResourceInfos(resourceInfos);
        Map<String, Object> map = HkUtils.cancleBandDoor(hkConfig.getCancelBandDoor(), hkDoorReq);
        System.out.println("权限取消成功");
    }

    /**
     * 查询门禁点状态
     * @param indexCodes
     * @return
     */
    @GetMapping("doorState")
    public Result doorState(@RequestParam("indexCodes") List<String> indexCodes){
        if (CollectionUtils.isEmpty(indexCodes)){
            return ResultUtil.error("缺少参数");
        }
        Map<String,Object> map = hkDoorService.doorState(indexCodes);
        return ResultUtil.success(map);
    }

    /**
     * 临时访问列表
     *
     * @param personId
     * @return
     */
    @GetMapping("getTemporaryAccessList")
    public Result getTemporaryAccessList(String personId) {

        return hkDoorService.getTemporaryAccessList(personId);
    }

    /**
     * 新增：临时访问
     *
     * @param data
     * @return
     */
    @PostMapping("addtemporaryVisit")
    public Result addtemporaryVisit(@RequestBody HKPersonDoorProvisionalAuthorityRelEntity data) {

        if (data == null) {
            return ResultUtil.error("缺少必填参数");
        }
        if (StringUtils.isEmpty(data.getLaboratoryMessage())) {
            return ResultUtil.error("缺少必填参数");
        }

        try {
            hkDoorService.addtemporaryVisit(data);
        } catch (Exception e) {
            return ResultUtil.error("授权失败");
        }

        return ResultUtil.error("授权失败");
    }

    /**
     * 通过实验室id 获取门禁列表
     *
     * @param testLaboratoryId
     * @return
     */
    @GetMapping("getAccessControlStatusList")
    public Result getAccessControlStatusList(String testLaboratoryId) {

        return hkDoorService.getAccessControlStatusList(testLaboratoryId);
    }

    /**
     * 通过实验室id 获取监控列表
     *
     * @param testLaboratoryId
     * @return
     */
    @GetMapping("getCameraList")
    public Result getCameraList(String testLaboratoryId) {

        return hkCameraService.getCameraList(testLaboratoryId);
    }

    /**
     * 任务单与人员授权
     *
     * @param taskId
     * @return
     */
    @GetMapping("taskListAuthorization")
    public Result taskListAuthorization(String taskId) {

        return hkDoorService.taskListAuthorization(taskId);
    }


}
