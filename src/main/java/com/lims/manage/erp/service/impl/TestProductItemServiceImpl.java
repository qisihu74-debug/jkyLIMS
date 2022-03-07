package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.mapper.TestProductItemDao;
import com.lims.manage.erp.entity.TestProductItem;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestProductItemService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 产品检测项(TestProductItem)表服务实现类
 *
 * @author makejava
 * @since 2022-03-02 15:14:51
 */
@Service("testProductItemService")
public class TestProductItemServiceImpl extends ServiceImpl<TestProductItemDao, TestProductItem> implements TestProductItemService {

    @Override
    public Result addTestProductItem(TestProductItem testProductItem) {
        if (testProductItem.getProductId()==null){
            return ResultUtil.error("产品ID不能为空");
        }
        if (testProductItem.getCheckItemName()==null){
            return ResultUtil.error("检测项目名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestProductItem>().eq("check_item_name",testProductItem.getCheckItemName()))!=null){
            return ResultUtil.error("检测项名称重复");
        }
        testProductItem.setStatus("0");
        testProductItem.setDelFlag(0);
        testProductItem.setCreateTime(new Date());
        if (this.save(testProductItem)){
            return ResultUtil.success("添加成功!");
        }else {
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

    @Override
    public Result updTestProductItem(TestProductItem testProductItem) {
        if (testProductItem.getCheckItemId()==null){
            return ResultUtil.error("修改对象ID不能为空");
        }
        if (testProductItem.getProductId()==null){
            return ResultUtil.error("产品ID不能为空");
        }
        if (testProductItem.getCheckItemName()==null){
            return ResultUtil.error("检测项目名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestProductItem>().eq("del_flag",0).ne("check_item_id",testProductItem.getCheckItemId()).eq("check_item_name",testProductItem.getCheckItemName()))!=null){
            return ResultUtil.error("检测项名称重复");
        }
        testProductItem.setUpdateTime(new Date());
        if (this.save(testProductItem)){
            return ResultUtil.success("修改成功!");
        }else {
            return ResultUtil.error("修改失败，未知异常!");
        }
    }

    @Override
    public Result delTestProductItem(List<Long> idList) {
        List<TestProductItem> testMethods=new ArrayList<>();
        for (Long aLong : idList) {
            TestProductItem testMethod=new TestProductItem();
            testMethod.setUpdateTime(new Date());
            testMethod.setDelFlag(1);
            testMethod.setCheckItemId(aLong.intValue());
            testMethods.add(testMethod);
        }
        if (this.updateBatchById(testMethods)){
            return ResultUtil.success("删除成功");
        }else {
            return ResultUtil.error("删除失败");
        }
    }
}

