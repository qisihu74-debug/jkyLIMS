package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestTeam;

/**
 * 团队管理(TestTeam)表服务接口
 *
 * @author makejava
 * @since 2022-02-22 14:33:00
 */
public interface TestTeamService extends IService<TestTeam> {

    IPage<TestTeam> selectPage(TestTeam testTeam);

}

