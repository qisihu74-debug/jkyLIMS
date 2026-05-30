package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.DataAuditRecord;
import com.lims.manage.erp.entity.DataInfo;
import com.lims.manage.erp.mapper.DataAuditRecordDao;
import com.lims.manage.erp.service.DataAuditRecordService;
import com.lims.manage.erp.vo.DataAuditRecordVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 学习资料审核记录业务层实现类
 *
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
@Service
@Slf4j
public class DataAuditRecordServiceImpl extends ServiceImpl<DataAuditRecordDao, DataAuditRecord> implements DataAuditRecordService {

    @Override
    public IPage<DataAuditRecordVo> pageList(Page page, DataInfo dataInfo) {
        return baseMapper.pageList(page, dataInfo);
    }
}
