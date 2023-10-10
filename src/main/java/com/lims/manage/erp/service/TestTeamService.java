package com.lims.manage.erp.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lims.manage.erp.entity.TestTeam;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.vo.Node;
import com.lims.manage.erp.vo.TestTeamVo;

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

    /**
     * 获取团队树形结构数据
     * @return
     */
    List<Node> getTree();

    /**
     * 根据团队名称获取团队信息列表
     * @param name 团队名称
     * @return 团队列表
     */
    List<TestTeam> getTreeByName(String name);
}

