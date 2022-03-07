package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestTeam;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.TestTeamVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 团队管理(TestTeamVo)表服务接口
 *
 * @author makejava
 * @since 2022-02-22 14:33:00
 */
public interface TestTeamService extends IService<TestTeam> {
    Result addTestTeam(TestTeam testTeam);
    Result updTestTeam(TestTeam testTeam);
    Result delTestTeam(List<Long> idList);
    IPage<TestTeamVo> getListPage(IPage<TestTeamVo> page,Wrapper<TestTeam> queryWrapper);
}

