package com.lims.manage.erp.controller;



import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.entity.Partenr;
import com.lims.manage.erp.entity.Patent;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.PartenrService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 合作伙伴（用户）(Partenr)表控制层
 *
 * @author makejava
 * @since 2022-03-09 16:01:29
 */
@RestController
@RequestMapping("partenr")
public class PartenrController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private PartenrService partenrService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param partenr 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(Page<Partenr> page, Partenr partenr) {

        QueryWrapper<Partenr> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("del_flag",0);
        if (partenr.getPartnername()!=null){
            queryWrapper.like("partnername",partenr.getPartnername());
        }
        queryWrapper.orderByDesc("create_time");
        return ResultUtil.success(this.partenrService.page(page, queryWrapper));

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
            Partenr partenr=this.partenrService.getOne(new QueryWrapper<Partenr>().eq("id",id).eq("status",0));
            return ResultUtil.success(partenr);
        }else {
            return ResultUtil.error("参数为空");
        }

    }

    /**
     * 新增数据
     *
     * @param partenr 实体对象
     * @return 新增结果
     */
    @PostMapping("/add")
    public Result insert(@RequestBody Partenr partenr) {
        if (StrUtil.isEmptyIfStr(partenr)){
            return ResultUtil.error("数据为空");
        }
        return this.partenrService.addpartenr(partenr);
    }

    /**
     * 修改数据
     *
     * @param partenr 实体对象
     * @return 修改结果
     */
    @PostMapping("/edit")
    public Result update(@RequestBody Partenr partenr) {

        if (StrUtil.isEmptyIfStr(partenr)){
            return ResultUtil.error("数据为空");
        }
        return this.partenrService.updpartenr(partenr);

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
            return this.partenrService.delPatent(idList);
        }else {
            return ResultUtil.error("数据为空");
        }

    }
}

