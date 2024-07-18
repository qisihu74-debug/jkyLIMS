package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lims.manage.erp.entity.QsAuditScheduleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface QsAuditScheduleMapper extends BaseMapper<QsAuditScheduleEntity> {
    int deleteByPrimaryKey(Integer scheduleId);

    int insert(QsAuditScheduleEntity record);

    int insertSelective(QsAuditScheduleEntity record);

    QsAuditScheduleEntity selectByPrimaryKey(Integer scheduleId);

    int updateByPrimaryKeySelective(QsAuditScheduleEntity record);

    int updateByPrimaryKey(QsAuditScheduleEntity record);
}