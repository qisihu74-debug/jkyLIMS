package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.mapper.TestProductItemDao;
import com.lims.manage.erp.entity.TestProductItem;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.TestProductItemService;
import com.lims.manage.erp.vo.TestProductItemVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
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
    public Result addTestProductItem(TestProductItemVo testProductItemVo) {
        if (testProductItemVo.getCheckItemName()==null){
            return ResultUtil.error("检测项目名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestProductItem>().eq("del_flag",0).eq("check_item_name",testProductItemVo.getCheckItemName()))!=null){
            return ResultUtil.error("检测项名称重复");
        }
        testProductItemVo.setStatus("0");
        testProductItemVo.setDelFlag(0);
        testProductItemVo.setCreateTime(new Date());
        TestProductItem testProductItem=new TestProductItem();
        BeanUtils.copyProperties(testProductItemVo,testProductItem);
        if (testProductItemVo.getMethodList()!=null&&testProductItemVo.getMethodList().size()!=0){
            testProductItem.setMethodIds(StringUtils.join(testProductItemVo.getMethodList().toArray(),","));
        }else {
            testProductItem.setMethodIds(null);
        }
        if (this.save(testProductItem)){
            return ResultUtil.success("添加成功!");
        }else {
            return ResultUtil.error("添加失败，未知异常!");
        }
    }

    @Override
    public Result updTestProductItem(TestProductItemVo testProductItemVo) {
        if (testProductItemVo.getCheckItemId()==null) {
            return ResultUtil.error("修改对象ID不能为空");
        }
        if (testProductItemVo.getCheckItemName()==null){
            return ResultUtil.error("检测项目名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestProductItem>().eq("del_flag",0).ne("check_item_id",testProductItemVo.getCheckItemId()).eq("check_item_name",testProductItemVo.getCheckItemName()))!=null){
            return ResultUtil.error("检测项名称重复");
        }
        testProductItemVo.setUpdateTime(new Date());
        TestProductItem testProductItem=new TestProductItem();
        BeanUtils.copyProperties(testProductItemVo,testProductItem);
        if (testProductItemVo.getMethodList().size()!=0){
            testProductItem.setMethodIds(StringUtils.join(testProductItemVo.getMethodList().toArray(),","));
        }else {
            testProductItem.setMethodIds(null);
        }
        if (this.updateById(testProductItem)){
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

