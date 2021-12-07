package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.TaskEntity;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.mapper
 * @desc
 * @date 2021/12/6 17:47
 * @Copyright © 河南交科院
 */
@Component
@Mapper
public interface TaskMapper extends BaseMapper {

    /**
     * 获取最大的任务编号
     * @return
     */
    Integer selectMaxNo();

    /**
     * 更新委托单状态
     * @param entrustmentId
     */
    void updateEntrustById(Long entrustmentId);

    /**
     * 保存任务单
     * @param entity
     */
    void save(TaskEntity entity);
}
