package com.lims.manage.erp.controller;



import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lims.manage.erp.annotation.Log;
import com.lims.manage.erp.entity.Mehord;
import com.lims.manage.erp.entity.Patent;
import com.lims.manage.erp.enums.BusinessType;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.MehordService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * (Mehord)表控制层
 *
 * @author makejava
 * @since 2022-03-07 16:29:25
 */
@RestController
@RequestMapping("mehord")
public class MehordController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private MehordService mehordService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(Page<Mehord> page, Mehord Mehord) {

        QueryWrapper<Mehord> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("del_flag",0);
        if (Mehord.getTitle()!=null){
            queryWrapper.like("title",Mehord.getTitle());
        }
        queryWrapper.orderByDesc("create_time");
        return ResultUtil.success(this.mehordService.page(page, queryWrapper));
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
            Mehord Mehord=this.mehordService.getOne(new QueryWrapper<Mehord>().eq("id",id).eq("del_flag",0));
            return ResultUtil.success(Mehord);
        }else {
            return ResultUtil.error("参数为空");
        }

    }

    /**
     * 新增数据
     *
     * @param mehord 实体对象
     * @return 新增结果
     */
    @Log(title = "新增锦囊妙计", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public Result insert(@RequestBody Mehord mehord) {
        if (StrUtil.isEmptyIfStr(mehord)){
            return ResultUtil.error("数据为空");
        }
        return mehordService.addMethod(mehord);

    }

    /**
     * 修改数据
     *
     * @param mehord 实体对象
     * @return 修改结果
     */
    @Log(title = "修改锦囊妙计", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    public Result update(@RequestBody Mehord mehord) {

        return this.mehordService.updMethod(mehord);
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @Log(title = "删除锦囊妙计", businessType = BusinessType.DELETE)
    @PostMapping("/del")
    public Result delete(@RequestBody List<Long> idList) {
        if (idList.size()!=0){
            return this.mehordService.delMethod(idList);
        }else {
            return ResultUtil.error("数据为空");
        }
    }
}

