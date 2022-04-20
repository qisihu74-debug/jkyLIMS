package com.lims.manage.erp.service;

import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.vo.SampleAddParamVo;
import com.lims.manage.erp.vo.SampleDetailVo;
import com.lims.manage.erp.vo.SamplePublicInfoVo;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
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
     * 通过参数 返回处理好的Excel 数据
     * @param fileStream 模板
     * @param result map参数
     * @param fileName 文件名
     * @return
     */
    Workbook returningData(InputStream fileStream, HashMap<String, SampleDetailVo> result, String fileName);
    /**
     * 根据样品id 获取Excel表格集合 存储到zip里面。
     * @return
     */
    ZipOutputStream packagingWorkbookZip(Integer sampleId, HttpServletResponse response) throws IOException;


    /**
     * 获取数据集合 打包成 zip格式。
     * @return
     */
    Boolean packagingZip(List<Workbook> dateSet);


}
