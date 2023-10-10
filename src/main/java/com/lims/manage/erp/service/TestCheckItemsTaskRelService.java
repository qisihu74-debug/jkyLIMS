package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestCheckItemsTaskRel;
import com.lims.manage.erp.vo.WorkHourStatisticVo;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dlc
 * @since 2023-10-08
 */
public interface TestCheckItemsTaskRelService extends IService<TestCheckItemsTaskRel> {

    /**
     * 根据条件获取工时统计信息
     * @param paramMap 查询条件
     * @return 工时统计信息列表
     */
    IPage<WorkHourStatisticVo> getWorkHoursList(Page<WorkHourStatisticVo> page, Map<String, Object> paramMap);

}
