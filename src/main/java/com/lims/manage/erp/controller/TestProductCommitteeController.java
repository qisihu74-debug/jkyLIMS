package com.lims.manage.erp.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.annotation.Log;
import com.lims.manage.erp.entity.TestProductCommitteeEntity;
import com.lims.manage.erp.enums.BusinessType;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestProductCommitteeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 产品委员会
 */
@RestController
@RequestMapping("testProductCommittee")
public class TestProductCommitteeController extends ApiController {

    @Resource
    private TestProductCommitteeService testProductCommitteeService;


    /**
     * 分页查询所有数据
     *
     * @param productCommittee 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(TestProductCommitteeEntity productCommittee,
                            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<TestProductCommitteeEntity> page = new Page<>(pageNo, pageSize);
        QueryWrapper<TestProductCommitteeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);
        if (productCommittee.getCouncilName() != null) {
            queryWrapper.like("council_name", productCommittee.getCouncilName());
        }
        queryWrapper.orderByDesc("create_time");
        return ResultUtil.success(this.testProductCommitteeService.page(page, queryWrapper));
    }

    /**
     * 新增数据
     *
     * @param productCommittee 实体
     * @return 新增结果
     */
    @Log(title = "新增产品委员会", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public Result insert(@RequestBody TestProductCommitteeEntity productCommittee) {
        if (StrUtil.isEmptyIfStr(productCommittee)) {
            return ResultUtil.error("数据为空");
        }
        return this.testProductCommitteeService.addProductCommittee(productCommittee);
    }


    /**
     * 删除数据
     *
     * @param councilId 主键ID
     * @return 删除结果
     */
    @Log(title = "删除产品委员会", businessType = BusinessType.DELETE)
    @PostMapping("/del")
    public Result delete(String councilId) {
        if (StringUtils.isNotBlank(councilId)) {
            return this.testProductCommitteeService.delProductCommittee(councilId);
        } else {
            return ResultUtil.error("数据为空");
        }
    }


}
