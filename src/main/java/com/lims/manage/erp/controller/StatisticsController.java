package com.lims.manage.erp.controller;

import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/statistics/")
public class StatisticsController {
    @Autowired
    private LogManagerService logManagerService;
    @Autowired
    private StatisticsService statisticsService;
}
