package com.lims.manage.erp.controller;



import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.TestCheckItemTeamRel;
import com.lims.manage.erp.entity.TestInstrumentAppraisalRecord;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestCheckItemTeamRelService;
import com.lims.manage.erp.vo.TestCheckItemTeamRelVo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 团队检测项目(TestCheckItemTeamRel)表控制层
 *
 * @author makejava
 * @since 2022-03-18 10:01:55
 */
@RestController
@RequestMapping("testCheckItemTeamRel")
public class TestCheckItemTeamRelController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestCheckItemTeamRelService testCheckItemTeamRelService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param testCheckItemTeamRel 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(Page<TestCheckItemTeamRelVo> page, TestCheckItemTeamRel testCheckItemTeamRel) {
        QueryWrapper<TestCheckItemTeamRel> queryWrapper=new QueryWrapper<>();
        if (testCheckItemTeamRel.getTeamId()!=null){
            queryWrapper.eq("r.team_id",testCheckItemTeamRel.getTeamId());
        }
        if (testCheckItemTeamRel.getCheckItemId()!=null){
            queryWrapper.eq("r.check_item_id",testCheckItemTeamRel.getCheckItemId());
        }
        if (testCheckItemTeamRel.getProductId()!=null){
            queryWrapper.eq("r.product_id",testCheckItemTeamRel.getProductId());
        }
        return ResultUtil.success(this.testCheckItemTeamRelService.getPageList(page, queryWrapper));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public Result selectOne(@PathVariable Serializable id) {
        if (id!=null&&id!=""){
            return ResultUtil.success(this.testCheckItemTeamRelService.getById(id));
        }else {
            return ResultUtil.error("参数为空");
        }
    }

    /**
     * 新增数据
     *
     * @param testCheckItemTeamRel 实体对象
     * @return 新增结果
     */
    @PostMapping("/add")
    public Result insert(@RequestBody TestCheckItemTeamRel testCheckItemTeamRel) {
        if (StrUtil.isEmptyIfStr(testCheckItemTeamRel)){
            return ResultUtil.error("数据为空");
        }
        return this.testCheckItemTeamRelService.addTestCheckItemTeamRel(testCheckItemTeamRel);
    }

    /**
     * 修改数据
     *
     * @param testCheckItemTeamRel 实体对象
     * @return 修改结果
     */
    @PostMapping("/edit")
    public Result update(@RequestBody TestCheckItemTeamRel testCheckItemTeamRel) {
        if (StrUtil.isEmptyIfStr(testCheckItemTeamRel)){
            return ResultUtil.error("数据为空");
        }
        return this.testCheckItemTeamRelService.updTestCheckItemTeamRel(testCheckItemTeamRel);
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @PostMapping("/del")
    public Result delete(@RequestBody List<Long> idList) {
        if (idList.size() != 0) {
            return this.testCheckItemTeamRelService.delTestCheckItemTeamRel(idList);
        } else {
            return ResultUtil.error("数据为空");
        }
    }

    /**
     * 更新检测项所属团队信息
     */
    @GetMapping("/updateTeamDetectionItems")
    @Transactional(rollbackFor = Exception.class)
    public Result updateTeamDetectionItems() {

        Result result = testCheckItemTeamRelService.updateTeamDetectionItems();
        List<TestCheckItemTeamRel> teamRelArrayList = (List<TestCheckItemTeamRel>) result.getData();
        testCheckItemTeamRelService.saveBatch(teamRelArrayList);
        return ResultUtil.success("操作成功");
    }
}

