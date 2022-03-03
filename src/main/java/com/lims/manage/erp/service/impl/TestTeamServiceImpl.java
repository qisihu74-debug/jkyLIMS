package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.mapper.TestTeamDao;
import com.lims.manage.erp.entity.TestTeam;
import com.lims.manage.erp.service.TestTeamService;
import org.springframework.stereotype.Service;

/**
 * 团队管理(TestTeam)表服务实现类
 *
 * @author makejava
 * @since 2022-02-23 09:14:46
 */
@Service("testTeamService")
public class TestTeamServiceImpl extends ServiceImpl<TestTeamDao, TestTeam> implements TestTeamService {

    @Override
    public IPage<TestTeam> selectPage(TestTeam testTeam) {

        return null;
    }
}

