package com.lims.manage.erp.mapper;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.lims.manage.erp.vo.TestCheckItemTeamRelVo;
import org.apache.ibatis.annotations.Param;
import com.lims.manage.erp.entity.TestCheckItemTeamRel;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 团队检测项目(TestCheckItemTeamRel)表数据库访问层
 *
 * @author makejava
 * @since 2022-03-18 10:01:56
 */
public interface TestCheckItemTeamRelDao extends BaseMapper<TestCheckItemTeamRel> {
    IPage<TestCheckItemTeamRelVo> getPageList(IPage<TestCheckItemTeamRelVo> page, @Param(Constants.WRAPPER) Wrapper<TestCheckItemTeamRel> queryWrapper);

    /**
     * 查询模版数据
     */
    @Select("SELECT\n" +
            "\tt1.check_item_id,\n" +
            "\tt1.team_name,\n" +
            "\tt2.product_id \n" +
            "FROM\n" +
            "\tSheet1 AS t1\n" +
            "\tLEFT JOIN test_product_item AS t2 ON t1.check_item_id = t2.check_item_id" +
            " WHERE team_name is not null")
    List<TestCheckItemTeamRelVo> selectCheckItemTeamRelList();

}

