package com.lims.manage.erp.mapper;

import com.lims.manage.erp.vo.LabelValueVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface HomeMapper {
    /**
     * 查询今日全部任务数
     *
     * @return
     */
    Integer getAllTask();

    /**
     * 查询今日已完成任务
     *
     * @return
     */
    Integer getCompleteTask();

    /**
     * 查询今日未完成任务
     *
     * @return
     */
    Integer getIncompleteTask();

    /**
     * 查询今日预估产值
     *
     * @return
     */
    Integer getOutputValue();

    /**
     * 产值分析
     * @return
     */
    List<LabelValueVo> outputValueStatistics(String beginDate,String endDate);
}
