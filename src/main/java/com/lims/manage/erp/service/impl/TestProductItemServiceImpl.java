package com.lims.manage.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.TestProductItemDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.*;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.TestProductItemParamVo;
import com.lims.manage.erp.vo.TestProductItemSelVo;
import com.lims.manage.erp.vo.TestProductItemTreeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    /*检测方法*/
    @Resource
    private TestMethodService testMethodService;
    /*检测方法关联*/
    @Resource
    private TestProductItemMethodRelService testProductItemMethodRelService;
    /*检测依据*/
    @Resource
    private TestStandardFileService testStandardFileService;
    /*检测依据关联*/
    @Resource
    private TestProductItemStandardFileRelService testProductItemStandardFileRelService;
    /*检测设备*/
    @Resource
    private TestInstrumentTypeService typeService;
    /*检测设备关联*/
    @Resource
    private TestProductItemInstrumentTypeRelService testProductItemInstrumentTypeRelService;
    /*模板*/
    @Resource
    private TestReportTemplateService testReportTemplateService;
    /*日志*/
    @Resource
    private LogManagerService logManagerService;
    @Override
    public Result addTestProductItem(TestProductItemParamVo testProductItemParamVo) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
                if (testProductItemParamVo.getTestProductItem().getCheckItemName()==null){
                    return ResultUtil.error("检测项目名称不能为空");
                }
                if (this.getOne(new QueryWrapper<TestProductItem>().eq("product_id",testProductItemParamVo.getTestProductItem().getProductId()).eq("del_flag",0).eq("check_item_name",testProductItemParamVo.getTestProductItem().getCheckItemName()))!=null){
                    return ResultUtil.error("检测项名称重复");
                }
                testProductItemParamVo.getTestProductItem().setStatus("0");
                testProductItemParamVo.getTestProductItem().setDelFlag(0);
                testProductItemParamVo.getTestProductItem().setCreateTime(new Date());
                if (this.save(testProductItemParamVo.getTestProductItem())){
                    //设置检查项检测方法
                    if (testProductItemParamVo.getMethodIds()!=null&&testProductItemParamVo.getMethodIds().size()>0){
                        for (Integer MethodId : testProductItemParamVo.getMethodIds()) {
                            testProductItemMethodRelService.save(new TestProductItemMethodRel(testProductItemParamVo.getTestProductItem().getCheckItemId(),MethodId));
                        }
                    }
                    //设置检查项检测依据
                    if (testProductItemParamVo.getStandardIds()!=null&&testProductItemParamVo.getStandardIds().size()>0){
                        for (Integer StandardId : testProductItemParamVo.getStandardIds()) {
                            testProductItemStandardFileRelService.save(new TestProductItemStandardFileRel(testProductItemParamVo.getTestProductItem().getCheckItemId(),StandardId));
                        }
                    }
                    //设置检查项检测设置
                    if (testProductItemParamVo.getTypeIds()!=null&&testProductItemParamVo.getTypeIds().size()>0){
                        for (Integer TypeId : testProductItemParamVo.getTypeIds()) {
                            testProductItemInstrumentTypeRelService.save(new TestProductItemInstrumentTypeRel(testProductItemParamVo.getTestProductItem().getCheckItemId(),TypeId));
                        }
                    }
                    logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加产品检测项"+testProductItemParamVo.getTestProductItem().getCheckItemId()+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
                    return ResultUtil.success("添加成功");
                }else {
                    logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加产品检测项失败!", Const.DETECTION_MANAGEMENT_LOG,false);
                    return ResultUtil.error("添加检查项失败，未知异常!");
                }
    }

    @Override
    public Result updTestProductItem(TestProductItemParamVo testProductItemParamVo) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testProductItemParamVo.getTestProductItem().getCheckItemId()==null){
            return ResultUtil.error("缺少修改对象");
        }
        if (testProductItemParamVo.getTestProductItem().getCheckItemName()==null){
            return ResultUtil.error("检测项目名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestProductItem>().eq("product_id",testProductItemParamVo.getTestProductItem().getProductId()).ne("check_item_id",testProductItemParamVo.getTestProductItem().getCheckItemId()).eq("del_flag",0).eq("check_item_name",testProductItemParamVo.getTestProductItem().getCheckItemName()))!=null){
            return ResultUtil.error("检测项名称重复");
        }
        testProductItemParamVo.getTestProductItem().setUpdateTime(new Date());
        if (this.updateById(testProductItemParamVo.getTestProductItem())){
            //删除原有检测方法
            testProductItemMethodRelService.remove(new QueryWrapper<TestProductItemMethodRel>().eq("check_item_id",testProductItemParamVo.getTestProductItem().getCheckItemId()));
            //设置检查项检测方法
            if (testProductItemParamVo.getMethodIds()!=null&&testProductItemParamVo.getMethodIds().size()>0){
                for (Integer MethodId : testProductItemParamVo.getMethodIds()) {
                    testProductItemMethodRelService.save(new TestProductItemMethodRel(testProductItemParamVo.getTestProductItem().getCheckItemId(),MethodId));
                }
            }
            //删除原有检测依据
            testProductItemStandardFileRelService.remove(new QueryWrapper<TestProductItemStandardFileRel>().eq("check_item_id",testProductItemParamVo.getTestProductItem().getCheckItemId()));
            //设置检查项检测依据
            if (testProductItemParamVo.getStandardIds()!=null&&testProductItemParamVo.getStandardIds().size()>0){
                for (Integer StandardId : testProductItemParamVo.getStandardIds()) {
                    testProductItemStandardFileRelService.save(new TestProductItemStandardFileRel(testProductItemParamVo.getTestProductItem().getCheckItemId(),StandardId));
                }
            }
            //删除原有检测设备
            testProductItemInstrumentTypeRelService.remove(new QueryWrapper<TestProductItemInstrumentTypeRel>().eq("check_item_id",testProductItemParamVo.getTestProductItem().getCheckItemId()));
            //设置检查项检测设备
            if (testProductItemParamVo.getTypeIds()!=null&&testProductItemParamVo.getTypeIds().size()>0){
                for (Integer TypeId : testProductItemParamVo.getTypeIds()) {
                    testProductItemInstrumentTypeRelService.save(new TestProductItemInstrumentTypeRel(testProductItemParamVo.getTestProductItem().getCheckItemId(),TypeId));
                }
            }
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改产品检测项"+testProductItemParamVo.getTestProductItem().getCheckItemId()+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("修改成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改产品检测项"+testProductItemParamVo.getTestProductItem().getCheckItemId()+"失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("修改检查项失败，未知异常!");
        }
    }

    @Override
    public Result delTestProductItem(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        List<TestProductItem> testMethods=new ArrayList<>();
        for (Long aLong : idList) {
            TestProductItem testMethod=new TestProductItem();
            testMethod.setUpdateTime(new Date());
            testMethod.setDelFlag(1);
            testMethod.setCheckItemId(aLong.intValue());
            testMethods.add(testMethod);
        }
        String idStr=idList.toString();
        if (this.updateBatchById(testMethods)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除产品检测项"+idStr+"成功!", Const.DETECTION_MANAGEMENT_LOG,true);
            return ResultUtil.success("删除成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除产品检测项"+idStr+"失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("删除失败");
        }
    }

    @Override
    public TestProductItemParamVo getItemParamVo(TestProductItem testProductItem) {
        TestProductItemParamVo testProductItemParamVo=new TestProductItemParamVo();
        testProductItemParamVo.setTestProductItem(testProductItem);
        List<TestProductItemMethodRel>  testProductItemMethodRels=testProductItemMethodRelService.list(new QueryWrapper<TestProductItemMethodRel>().eq("check_item_id",testProductItem.getCheckItemId()));
        List<TestProductItemStandardFileRel>  testProductItemStandardFileRels=testProductItemStandardFileRelService.list(new QueryWrapper<TestProductItemStandardFileRel>().eq("check_item_id",testProductItem.getCheckItemId()));
        List<TestProductItemInstrumentTypeRel>  testProductItemInstrumentTypeRels=testProductItemInstrumentTypeRelService.list(new QueryWrapper<TestProductItemInstrumentTypeRel>().eq("check_item_id",testProductItem.getCheckItemId()));
        List<Integer> MethodRel=new ArrayList<>();
        List<Integer> StandardFileRel=new ArrayList<>();
        List<Integer> TypeRel=new ArrayList<>();
        for (TestProductItemMethodRel testProductItemMethodRel : testProductItemMethodRels) {
            MethodRel.add(testProductItemMethodRel.getMethodId());
        }
        for (TestProductItemStandardFileRel testProductItemStandardFileRel : testProductItemStandardFileRels) {
            StandardFileRel.add(testProductItemStandardFileRel.getStandardFileId());
        }
        for (TestProductItemInstrumentTypeRel testProductItemInstrumentTypeRel : testProductItemInstrumentTypeRels) {
            TypeRel.add(testProductItemInstrumentTypeRel.getIntrusmentTypeId());
        }
        testProductItemParamVo.setMethodIds(MethodRel);
        testProductItemParamVo.setStandardIds(StandardFileRel);
        testProductItemParamVo.setTypeIds(TypeRel);
        return testProductItemParamVo;
    }

    @Override
    public List<TestProductItemSelVo> getTestProductSelVoList(Integer ProductId) {
        //初始化
        List<TestProductItemSelVo> testProductItemSelVos=new ArrayList<>();
        //查询产品检测项
        List<TestProductItem> testProductItems=this.list(new QueryWrapper<TestProductItem>().eq("del_flag",0).eq("product_id",ProductId));
        //遍历
        for (TestProductItem testProductItem : testProductItems) {
            TestProductItemSelVo testProductItemSelVo=new TestProductItemSelVo();
            //设置检查项基础信息
            testProductItemSelVo.setTestProductItem(testProductItem);
            //设置检测项模板
            if (testProductItem.getReportModelId()!=null){
                testProductItemSelVo.setReportName(testReportTemplateService.getById(testProductItem.getReportModelId()).getReportName());
            }
            //设置检查项方法
            List<TestProductItemMethodRel> testProductItemMethodRels=testProductItemMethodRelService.list(new QueryWrapper<TestProductItemMethodRel>().eq("check_item_id",testProductItem.getCheckItemId()));
            List<String> ItemMethodRelList=new ArrayList<>();
            for (TestProductItemMethodRel testProductItemMethodRel : testProductItemMethodRels) {
                ItemMethodRelList.add(testMethodService.getById(testProductItemMethodRel.getMethodId()).getName());
            }
            testProductItemSelVo.setMethodList(ItemMethodRelList);
            //设置检查项设备
            List<TestProductItemInstrumentTypeRel> testProductItemInstrumentTypeRels=testProductItemInstrumentTypeRelService.list(new QueryWrapper<TestProductItemInstrumentTypeRel>().eq("check_item_id",testProductItem.getCheckItemId()));
            List<String> ItemInstrumentTypeList=new ArrayList<>();
            for (TestProductItemInstrumentTypeRel testProductItemInstrumentTypeRel : testProductItemInstrumentTypeRels) {
                ItemInstrumentTypeList.add(typeService.getById(testProductItemInstrumentTypeRel.getIntrusmentTypeId()).getName());
            }
            testProductItemSelVo.setTypeList(ItemInstrumentTypeList);
            //设置检测项依据
            List<TestProductItemStandardFileRel> testProductStandardFileRels=testProductItemStandardFileRelService.list(new QueryWrapper<TestProductItemStandardFileRel>().eq("check_item_id",testProductItem.getCheckItemId()));
            List<String> StandarString=new ArrayList<>();
            for (TestProductItemStandardFileRel testProductItemStandardFileRel : testProductStandardFileRels) {
                StandarString.add(testStandardFileService.getById(testProductItemStandardFileRel.getStandardFileId()).getName());
            }
            testProductItemSelVo.setItemStandardList(StandarString);
            //追加进Vo集合
            testProductItemSelVos.add(testProductItemSelVo);
        }
        return testProductItemSelVos;
    }

    @Override
    public List<TestProductItemTreeVo> getTreeList(TestProductItem testProductItem) {
        List<TestProductItemTreeVo> treeVos=new ArrayList<>();
        QueryWrapper<TestProductItem> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("del_flag",0);
        if (testProductItem.getProductId()!=null){
            queryWrapper.eq("product_id",testProductItem.getProductId());
        }
        if (testProductItem.getCheckItemPid()!=null){
            queryWrapper.eq("check_item_pid",testProductItem.getProductId());
        }else {
            queryWrapper.eq("check_item_pid",0);
        }
        queryWrapper.orderByDesc("create_time");
        List<TestProductItem> list=this.list(queryWrapper);
        if (list.size()!=0){
            for (TestProductItem productItem : list) {
                TestProductItemTreeVo treeVo=new TestProductItemTreeVo();
                BeanUtils.copyProperties(productItem,treeVo);
                TestProductItem testProductItem1=new TestProductItem();
                testProductItem1.setProductId(productItem.getProductId());
                testProductItem1.setCheckItemPid(productItem.getCheckItemId());
                treeVo.setChildren(getTreeList(testProductItem1));
                treeVos.add(treeVo);
            }
        }
        return treeVos;
    }
}

