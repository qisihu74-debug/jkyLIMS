package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.TestOriginalRecordTemplate;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.TorttpiVo;

import java.util.List;

/**
 * 原始记录模板(TestOriginalRecordTemplate)表服务接口
 *
 * @author makejava
 * @since 2022-03-16 14:12:44
 */
public interface TestOriginalRecordTemplateService extends IService<TestOriginalRecordTemplate> {

    Result addtestOriginalRecordTemplate(TestOriginalRecordTemplate testOriginalRecordTemplate);
    Result updtestOriginalRecordTemplate(TestOriginalRecordTemplate testOriginalRecordTemplate);
    Result delTtestOriginalRecordTemplate(List<Long> idList);
    IPage<TorttpiVo> getPageList(Page<TorttpiVo> page, QueryWrapper<TestOriginalRecordTemplate> queryWrapper);

    /**
     * 查询线下原始记录信息
     * @return
     */
    Result getAllList();

    Result changeTestOriginalRecordTemplate(TestOriginalRecordTemplate testOriginalRecordTemplate);

    PageInfo getRecords(Integer pid, Integer pageNum, Integer pageSize);

}

