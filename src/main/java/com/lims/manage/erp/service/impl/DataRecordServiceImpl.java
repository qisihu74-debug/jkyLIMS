package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.DataRecord;
import com.lims.manage.erp.mapper.DataRecordDao;
import com.lims.manage.erp.service.DataRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 学习资料动态记录业务层实现类
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
@Service
@Slf4j
public class DataRecordServiceImpl extends ServiceImpl<DataRecordDao, DataRecord> implements DataRecordService {
}
