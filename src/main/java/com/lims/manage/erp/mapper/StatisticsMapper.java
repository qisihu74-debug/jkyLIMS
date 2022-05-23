package com.lims.manage.erp.mapper;

import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.vo.AreaStatisticsResultVo;
import com.lims.manage.erp.vo.StatisticsParamVo;
import com.lims.manage.erp.vo.TaskStatsVo;
import com.lims.manage.erp.vo.TeamOutputValueVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface StatisticsMapper {

    /**
     * 任务查询
     */
    List<TaskStatsVo> selectTaskQuery(TaskStatsVo taskStatsVo);

    /**
     * 通过任务单id 和 委托信息
     *  获取样品名 和 所属的检测项价格及检测项状态
     */
    List<SampleEntity> selectSampleEntityList(Long taskId,Long entrustmentId);

    /**
     * 区域产值统计
     * @param paramVo
     * @return
     */
    List<AreaStatisticsResultVo> areaStatistics(StatisticsParamVo paramVo);

    /**
     * 部门产值统计
     * @param paramVo
     * @return
     */
    List<TeamOutputValueVo> teamStatistics(StatisticsParamVo paramVo);
}
