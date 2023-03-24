package com.lims.manage.erp.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.Patent;
import com.lims.manage.erp.entity.PatentAuthorization;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.PatentAuthorizationService;
import com.lims.manage.erp.service.PatentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (Patent)表控制层
 *
 * @author makejava
 * @重构时间 2023/03/22
 * @since 2022-03-08 10:40:13
 */
@RestController
@RequestMapping("patent")
public class PatentController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private PatentService patentService;
    @Resource
    private PatentAuthorizationService authorizationService;


    /**
     * 分页查询所有数据
     *
     * @param patent 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(Patent patent,
                            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<Patent> page = new Page<>(pageNo, pageSize);
        QueryWrapper<Patent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);
        if (patent.getPatentName() != null) {
            queryWrapper.like("patent_name", patent.getPatentName());
        }
        if (patent.getPatentId() != null) {
            queryWrapper.like("patent_id", patent.getPatentId());
        }
        if (patent.getProduct() != null) {
            queryWrapper.like("product", patent.getProduct());
        }
        if (patent.getMaturityTime() != null) {
            queryWrapper.like("maturity_time", patent.getMaturityTime());
        }
        if (patent.getPatentType() != null) {
            queryWrapper.like("patent_type", patent.getPatentType());
        }

        queryWrapper.orderByDesc("create_time");
        return ResultUtil.success(this.patentService.page(page, queryWrapper));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param patentId 专利号
     * @return 单条数据
     */
    @GetMapping("getById")
    public Result selectOne(String patentId) {
        if (patentId != null && patentId != "") {
            Patent Patent = this.patentService.getOne(new QueryWrapper<Patent>()
                    .eq("patent_id", patentId).eq("del_flag", 0));
            Patent.setPatentAuthorizations(this.authorizationService.list(new QueryWrapper<PatentAuthorization>()
                    .eq("patent_id", patentId)));
            return ResultUtil.success(Patent);
        } else {
            return ResultUtil.error("参数为空");
        }

    }

    /**
     * 新增数据
     *
     * @param patent 实体对象
     * @return 新增结果
     */
    @PostMapping("/add")
    public Result insert(@RequestBody Patent patent) {
        if (StrUtil.isEmptyIfStr(patent)) {
            return ResultUtil.error("数据为空");
        }
        return this.patentService.addPatent(patent);
    }

    /**
     * 修改数据
     *
     * @param patent 实体对象
     * @return 修改结果
     */
    @PostMapping("/edit")
    public Result update(@RequestBody Patent patent) {
        if (StrUtil.isEmptyIfStr(patent)) {
            return ResultUtil.error("数据为空");
        }
        return this.patentService.updPatent(patent);
    }

    /**
     * 删除数据
     *
     * @param id 主键结合
     * @return 删除结果
     */
    @PostMapping("/del")
    public Result delete(String id) {
        if (StringUtils.isNotBlank(id)) {
            return this.patentService.delPatent(id);
        } else {
            return ResultUtil.error("数据为空");
        }
    }
}

