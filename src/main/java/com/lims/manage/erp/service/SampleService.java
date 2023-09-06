package com.lims.manage.erp.service;

import com.aspose.cells.Cells;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.TestSampleEntity;
import com.lims.manage.erp.vo.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipOutputStream;

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
    List<SamplePublicInfoVo> getSamplePublicInfos(SampleDetailVo paramVo);

    PageInfo getSamplePublicInfos2(SampleDetailVo paramVo);

    /**
     * 新增委托时样品的查询列表
     *
     * @param sampleEntity
     * @return
     */
    List<SampleEntity> getSampleDataList(SampleEntity sampleEntity);

    PageInfo getSampleDataList2(SampleEntity sampleEntity);
    PageInfo getSampleDataListNew(SampleEntity sampleEntity);

    /**
     * 查询样品组基本信息
     *
     * @param insertFlag
     * @return
     */
    SampleDetailVo getSampleGroupInfo(String insertFlag);

    /**
     * 查询样品信息--带委托单位名称
     *
     * @param sampleEntity
     * @return
     */
    List<SampleDetailVo> selectSampleList2(SampleEntity sampleEntity);

    /**
     * 修改样品信息
     *
     * @param record
     * @return
     */
    int updateSampleInfo(SampleEntity record);

    /**
     * 查询样品标签信息
     *
     * @param sampleId
     * @return
     */
    SampleDetailVo getSampleTagInfo(Integer sampleId);

    List<HashMap<String, SampleDetailVo>>getSampleTagInfoList(Integer sampleId);

    /**
     * 根据样品id 获取Excel表格集合 存储到zip里面。
     * @return
     */
    ZipOutputStream packagingWorkbookZip(Integer sampleId, HttpServletResponse response) throws IOException;


    ServletOutputStream downloadNewSampleTab(int type, Integer sampleId, SampleDetailVo sampleTagInfo, HttpServletResponse response);

    void downloadNewSampleTab1(int type, Integer sampleId, SampleDetailVo sampleTagInfo, HttpServletResponse response);

    TestSampleEntity sampleInfo(int type, Integer sampleId);

    Integer updateState(Integer sampleId, Integer state, Date time,Integer saveTime,Integer sampleRetentionPeriod,String sampleProcessMode,
                        String approver,String sampleRetentionArea);

    /**
     * 样品留样列表
     * @param sampleOutPutVo
     * @return
     */
    PageInfo sampleRetentionList(SampleOutPutVo sampleOutPutVo);

    /**
     * 样品留样列表 导出
     */
    InputStream sampleRetentionExport(SampleOutPutVo sampleOutPutVo) throws Exception;

    /**
     * 查询技术负责人列表
     * @return
     */
    List<LabelValueVo> getApprover();

    /**
     * 样品留样栏 更新
     * @param sampleOutPutVo
     * @return
     */
    Boolean sampleRetentionUpdate(SampleOutPutVo sampleOutPutVo);

    /**
     * 样品出入库列表
     * @param sampleOutPutVo
     * @return
     */
    PageInfo sampleOutPutList(SampleOutPutVo sampleOutPutVo);

    /**
     * 样品出入库列表 导出
     */
    InputStream sampleOutPutExport(SampleOutPutVo sampleOutPutVo) throws Exception;

    /**
     * 样样品出入库列表 更新
     * @param sampleOutPutVo
     * @return
     */
    Boolean sampleOutPutUpdate(SampleOutPutVo sampleOutPutVo);

    /**
     * 样品出入库列表-分页
     * @param sampleOutPutVo
     * @return
     */
    PageInfo sampleRetentionPageInfoList(SampleOutPutVo sampleOutPutVo);

    /**
     * 样品留样列表-分页
     * @param sampleOutPutVo
     * @return
     */
    PageInfo sampleReservedSamplePageInfoList(SampleOutPutVo sampleOutPutVo);

    int getIdByCode(String code);

    void updateDayByCode(String code, String value);

    void exportWtTz(Cells cells);
}
