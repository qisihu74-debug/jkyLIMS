package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.result.Result;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.service
 * @desc
 * @date 2024-09-14 11:15
 * @Copyright © 河南交科院
 */
public interface HkDoorService extends IService<HkDoor> {
    PageInfo<HkDoor> doorList(Integer pageNum, Integer pageSize, String name, String position, String state);

    Map<String, Object> doorDetails(DoorDetailReq doorDetailReq);


    /**
     * 编辑门禁与实验室id 进行关联
     *
     * @param hkDoorLaboratoryRelEntity
     * @return
     */
    Result editDoorLaboratoryRel(HKDoorLaboratoryRelEntity hkDoorLaboratoryRelEntity);

    /**
     * 编辑人员与userid 进行关联
     *
     * @param hkPersonUserRelEntity
     * @return
     */
    Result editPersonUserRel(HKPersonUserRelEntity hkPersonUserRelEntity);

    /**
     * 获取监控与试验室和仪器关系
     *
     * @param hkDoorLaboratoryInstrumentRelEntity
     * @return
     */
    Result getDoorLaboratoryInstruments(HKDoorLaboratoryInstrumentRelEntity hkDoorLaboratoryInstrumentRelEntity);

    /**
     * 进行监控与试验室和仪器关系授权
     *
     * @param indexCode
     * @param testLaboratoryId
     * @param ids
     * @return
     */
    Result impowerDoorLaboratoryInstruments(String indexCode, Integer testLaboratoryId, Integer ids[]);

    /**
     * 进行监控与试验室和仪器关系移除
     *
     * @param ids
     * @return
     */
    Result removeDoorLaboratoryInstruments(Integer ids[]);

    Map<String, Object> pictures(String svrIndexCode, String picUri);

    Boolean temporaryVisit(Integer id);

    Boolean cancelVisit(String id);

    Map<String, Object> doorState(List<String> indexCodes);

    /**
     * 临时访问列表
     *
     * @param personId
     * @return
     */
    Result getTemporaryAccessList(String personId);

    /**
     * 新增：临时访问
     *
     * @param data
     * @return
     */
    Result addtemporaryVisit(HKPersonDoorProvisionalAuthorityRelEntity data);

    /**
     * 通过实验室id 获取门禁列表
     *
     * @param testLaboratoryId
     * @return
     */
    Result getAccessControlStatusList(String testLaboratoryId);
}
