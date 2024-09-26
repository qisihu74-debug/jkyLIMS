package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.config.HkConfig;
import com.lims.manage.erp.entity.DoorDetailReq;
import com.lims.manage.erp.entity.DoorStateReq;
import com.lims.manage.erp.entity.HKCameraLaboratoryInstrumentRelEntity;
import com.lims.manage.erp.entity.HKDoorLaboratoryRelEntity;
import com.lims.manage.erp.entity.HKPersonDoorProvisionalAuthorityRelEntity;
import com.lims.manage.erp.entity.HKPersonUserRelEntity;
import com.lims.manage.erp.entity.HkDoor;
import com.lims.manage.erp.entity.HkDoorReq;
import com.lims.manage.erp.entity.HkGrantDoorReq;
import com.lims.manage.erp.entity.PersonDoorReq;
import com.lims.manage.erp.entity.ResourceInfo;
import com.lims.manage.erp.mapper.HKDoorLaboratoryInstrumentRelEntityMapper;
import com.lims.manage.erp.mapper.HKDoorLaboratoryRelEntityMapper;
import com.lims.manage.erp.mapper.HKPersonDoorProvisionalAuthorityRelEntityMapper;
import com.lims.manage.erp.mapper.HKPersonUserRelEntityMapper;
import com.lims.manage.erp.mapper.HkDoorDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.HKDoorLaboratoryInstrumentRelService;
import com.lims.manage.erp.service.HkDoorService;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.HkUtils;
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

    @Override
    public PageInfo<HkDoor> doorList(Integer pageNum, Integer pageSize, String name, String position, String state) {
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
        // 先删除 再新增关系
        LambdaQueryWrapper<HKPersonUserRelEntity> deteWapper = new LambdaQueryWrapper<>();
        deteWapper.eq(HKPersonUserRelEntity::getPersonId, hkPersonUserRelEntity.getPersonId());
        hkPersonUserRelEntityMapper.delete(deteWapper);

        // 新增
        hkPersonUserRelEntityMapper.insert(hkPersonUserRelEntity);

        return ResultUtil.success("操作成功");
    }

    @Override
    public Result getDoorLaboratoryInstruments(HKCameraLaboratoryInstrumentRelEntity hkCameraLaboratoryInstrumentRelEntity) {

        LambdaQueryWrapper<HKCameraLaboratoryInstrumentRelEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(hkCameraLaboratoryInstrumentRelEntity.getCamera())) {
            queryWrapper.eq(HKCameraLaboratoryInstrumentRelEntity::getCamera, hkCameraLaboratoryInstrumentRelEntity.getCamera());
        }
        if (hkCameraLaboratoryInstrumentRelEntity.getTestLaboratoryId() != null) {
            queryWrapper.eq(HKCameraLaboratoryInstrumentRelEntity::getTestLaboratoryId, hkCameraLaboratoryInstrumentRelEntity.getTestLaboratoryId());
        }
        if (hkCameraLaboratoryInstrumentRelEntity.getTestInstrumentId() != null) {
            queryWrapper.eq(HKCameraLaboratoryInstrumentRelEntity::getTestInstrumentId, hkCameraLaboratoryInstrumentRelEntity.getTestInstrumentId());
        }
        List<HKCameraLaboratoryInstrumentRelEntity> list = hkDoorLaboratoryInstrumentRelEntityMapper.selectList(queryWrapper);

        return ResultUtil.success(list);
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
        // 效验重复项
        LambdaQueryWrapper<HKCameraLaboratoryInstrumentRelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HKCameraLaboratoryInstrumentRelEntity::getCamera, indexCode);
        queryWrapper.eq(HKCameraLaboratoryInstrumentRelEntity::getTestLaboratoryId, testLaboratoryId);
        queryWrapper.in(HKCameraLaboratoryInstrumentRelEntity::getTestInstrumentId, ids);
        List<HKCameraLaboratoryInstrumentRelEntity> list = hkDoorLaboratoryInstrumentRelEntityMapper.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(list)) {
            return ResultUtil.error("操作失败，有重复项，请重新选择");
        }
        List<HKCameraLaboratoryInstrumentRelEntity> dataSet = new ArrayList<>();
        for (int i = 0; i < ids.length; i++) {
            HKCameraLaboratoryInstrumentRelEntity data = new HKCameraLaboratoryInstrumentRelEntity();
            data.setCamera(indexCode);
            data.setTestLaboratoryId(testLaboratoryId);
            data.setTestInstrumentId(ids[i]);
            dataSet.add(data);
        }
        hkDoorLaboratoryInstrumentRelService.saveBatch(dataSet);
        return ResultUtil.success("操作成功");
    }

    /**
     * 进行监控与试验室和仪器关系移除
     *
     * @param ids
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result removeDoorLaboratoryInstruments(Integer[] ids) {
        hkDoorLaboratoryInstrumentRelEntityMapper.deleteBatchIds(Arrays.asList(ids));
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

        // 进行转换： 年月日 时分秒 转成 UTC：东八区存放
        if (StringUtils.isNotEmpty(data.getStartTime())) {
            data.setStartTime(DateUtil.getISO8601TimestampFromDateStr(data.getStartTime()));
        }
        if (StringUtils.isNotEmpty(data.getEndTime())) {
            data.setEndTime(DateUtil.getISO8601TimestampFromDateStr(data.getEndTime()));
        }
        hkPersonDoorProvisionalAuthorityRelEntityMapper.insert(data);
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


}
