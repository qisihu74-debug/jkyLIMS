package com.lims.manage.erp.controller;



import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.Patent;
import com.lims.manage.erp.entity.TestMethod;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.PatentService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * (Patent)表控制层
 *
 * @author makejava
 * @since 2022-03-08 10:40:13
 */
@RestController
@RequestMapping("patent")
//@Transactional
public class PatentController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private PatentService patentService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param patent 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(Page<Patent> page, Patent patent) {

        QueryWrapper<Patent> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("del_flag",0);
        if (patent.getPatentname()!=null){
            queryWrapper.like("Patentname",patent.getPatentname());
        }
        queryWrapper.orderByDesc("patenttime");
        return ResultUtil.success(this.patentService.page(page, queryWrapper));
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
            Patent Patent=this.patentService.getOne(new QueryWrapper<Patent>().eq("id",id).eq("start",0));
            return ResultUtil.success(Patent);
        }else {
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
        if (StrUtil.isEmptyIfStr(patent)){
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
        if (StrUtil.isEmptyIfStr(patent)){
            return ResultUtil.error("数据为空");
        }
        return this.patentService.updPatent(patent);
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
            return this.patentService.delPatent(idList);
        }else {
            return ResultUtil.error("数据为空");
        }
    }
}

