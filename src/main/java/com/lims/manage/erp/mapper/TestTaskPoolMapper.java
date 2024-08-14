package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.SampleItemEntity;
import com.lims.manage.erp.entity.TestTaskPool;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 任务单 Mapper 接口
 * </p>
 *
 * @author dlc
 * @since 2023-10-08
 */
@Component
@Mapper
public interface TestTaskPoolMapper extends BaseMapper<TestTaskPool> {

    /**
     * 根据委托单id 查询检测项信息
     * @param entrustId
     * @return
     */
    List<SampleItemEntity> selectItems(@Param("entrustId") Long entrustId);

    /**
     * 通过检测项主键 获取委托单id
     * @param itemId
     * @return
     */
    @Select("\n" +
            "SELECT\n" +
            "\tt1.id \n" +
            "FROM\n" +
            "\ttest_entrusted_info AS t1\n" +
            "\tLEFT JOIN test_entrusted_sample_details_rel AS t2 ON t1.id = t2.entrustment_id\n" +
            "\tLEFT JOIN test_entrusted_sample_checkitem_rel AS t3 ON t3.sample_id = t2.sample_id \n" +
            "WHERE\n" +
            "\tt3.id = #{itemId}\n" +
            "\tlimit 1")
    Long selectEntrustmentId(@Param("itemId") Integer itemId);

    @Select("SELECT\n" +
            "\tcheck_item_name \n" +
            "FROM\n" +
            "\ttest_entrusted_sample_checkitem_rel \n" +
            "WHERE\n" +
            "\tentrust_id = #{entrustId} \n" +
            "\tAND task_id IS NULL")
    List<String> selectCheckitemRelAboutTaskList(@Param("entrustId") Long entrustId);

}
