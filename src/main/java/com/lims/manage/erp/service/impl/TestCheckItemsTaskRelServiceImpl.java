package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.TestCheckItemsTaskRel;
import com.lims.manage.erp.entity.TestTeam;
import com.lims.manage.erp.mapper.TestCheckItemsTaskRelMapper;
import com.lims.manage.erp.mapper.TestTeamDao;
import com.lims.manage.erp.service.TestCheckItemsTaskRelService;
import com.lims.manage.erp.vo.WorkHourStatisticVo;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author dlc
 * @since 2023-10-08
 */
@Service
public class TestCheckItemsTaskRelServiceImpl extends ServiceImpl<TestCheckItemsTaskRelMapper, TestCheckItemsTaskRel> implements TestCheckItemsTaskRelService {

    @Resource
    private TestTeamDao testTeamDao;

    @Override
    public IPage<WorkHourStatisticVo> getWorkHoursList(Page<WorkHourStatisticVo> page, Map<String, Object> paramMap) {
        //根据传入的部门id获取部门及下级的所有部门id
        paramMap.put("deptIds",getTeamIdList(MapUtils.getInteger(paramMap, "deptId")));
        return baseMapper.getWorkHoursList(page, paramMap);
    }


    public List<Integer> getTeamIdList(Integer id) {
        List<Integer> ids = new ArrayList<>();
        ids.add(id);
        LambdaQueryWrapper<TestTeam> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(TestTeam::getPid, id);
        List<TestTeam> teamList = testTeamDao.selectList(queryWrapper);
        if (teamList != null && teamList.size() > 0) {
            for (TestTeam team : teamList) {
                ids.addAll(getTeamIdList(team.getId()));
            }
        }
        return ids;
    }
}
