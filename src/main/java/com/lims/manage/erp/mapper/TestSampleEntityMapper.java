package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.EntrustEntity;
import com.lims.manage.erp.entity.PreSampleCode;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.TestSampleEntity;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.SampleJudgeBasisVo;
import com.lims.manage.erp.vo.SampleSimpleListVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface TestSampleEntityMapper extends BaseMapper<TestSampleEntity> {
    int deleteByPrimaryKey(Integer id);

    int insert(TestSampleEntity record);

    int insertSelective(TestSampleEntity record);

    TestSampleEntity selectByPrimaryKey(Integer id);

    List<TestSampleEntity> selectByPid(Integer id);

    int updateByPrimaryKeySelective(TestSampleEntity record);

    int updateByPrimaryKeyWithBLOBs(TestSampleEntity record);

    int updateByPrimaryKey(TestSampleEntity record);

    int updateByPrimaryKeyNotAll(TestSampleEntity record);

    /**
     * 批量新增样品
     *
     * @param entities
     * @return
     */
    int insertBatch(@Param("entities") List<TestSampleEntity> entities);

    /**
     * 批量新增配合比样品
     *
     * @param entities
     * @return
     */
    int insertBatchMixSamples(@Param("entities") List<TestSampleEntity> entities);

    /**
     * 添加委托查询样品列表
     * @param entity
     * @return
     */
    List<SampleSimpleListVo> getSimpleList(TestSampleEntity entity);
    /**
     * 样品查询/打印列表
     * @param entity
     * @return
     */
    List<SampleSimpleListVo> showSimpleList(TestSampleEntity entity);

    /**
     * 配合比可选择导入样品列表
     * @param sampleCode
     * @param companyId
     * @return
     */
    List<SampleSimpleListVo> importSampleList(String sampleCode,Integer companyId);

    /**
     * 新增委托--导入样品列表--带判定依据
     * @param entity
     * @return
     */
    List<SampleJudgeBasisVo> querySampleJudgeBasisList(TestSampleEntity entity);

    /**
     * 查询样品表当前最大ID
     * @return
     */
    Integer getMaxId();

    List<Integer> getAllNodeIds(Integer sampleId);

    /**
     * 获取样品信息集合 id 和 state
     */
    List<TestSampleEntity> selectStateCollection(@Param("state")String state);

    /**
     * 获取委托状态，委托id
     */
    EntrustEntity selectEntrustState(Integer sampleId);

    /**
     * 根据委托单位id  查询委托单位信息
     */
    @Select("SELECT company_name FROM test_company WHERE company_id = #{companyId}")
    String getCompanyName(@Param("companyId") Integer companyId);

    /**
     * 根据样品id 查询委托单下 委托单位信息
     */
    EntrustAddVo getEntrustCompanyName(@Param("sampleId")Integer sampleId);

    /**
     * 根据样品ID查询样品所有信息
     * @param sampleCode
     * @return
     */
    TestSampleEntity getAllById(String sampleCode);

    /**
     * 查询当前年月下的预收样编号
     * @param year
     * @param month
     * @return
     */
    Integer getPreCode(String year,String month);

    /**
     * 插入最新的预收样编号
     * @param preSampleCode
     * @return
     */
    int insertLatestPreCode(PreSampleCode preSampleCode);
}