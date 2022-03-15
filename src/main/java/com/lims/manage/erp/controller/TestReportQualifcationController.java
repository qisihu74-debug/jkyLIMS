package com.lims.manage.erp.controller;



import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.TestProduct;
import com.lims.manage.erp.entity.TestReportQualifcation;
import com.lims.manage.erp.entity.TestStandardFile;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestProductService;
import com.lims.manage.erp.service.TestReportQualifcationService;
import com.lims.manage.erp.vo.TestReportQualifcationVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * (TestReportQualifcation)表控制层
 *
 * @author makejava
 * @since 2022-03-14 14:33:33
 */
@RestController
@RequestMapping("testReportQualifcation")
public class TestReportQualifcationController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestReportQualifcationService testReportQualifcationService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param testReportQualifcation 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(Page<TestReportQualifcationVo> page, TestReportQualifcation testReportQualifcation) {
        QueryWrapper<TestReportQualifcation> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("q.del_flag",0);
        if (testReportQualifcation.getConditionValue()!=null){
            queryWrapper.like("q.specs_content",testReportQualifcation.getSpecsContent());
        }
        queryWrapper.orderByDesc("q.create_time");
        return ResultUtil.success(this.testReportQualifcationService.getPageList(page, queryWrapper));


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
            TestReportQualifcation testStandardFile=this.testReportQualifcationService.getOne(new QueryWrapper<TestReportQualifcation>().eq("id",id).eq("del_flag",0));
            return ResultUtil.success(testStandardFile);
        }else {
            return ResultUtil.error("参数为空");
        }
    }

    /**
     * 新增数据
     *
     * @param testReportQualifcation 实体对象
     * @return 新增结果
     */
    @PostMapping("/add")
    public Result insert(@RequestBody TestReportQualifcation testReportQualifcation) {
        if (StrUtil.isEmptyIfStr(testReportQualifcation)){
            return ResultUtil.error("数据为空");
        }
        return this.testReportQualifcationService.addtestReportQualifcation(testReportQualifcation);
    }

    /**
     * 修改数据
     *
     * @param testReportQualifcation 实体对象
     * @return 修改结果
     */
    @PostMapping("/edit")
    public Result update(@RequestBody TestReportQualifcation testReportQualifcation) {
        if (StrUtil.isEmptyIfStr(testReportQualifcation)){
            return ResultUtil.error("数据为空");
        }
        return this.testReportQualifcationService.updtestReportQualifcation(testReportQualifcation);
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @PostMapping("/del")
    public Result delete(@RequestBody List<Long> idList) {
        if (idList.size()!=0){
            return this.testReportQualifcationService.deltestReportQualifcation(idList);
        }else {
            return ResultUtil.error("数据为空");
        }
    }
}

