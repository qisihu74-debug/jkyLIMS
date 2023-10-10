package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.TestCheckItemsTaskRel;
import com.lims.manage.erp.vo.WorkHourStatisticVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author dlc
 * @since 2023-10-08
 */
@Component
@Mapper
public interface TestCheckItemsTaskRelMapper extends BaseMapper<TestCheckItemsTaskRel> {


    /**
     * 根据条件获取工时统计信息
     * @param paramMap 查询条件
     * @return 工时统计信息列表
     */
    IPage<WorkHourStatisticVo> getWorkHoursList(Page<WorkHourStatisticVo> page, @Param("map") Map<String,Object> paramMap);
}
