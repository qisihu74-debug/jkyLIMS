package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.mapper.StatisticsMapper;
import com.lims.manage.erp.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    @Autowired
    private StatisticsMapper statisticsMapper;
}
