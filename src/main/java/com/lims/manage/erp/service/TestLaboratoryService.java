package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestLaboratory;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.TestLaboratoryVo;

import javax.management.Query;
import java.util.List;

/**
 * 实验室管理(TestLaboratory)表服务接口
 *
 * @author makejava
 * @since 2022-02-25 10:08:36
 */
public interface TestLaboratoryService extends IService<TestLaboratory> {
    Result addLaboratory(TestLaboratory testLaboratory);
    Result updLaboratory(TestLaboratory testLaboratory);
    Result delLaboratory(List<Long> idList);

    Result getPageList(TestLaboratoryVo testLaboratory);
}

