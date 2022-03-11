package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.mapper.TestProductItemMethodRelDao;
import com.lims.manage.erp.entity.TestProductItemMethodRel;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestProductItemMethodRelService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 检测项的检测方法(TestProductItemMethodRel)表服务实现类
 *
 * @author makejava
 * @since 2022-03-02 15:15:27
 */
@Service("testProductItemMethodRelService")
public class TestProductItemMethodRelServiceImpl extends ServiceImpl<TestProductItemMethodRelDao, TestProductItemMethodRel> implements TestProductItemMethodRelService {

    @Override
    public Result addTestMethodRel(TestProductItemMethodRel testProductItemMethodRel) {
        if (testProductItemMethodRel.getCheckItemId()==null){
            return ResultUtil.error("检测项ID不能为空");
        }
        if (testProductItemMethodRel.getMethodId()==null){
            return ResultUtil.error("检测方法ID不能为空");
        }
        if (this.getOne(new QueryWrapper<TestProductItemMethodRel>().eq("method_id",testProductItemMethodRel.getMethodId()).eq("check_item_id",testProductItemMethodRel.getCheckItemId()))!=null){
            return ResultUtil.error("检测方法重复");
        }
        if (this.save(testProductItemMethodRel)){
            return ResultUtil.success("添加成功!");
        }else {
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

    @Override
    public Result updTestMethodRel(TestProductItemMethodRel testProductItemMethodRel) {
        if (testProductItemMethodRel.getCheckItemId()==null){
            return ResultUtil.error("检测项ID不能为空");
        }
        if (testProductItemMethodRel.getMethodId()==null){
            return ResultUtil.error("检测方法ID不能为空");
        }
        if (this.getOne(new QueryWrapper<TestProductItemMethodRel>().eq("method_id",testProductItemMethodRel.getMethodId()).eq("check_item_id",testProductItemMethodRel.getCheckItemId()).eq("del_flag",0).ne("id",testProductItemMethodRel.getId()))!=null){
            return ResultUtil.error("检测方法重复");
        }
        if (this.save(testProductItemMethodRel)){
            return ResultUtil.success("修改成功!");
        }else {
            return ResultUtil.error("修改失败，未知异常!");
        }
    }

    @Override
    public Result delTestMethodRel(List<Long> idList) {
        List<TestProductItemMethodRel> testMethods=new ArrayList<>();
        for (Long aLong : idList) {
            TestProductItemMethodRel testMethod=new TestProductItemMethodRel();
            testMethod.setId(aLong.intValue());
            testMethods.add(testMethod);
        }
        if (this.updateBatchById(testMethods)){
            return ResultUtil.success("删除成功");
        }else {
            return ResultUtil.error("删除失败");
        }
    }
}

