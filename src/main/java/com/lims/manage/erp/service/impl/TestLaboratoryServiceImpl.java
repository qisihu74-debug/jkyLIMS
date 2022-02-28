package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.TestInstrument;
import com.lims.manage.erp.mapper.TestLaboratoryDao;
import com.lims.manage.erp.entity.TestLaboratory;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestLaboratoryService;
import com.lims.manage.erp.vo.TestLaboratoryVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 实验室管理(TestLaboratory)表服务实现类
 *
 * @author makejava
 * @since 2022-02-25 10:08:36
 */
@Service("testLaboratoryService")
public class TestLaboratoryServiceImpl extends ServiceImpl<TestLaboratoryDao, TestLaboratory> implements TestLaboratoryService {
    @Resource
    private TestLaboratoryDao testLaboratoryDao;
    @Override
    public Result addLaboratory(TestLaboratory testLaboratory) {
        if (testLaboratory.getName()==null){
            return ResultUtil.error("实验室名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestLaboratory>().eq("name",testLaboratory.getName()))!=null){
            return ResultUtil.error("实验室名称重复");
        }
        testLaboratory.setStatus("0");
        testLaboratory.setDelFlag(0);
        testLaboratory.setCreateTime(new Date());
        testLaboratory.setUpdateTime(null);
        if (this.save(testLaboratory)){
            return ResultUtil.success("添加成功!");
        }else {
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

    @Override
    public Result updLaboratory(TestLaboratory testLaboratory) {
        if (testLaboratory.getId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        if (testLaboratory.getName()==null){
            return ResultUtil.error("实验室名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestLaboratory>().eq("name",testLaboratory.getName()).ne("id",testLaboratory.getId()))!=null){
            return ResultUtil.error("实验室名称重复");
        }
        testLaboratory.setUpdateTime(new Date());
        if (this.updateById(testLaboratory)){
            return ResultUtil.success("修改成功!");
        }else {
            return ResultUtil.error("修改失败，未知异常!");
        }
    }

    @Override
    public Result delLaboratory(List<Long> idList) {
        List<TestLaboratory> testLaboratoryList=new ArrayList<>();
        for (Long aLong : idList) {
            TestLaboratory testLaboratory=new TestLaboratory();
            testLaboratory.setUpdateTime(new Date());
            testLaboratory.setDelFlag(1);
            testLaboratory.setId(aLong.intValue());
            testLaboratoryList.add(testLaboratory);
        }
        if (this.updateBatchById(testLaboratoryList)){
            return ResultUtil.success("删除成功");
        }else {
            return ResultUtil.error("删除失败");
        }
    }

    @Override
    public IPage<TestLaboratoryVo> getPageList(Page<TestLaboratoryVo> page, QueryWrapper<TestLaboratory> queryWrapper) {
        return testLaboratoryDao.getListPage(page,queryWrapper);
    }
}

