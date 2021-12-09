package com.lims.manage.erp.service;

import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.vo.SampleAddParamVo;
import com.lims.manage.erp.vo.SampleDetailVo;
import com.lims.manage.erp.vo.SamplePublicInfoVo;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SampleService {
    /**
     * 新增样品信息
     *
     * @param addParamVo
     * @return
     */
    Integer addSampleData(SampleAddParamVo addParamVo, MultipartFile[] file);

    /**
     * 查询样品公用信息
     *
     * @param paramVo
     * @return
     */
    List<SamplePublicInfoVo> getSamplePublicInfos(SampleEntity paramVo);

    /**
     * 新增委托时样品的查询列表
     *
     * @param sampleEntity
     * @return
     */
    List<SampleEntity> getSampleDataList(SampleEntity sampleEntity);

    /**
     * 查询样品信息--带委托单位名称
     *
     * @param sampleEntity
     * @return
     */
    List<SampleDetailVo> selectSampleList2(SampleEntity sampleEntity);

    /**
     * 查询样品标签信息
     *
     * @param sampleId
     * @return
     */
    Workbook getSampleTagInfo(Integer sampleId);
}
