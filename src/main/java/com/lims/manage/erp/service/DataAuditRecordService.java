package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.DataAuditRecord;
import com.lims.manage.erp.entity.DataInfo;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.DataAuditRecordVo;
import com.lims.manage.erp.vo.DataInfoVo;

/**
 * 学习资料审核记录业务层接口
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
public interface DataAuditRecordService extends IService<DataAuditRecord> {

    /**
     * 知识审核列表
     * @param page 分页参数
     * @param dataInfo 查询条件
     * @return IPage<DataInfoVo>
     */
    IPage<DataAuditRecordVo> pageList(Page page, DataInfo dataInfo);


}
