package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestCheckItemTeamRel;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.JsonVo;
import com.lims.manage.erp.vo.TestCheckItemTeamRelVo;

import java.util.List;

/**
 * 团队检测项目(TestCheckItemTeamRel)表服务接口
 *
 * @author makejava
 * @since 2022-03-18 10:01:57
 */
public interface TestCheckItemTeamRelService extends IService<TestCheckItemTeamRel> {
    IPage<TestCheckItemTeamRelVo> getPageList(Page<TestCheckItemTeamRelVo> page, QueryWrapper<TestCheckItemTeamRel> queryWrapper);

    Result addTestCheckItemTeamRel(TestCheckItemTeamRel testCheckItemTeamRel);

    Result updTestCheckItemTeamRel(TestCheckItemTeamRel testCheckItemTeamRel);

    Result delTestCheckItemTeamRel(List<Long> idList);

    /**
     * 更新检测项所属团队信息
     *
     * @return
     */
    Result updateTeamDetectionItems();

    /**
     * 批量更新优先级
     *
     * @param jsonVo
     * @return
     */
    Result batchUpdatePriority(JsonVo jsonVo);
}

