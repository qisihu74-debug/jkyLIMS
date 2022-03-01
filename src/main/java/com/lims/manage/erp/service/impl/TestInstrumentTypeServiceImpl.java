package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.TestInstrumentType;
import com.lims.manage.erp.entity.TestLaboratory;
import com.lims.manage.erp.mapper.TestInstrumentTypeDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestInstrumentTypeService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 仪器大类(TestInstrumentType)表服务实现类
 *
 * @author makejava
 * @since 2022-03-01 09:14:39
 */
@Service("testInstrumentTypeService")
public class TestInstrumentTypeServiceImpl extends ServiceImpl<TestInstrumentTypeDao, TestInstrumentType> implements TestInstrumentTypeService {
    @Override
    public Result addTestInstrumentType(TestInstrumentType testInstrumentType) {
        if (testInstrumentType.getName()==null){
            return ResultUtil.error("设备分类名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestInstrumentType>().eq("name",testInstrumentType.getName()))!=null){
            return ResultUtil.error("分类名称重复");
        }
        testInstrumentType.setStatus("0");
        testInstrumentType.setDelFlag(0);
        testInstrumentType.setCreateTime(new Date());
        if (this.save(testInstrumentType)){
            return ResultUtil.success("添加成功!");
        }else {
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

    @Override
    public Result updTestInstrumentType(TestInstrumentType testInstrumentType) {
        if (testInstrumentType.getId()==null){
            return ResultUtil.error("修改对象ID为空");
        }
        if (testInstrumentType.getName()==null){
            return ResultUtil.error("设备分类名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestInstrumentType>().eq("name",testInstrumentType.getName()).eq("del_flag",0).ne("id",testInstrumentType.getId()))!=null){
            return ResultUtil.error("分类名称重复");
        }
        testInstrumentType.setUpdateTime(new Date());
        if (this.updateById(testInstrumentType)){
            return ResultUtil.success("修改成功!");
        }else {
            return ResultUtil.error("修改失败，未知异常!");
        }
    }

    @Override
    public Result delTestInstrumentType(List<Long> idList) {
        List<TestInstrumentType> testLaboratoryList=new ArrayList<>();
        for (Long aLong : idList) {
            TestInstrumentType testLaboratory=new TestInstrumentType();
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
}

