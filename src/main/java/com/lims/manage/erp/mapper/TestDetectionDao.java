package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.SampleItemInstrumentEntity;
import com.lims.manage.erp.entity.TestChItemInstrumentMiddleEntity;
import com.lims.manage.erp.entity.TestInstrumentEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: DLC
 * @Date: 2021/12/15 15:30
 * 开始试验检测
 */
@Component
@Mapper
public interface TestDetectionDao {

    /**
     * 依据检测项 选择设备仪器
     * @param checkItemId
     * @return
     */
    List<TestInstrumentEntity> selectTheInstrument(Integer checkItemId);
    /**
     * 针对（/task/passorno） 进行 修改检测项 内容
     * start_time =null,origin_url=null,file_url_str=null
     */
    int updateTaskPassorno(SampleItemInstrumentEntity sampleItemInstrumentEntity);
    /**
     * 进行 修改检测项
     */
    int updateSampleItemInstrumentEntity(SampleItemInstrumentEntity sampleItemInstrumentEntity);
    /**
     * 新增 检测项id 多个仪器id
     */
    int addItemInstrumentMiddleRel(TestChItemInstrumentMiddleEntity testChItemInstrumentMiddleEntity);

    /**
     * 根据检测项主键删除 所属设备仪器
     * @param itemId
     * @return
     */
    int deleteInstrument(Integer itemId);
    /**
     * 更新仪器 结束时间
     */
    int updateItemInstrumentMiddleRel(TestChItemInstrumentMiddleEntity testChItemInstrumentMiddleEntity);

    /**
     * 根据必要条件查询 任务单信息
     * @param entrustId
     * @param sampleId
     * @param checkItemId
     * @return
     */
    SampleItemInstrumentEntity getTestEntrustedSampleCheckitemRelDetailIf(Long entrustId, Integer sampleId, Integer checkItemId);
    /**
     * 根据检测项 主键  获取（test_entrusted_sample_checkitem_rel）详情信息
     *
     */
    SampleItemInstrumentEntity getTestEntrustedSampleCheckitemRelDetail(Integer id);
    /**
     * 通过委托单id 和部门ID为条件  遍历（判断每个状态 state = 3）
     */
    @Select("SELECT\n" +
            "\tstate \n" +
            "FROM\n" +
            "\ttest_entrusted_sample_checkitem_rel \n" +
            "WHERE\n" +
            "\tentrust_id = #{entrustId} \n" +
            "\tAND dept_id = #{deptId}")
    List<Integer> getSampleCheckitemRelDetailState(Long entrustId,Integer deptId);
    /**
     * 实验完成-依据检测项主键 展示 所属仪器列表
     */
    List<TestInstrumentEntity> getInstrumentTestItem(Integer checkItemId);
    /**
     * 根据检测项主键 获取仪器信息
     */
    List<TestChItemInstrumentMiddleEntity> getInstrumentCollection(Integer sidItem);




}
