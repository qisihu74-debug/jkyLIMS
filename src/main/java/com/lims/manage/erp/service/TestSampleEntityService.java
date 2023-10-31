package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.TestSampleEntity;
import com.lims.manage.erp.entity.TestSampleMixInfoEntity;
import com.lims.manage.erp.vo.SampleDetailAddVo;
import com.lims.manage.erp.vo.SamplesAddVo;
import io.swagger.models.auth.In;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TestSampleEntityService extends IService<TestSampleEntity> {
    /**
     * 批量新增样品
     * @param samples
     * @return
     */
    Integer batchInsertSample(List<SampleDetailAddVo> samples);

    /**
     * 预收样
     * @param samples
     * @return
     */
    Integer batchPreReceivedSamples(List<SampleDetailAddVo> samples);
    /**
     * 批量新增配合比样品
     * @param samples
     * @return
     */
    Integer batchInsertMixSample(SamplesAddVo samples);
    Integer preAddMixSamples(SamplesAddVo samples);

    /**
     * 添加委托查询样品列表
     * @param sampleEntity
     * @return
     */
    PageInfo querySampleList(TestSampleEntity sampleEntity);

    /**
     * 样品查询打印列表
     * @param sampleEntity
     * @return
     */
    PageInfo showSampleList(TestSampleEntity sampleEntity);

    /**
     * 配合比可选择导入样品列表
     * @param sampleCode
     * @param companyId
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo importSampleList(String sampleCode,Integer companyId,Integer pageNum,Integer pageSize);
    /**
     * 上传样品多个文件
     */
    Boolean uploading(Integer id, MultipartFile[] file);
    /**
     * 删除文件id
     * @param id
     * @return
     */
    Boolean removeding(Integer id);

    /**
     * 根据ID查询样品详情
     * @param id
     * @return
     */
    TestSampleEntity sampleDetail(Integer id);

    /**
     * 更新样品信息
     * @param sampleEntity
     * @return
     */
    int updateSample(TestSampleEntity sampleEntity);
    int updateSample1030(TestSampleEntity sampleEntity);

    /**
     * 更新配合比样品信息
     * @param sampleEntity
     * @return
     */
    int updateSampleBatch(TestSampleEntity sampleEntity);

    /**
     * 批量新增样品 后 返回新增的数据
     * @param samples
     * @return
     */
    List<TestSampleEntity>  batchInsertSampleCopy(List<SampleDetailAddVo> samples);

    /**
     * 批量新增配合比样品 后 返回新增的数据
     * @param samples
     * @return
     */
    TestSampleMixInfoEntity batchInsertMixSampleCopy(SamplesAddVo samples, long id);

    /**
     * 通过样品id 与 委托单位id 处理。
     * @param id
     * @param companyId
     * @return
     */
    Boolean judgmentSampleUnit(Integer id,Integer companyId);

}
