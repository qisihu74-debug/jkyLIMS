package com.lims.manage.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.DataAuditRecord;
import com.lims.manage.erp.entity.DataInfo;
import com.lims.manage.erp.vo.DataAuditRecordVo;
import org.apache.ibatis.annotations.Param;

/**
 * 学习资料审核记录dao层接口
 *
 * @author: zhq
 * @date: 2023-01-04
 * @version: v1.0
 */
public interface DataAuditRecordDao extends BaseMapper<DataAuditRecord> {

    /**
     * 知识审核列表
     * @param page 分页参数
     * @param dataInfo 查询条件
     * @return IPage<DataInfoVo>
     */
    IPage<DataAuditRecordVo> pageList(Page page,@Param("dataInfo") DataInfo dataInfo);
}
