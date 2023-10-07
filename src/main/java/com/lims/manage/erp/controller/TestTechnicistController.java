package com.lims.manage.erp.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.TechnicistCapacity;
import com.lims.manage.erp.entity.TestProduct;
import com.lims.manage.erp.entity.TestProductType;
import com.lims.manage.erp.entity.TestTechnicist;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestTechnicistService;
import com.lims.manage.erp.vo.TestTechnicistVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 技术人员(TestTechnicistVo)表控制层
 *
 * @author makejava
 * @since 2022-02-23 09:14:41
 */
@RestController
@RequestMapping("testTechnicist")
public class TestTechnicistController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestTechnicistService testTechnicistService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param testTechnicistVo 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public Result selectAll(Page<TestTechnicistVo> page, TestTechnicistVo testTechnicistVo) {
        QueryWrapper<TestTechnicist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("i.del_flag",0);
        if (testTechnicistVo.getUserId()!=null&&testTechnicistVo.getUserId()!=""){
            queryWrapper.eq("i.user_id",testTechnicistVo.getUserId());
        }
        if (testTechnicistVo.getTeamId()!=null&&testTechnicistVo.getTeamId()!=0){
            queryWrapper.eq("i.team_id",testTechnicistVo.getTeamId());
        }
        queryWrapper.orderByDesc("i.create_time");
        IPage<TestTechnicistVo> teamIPage = this.testTechnicistService.getListPage(page, queryWrapper);
        return ResultUtil.success(teamIPage);
    }

    /**
     * 查询授权人员参数详情
     * @param id
     * @return
     */
    @GetMapping("/getTechnicistCapacity")
    public Result getTechnicistCapacity(Integer id) {
        if (id == null){
            return ResultUtil.error("缺少参数");
        }
        return ResultUtil.success(testTechnicistService.getCapacityMessage(id));
    }

    /**
     * 获取技术人员授权参数来源数据
     * @return
     */
    @GetMapping("/getProductTypeAndProduct")
    public Result getProductTypeAndProduct(){
        List<TestProductType> list = testTechnicistService.getProductTypeAndProduct();
        return ResultUtil.success(list);
    }

    /**
     * 查询用户列表
     * @return
     */
    @GetMapping("/userList")
    public Result selectUserList() {
        return ResultUtil.success(this.testTechnicistService.getUserList());
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public Result selectOne(@PathVariable Serializable id) {
        if (id!=""&&id!=null){
            TestTechnicist byId = this.testTechnicistService.getById(id);
            List<TestProductType> capacityMessage = testTechnicistService.getCapacityMessage((Integer) id);
            byId.setList(capacityMessage);
            return ResultUtil.success(byId);
        }else {
            return ResultUtil.error("参数为空");
        }

    }

    /**
     * 新增数据
     *
     * @param testTechnicist 实体对象
     * @return 新增结果
     */
    @PostMapping("/add")
    public Result insert(@RequestBody TestTechnicist testTechnicist) {
        if (StrUtil.isEmptyIfStr(testTechnicist)){
            return ResultUtil.error("数据为空");
        }
        return this.testTechnicistService.addTestTechnicist(testTechnicist);
    }

    /**
     * 修改数据
     *
     * @param testTechnicist 实体对象
     * @return 修改结果
     */
    @PostMapping("/edit")
    public Result update(@RequestBody TestTechnicist testTechnicist) {
        if (StrUtil.isEmptyIfStr(testTechnicist)){
            return ResultUtil.error("数据为空");
        }
        return this.testTechnicistService.updTestTechnicist(testTechnicist);
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
            return this.testTechnicistService.delTestTechnicist(idList);
        }else {
            return ResultUtil.error("数据为空");
        }
    }
}

