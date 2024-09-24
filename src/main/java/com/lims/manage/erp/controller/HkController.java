package com.lims.manage.erp.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.config.HkConfig;
import com.lims.manage.erp.entity.DoorDetailReq;
import com.lims.manage.erp.entity.HkDoor;
import com.lims.manage.erp.entity.HkDoorReq;
import com.lims.manage.erp.entity.HkGrantDoorReq;
import com.lims.manage.erp.entity.HkPerson;
import com.lims.manage.erp.entity.PersonDoorReq;
import com.lims.manage.erp.entity.ResourceInfo;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.HkDoorService;
import com.lims.manage.erp.service.HkPersonService;
import com.lims.manage.erp.util.HkUtils;
import com.lims.manage.erp.util.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        Map<String, Object> map = HkUtils.personList(hkConfig.getDoorSearch());
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
     * 海康人员门禁绑定发送
     * @param hkDoorReq
     * @return
     */
    @PostMapping("personBandDoor")
    public Result personBandDoor(@RequestBody HkDoorReq hkDoorReq){
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
    public Result personGrant(@RequestBody HkGrantDoorReq hkGrantDoorReq){
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
     * 查询门禁点出入事件详情记录
     * @param doorDetailReq
     * @return
     */
    @PostMapping("doorDetails")
    public Result doorDetails(@RequestBody DoorDetailReq doorDetailReq){
        if (doorDetailReq.getPageNo() == null || doorDetailReq.getPageSize() == null){
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
            return ResultUtil.error("未抓拍图片");
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
     * 获取监控与试验室和仪器关系
     *
     * @param hkDoorLaboratoryInstrumentRelEntity
     * @return
     */
    @GetMapping("getDoorLaboratoryInstruments")
    public Result getDoorLaboratoryInstruments(HKDoorLaboratoryInstrumentRelEntity hkDoorLaboratoryInstrumentRelEntity) {

        return hkDoorService.getDoorLaboratoryInstruments(hkDoorLaboratoryInstrumentRelEntity);
    }


    /**
     * 进行监控与试验室和仪器关系授权
     *
     * @param indexCode
     * @param testLaboratoryId
     * @param ids
     * @return
     */
    @GetMapping("/impowerDoorLaboratoryInstruments")
    public Result impowerDoorLaboratoryInstruments(String indexCode, Integer testLaboratoryId, Integer ids[]) {

        return hkDoorService.impowerDoorLaboratoryInstruments(indexCode, testLaboratoryId, ids);
    }


    /**
     * 进行监控与试验室和仪器关系移除
     *
     * @param ids
     * @return
     */
    @GetMapping("/removeDoorLaboratoryInstruments")
    public Result removeDoorLaboratoryInstruments(Integer ids[]) {

        return hkDoorService.removeDoorLaboratoryInstruments(ids);
    }

    /**
     * 临时授权
     * @param personId
     * @return
     */
    @GetMapping("temporaryVisit")
    public Result temporaryVisit(String personId){
        
        return null;
    }

    /**
     * 取消临时授权
     *
     * @param personId
     * @return
     */
    @GetMapping("cancelVisit")
    public Result cancelVisit(String personId) {

        return null;
    }

    /**
     * 临时访问列表
     *
     * @param personId
     * @return
     */
    @GetMapping("getTemporaryAccessList")
    public Result getTemporaryAccessList(String personId) {

        return null;
    }

    /**
     * 新增：临时访问
     *
     * @param data
     * @return
     */
    @PostMapping("addtemporaryVisit")
    public Result addtemporaryVisit(@RequestBody HKPersonDoorProvisionalAuthorityRelEntity data) {

        return null;
    }


}
