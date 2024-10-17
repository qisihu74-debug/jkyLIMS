package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.CameraInfo;
import com.lims.manage.erp.entity.HKCameraLaboratoryInstrumentRelEntity;
import com.lims.manage.erp.entity.HkDoor;
import com.lims.manage.erp.mapper.HkCameraDao;
import com.lims.manage.erp.mapper.InstrumentRecordEntityMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.HKDoorLaboratoryInstrumentRelService;
import com.lims.manage.erp.service.HkCameraService;
import com.lims.manage.erp.service.HkDoorService;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.StringUtils;
import com.lims.manage.erp.vo.InstrumentRecordVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service.impl
 * @desc
 * @date 2024-09-26 11:01
 * @Copyright © 河南交科院
 */
@Service
public class HkCameraServiceImpl extends ServiceImpl<HkCameraDao, CameraInfo> implements HkCameraService {
    @Autowired
    private HkCameraDao hkCameraDao;
    @Autowired
    InstrumentRecordEntityMapper instrumentRecordEntityMapper;
    @Autowired
    private HKDoorLaboratoryInstrumentRelService hkDoorLaboratoryInstrumentRelService;
    @Autowired
    private HkDoorService hkDoorService;

    @Override
    public PageInfo<HkDoor> cameraList(Integer pageNum, Integer pageSize, String name, String position, String state) {
        PageHelper.startPage(pageNum, pageSize);
        List<HkDoor> list = hkCameraDao.cameraList(name, position, state);
        PageInfo<HkDoor> pageInfo = new PageInfo(list);
        return pageInfo;
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
    @Override
    public Result cameraDetailsList(Integer pageNum, Integer pageSize, String indexCode, String taskCode, String timeCycle, String user) {


        if (StringUtils.isEmpty(indexCode)) {
            return ResultUtil.error("监控标识不能为空");
        }

        PageHelper.clearPage();
        // 获取门禁标识
        LambdaQueryWrapper<HKCameraLaboratoryInstrumentRelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HKCameraLaboratoryInstrumentRelEntity::getCamera, indexCode);
        List<HKCameraLaboratoryInstrumentRelEntity> CameraLaboratoryList = hkDoorLaboratoryInstrumentRelService.list(queryWrapper);
        if (CollectionUtil.isEmpty(CameraLaboratoryList)) {
            return ResultUtil.error("监控与实验室 无绑定关系");
        }

        // 获取仪器集合
        Set<Long> ids = new HashSet<>();
        for (HKCameraLaboratoryInstrumentRelEntity data : CameraLaboratoryList) {
            String[] instrumentIds = data.getTestInstrumentId().split("\\,");
            for (int i = 0; i < instrumentIds.length; i++) {
                ids.add(Long.valueOf(instrumentIds[i]));
            }
        }

        Date startTime = null;
        Date endTime = null;
        if (StringUtils.isNotEmpty(timeCycle)) {
            try {
                String[] strings = timeCycle.split("~");
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                startTime = simpleDateFormat.parse(strings[0]);
                endTime = simpleDateFormat.parse(strings[1]);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        PageHelper.clearPage();
        PageHelper.startPage(pageNum, pageSize);
        // 进行查询仪器使用记录
        List<InstrumentRecordVo> list = instrumentRecordEntityMapper.selectInstrumentVos(ids, taskCode, user, startTime, endTime);
        PageInfo<InstrumentRecordVo> pageInfo = new PageInfo(list);
        if (CollectionUtil.isNotEmpty(pageInfo.getList())) {
            for (InstrumentRecordVo data : pageInfo.getList()) {
                // 开始时间与结束时间拼接
                if (data.getStartTime() != null && data.getEndTime() != null) {

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String startTimeS = sdf.format(data.getStartTime());
                    String endTimeS = sdf.format(data.getEndTime());
                    data.setTimeCycle(startTimeS + "~" + endTimeS);

                    // 转成 时区
                    String startTimeUT = DateUtil.getISO8601TimestampFromDateStr(startTimeS);
                    String endTimeUT = DateUtil.getISO8601TimestampFromDateStr(endTimeS);
                    data.setTimeCycleUT(startTimeUT + "~" + endTimeUT);

                }
            }
        }

        return ResultUtil.success(pageInfo);
    }

    /**
     * 获取实验室下 监控信息
     *
     * @param testLaboratoryId
     * @return
     */
    @Override
    public Result getCameraList(String testLaboratoryId) {

        // 获取门禁标识
        LambdaQueryWrapper<HKCameraLaboratoryInstrumentRelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HKCameraLaboratoryInstrumentRelEntity::getTestLaboratoryId, testLaboratoryId);
        List<HKCameraLaboratoryInstrumentRelEntity> cameraLaboratoryList = hkDoorLaboratoryInstrumentRelService.list(queryWrapper);
        if (CollectionUtil.isEmpty(cameraLaboratoryList)) {
            return ResultUtil.success(null);
        }

        // 获取监控列表
        List<String> cameras = cameraLaboratoryList.stream().map(HKCameraLaboratoryInstrumentRelEntity::getCamera).collect(Collectors.toList());
        LambdaQueryWrapper<CameraInfo> cameraInfoWrapper = new LambdaQueryWrapper<>();
        cameraInfoWrapper.select(CameraInfo::getName);
        cameraInfoWrapper.select(CameraInfo::getIndexCode);
        cameraInfoWrapper.in(CameraInfo::getIndexCode, cameras);
        List<CameraInfo> list = this.baseMapper.selectList(cameraInfoWrapper);

        // 通过门禁集合 获取对应状态
        Map<String, Object> objectMap = hkDoorService.doorState(cameras);
        Map<String, Object> authDoorMap = (Map<String, Object>) objectMap.get("data");
        List<Map<String, Object>> hkDoorList = (List<Map<String, Object>>) authDoorMap.get("authDoorList");

        // 处理业务数据
        if (CollectionUtil.isNotEmpty(hkDoorList)) {
            for (Map<String, Object> map : hkDoorList) {
                String doorIndexCode = map.get("doorIndexCode").toString();
                Integer doorState = Integer.parseInt(map.get("doorState").toString());
                for (CameraInfo cameraInfo : list) {
                    if (doorIndexCode.equals(cameraInfo.getIndexCode())) {
                        cameraInfo.setDoorState(doorState);
                    }
                }
            }
        }

        return ResultUtil.success(list);

    }
}
