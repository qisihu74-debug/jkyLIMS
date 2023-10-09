package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.SampleItemEntity;
import com.lims.manage.erp.entity.TestTaskPool;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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

}
