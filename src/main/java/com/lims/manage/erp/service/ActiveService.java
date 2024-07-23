package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.AduditBaseData;
import com.lims.manage.erp.entity.QsActiveEntity;
import com.lims.manage.erp.entity.QsAuditScheduleRelEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.QsActiveVo;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @Author: DLC
 * @Date: 2024/7/10 16:23
 */
public interface ActiveService extends IService<QsActiveEntity> {

    /**
     * 创建内审活动
     *
     * @param qsActiveEntity
     * @return
     */
    Result addQsActiveData(QsActiveEntity qsActiveEntity);

    /**
     * 更新内审活动
     *
     * @param qsActiveEntity
     * @return
     */
    Result updateQsActiveData(QsActiveEntity qsActiveEntity);


    /**
     * 查询详情内审活动
     *
     * @param activeId
     * @return
     */
    Result queryDetailsQsActiveData(String activeId);

    /**
     * 返回内审基础信息
     *
     * @return
     */
    Result getInternalAuditBasics();

    /**
     * 开始进行内审活动
     *
     * @param qsActiveVo
     * @return
     */
    Result startInternalAuditPlan(QsActiveVo qsActiveVo);

    /**
     * 发起会议：首次会议、末次会议
     *
     * @param qsAuditScheduleRelEntity
     * @param file
     * @return
     */
    Result initiateAMeeting(QsAuditScheduleRelEntity qsAuditScheduleRelEntity, MultipartFile[] file);

    /**
     * 提交内审总结附件
     *
     * @param qsAuditScheduleRelEntity
     * @param file
     * @return
     */
    Result submitInternalAuditDocument(QsAuditScheduleRelEntity qsAuditScheduleRelEntity, MultipartFile[] file);

    List<String> getDiviDeStates(int activeId, int divideId);

    /**
     * 内审检查 根据内审ID 展示 详情
     *
     * @param activeId
     * @param type     = 1 内审检查 根据内审ID 展示 详情 、 type = 2 问题整改详情（展示整改详情，不展示 检查记录）
     * @return
     */
    Result getInternalAuditInspectionDetails(String activeId, Integer type);

    /**
     * 填充数据
     *
     * @param divideId
     * @param object
     * @return
     */
    XWPFDocument downloadEntrust(String divideId, InputStream object, List<AduditBaseData> aduditBaseDataList) throws IOException;

}
