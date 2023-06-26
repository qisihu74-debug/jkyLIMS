package com.lims.manage.erp.service.impl;

import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.aspose.cells.WorksheetCollection;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.TestProductItemDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.*;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.LabelValueVo;
import com.lims.manage.erp.vo.TestProductItemParamVo;
import com.lims.manage.erp.vo.TestProductItemSelVo;
import com.lims.manage.erp.vo.TestProductItemTreeVo;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
    private TestInstrumentService typeService;
    /*检测设备关联*/
    @Resource
    private TestProductItemInstrumentTypeRelService testProductItemInstrumentTypeRelService;
    /*模板*/
    @Resource
    private TestReportTemplateService testReportTemplateService;
    /*科室检测项*/
    @Resource
    private TestCheckItemTeamRelService testCheckItemTeamRelService;
    /*科室*/
    @Resource
    private TestTeamService testTeamService;
    /*日志*/
    @Resource
    private LogManagerService logManagerService;
    /*文件*/
    @Resource
    private SysOssService sysOssService;
    @Resource
    private TestProductItemDao testProductItemDao;
    @Override
    public Result addTestProductItem(TestProductItemParamVo testProductItemParamVo) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if(testProductItemParamVo.getTestProductItem().getCheckItemPid()==null){
            testProductItemParamVo.getTestProductItem().setCheckItemPid(0);
        }
        if (testProductItemParamVo.getTestProductItem().getCheckItemName()==null){
            return ResultUtil.error("检测项目名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestProductItem>().eq("product_id",testProductItemParamVo.getTestProductItem().getProductId()).eq("del_flag",0).eq("check_item_pid",testProductItemParamVo.getTestProductItem().getCheckItemPid()).eq("check_item_name",testProductItemParamVo.getTestProductItem().getCheckItemName()))!=null){
                    return ResultUtil.error("同层检测项名称不能重复");
                }
        testProductItemParamVo.getTestProductItem().setStatus("0");
        testProductItemParamVo.getTestProductItem().setDelFlag(0);
        testProductItemParamVo.getTestProductItem().setCreateTime(new Date());
        if (this.save(testProductItemParamVo.getTestProductItem())){
            //设置检查项检测依据
            if (testProductItemParamVo.getStandardIds()!=null&&testProductItemParamVo.getStandardIds().size()>0){
                for (Integer StandardId : testProductItemParamVo.getStandardIds()) {
                    testProductItemStandardFileRelService.save(new TestProductItemStandardFileRel(testProductItemParamVo.getTestProductItem().getCheckItemId(),StandardId));
                }
            }
            //设置检查项检测设备
            if (testProductItemParamVo.getTypeIds()!=null&&testProductItemParamVo.getTypeIds().size()>0){
                for (Integer TypeId : testProductItemParamVo.getTypeIds()) {
                    testProductItemInstrumentTypeRelService.save(new TestProductItemInstrumentTypeRel(testProductItemParamVo.getTestProductItem().getCheckItemId(),TypeId));
                }
            }
            //设置检测项所属科室
            if (testProductItemParamVo.getItemIds()!=null&&testProductItemParamVo.getItemIds().size()>0){
                for (Integer itemId : testProductItemParamVo.getItemIds()) {
                    testCheckItemTeamRelService.save(new TestCheckItemTeamRel(testProductItemParamVo.getTestProductItem().getCheckItemId(),itemId,testProductItemParamVo.getTestProductItem().getProductId()));
                }
            }
            //设置检测项绑定的报告原始记录sheet
            if(testProductItemParamVo.getSheetIndex() != null && testProductItemParamVo.getSheetIndex().size()>0){
                for (int i = 0; i < testProductItemParamVo.getSheetIndex().size(); i++) {
                    List<ItemSheetRelEntity> relList = Lists.newArrayList();
                    Integer checkItemId = testProductItemParamVo.getTestProductItem().getCheckItemId();
                    Integer sheetIndex = testProductItemParamVo.getSheetIndex().get(i);
                    ItemSheetRelEntity relEntity = new ItemSheetRelEntity();
                    relEntity.setCheckItemId(checkItemId);
                    relEntity.setSheetIndex(sheetIndex);
                    relList.add(relEntity);
                    testProductItemDao.addItemSheetRel(relList);
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
        if (this.getOne(new QueryWrapper<TestProductItem>().eq("check_item_pid",testProductItemParamVo.getTestProductItem().getCheckItemPid()).eq("product_id",testProductItemParamVo.getTestProductItem().getProductId()).ne("check_item_id",testProductItemParamVo.getTestProductItem().getCheckItemId()).eq("del_flag",0).eq("check_item_name",testProductItemParamVo.getTestProductItem().getCheckItemName()))!=null){
            return ResultUtil.error("同层检测项名称不能重复");
        }
        testProductItemParamVo.getTestProductItem().setUpdateTime(new Date());
        if (this.updateById(testProductItemParamVo.getTestProductItem())){
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
            //删除原有检测设备
            testCheckItemTeamRelService.remove(new QueryWrapper<TestCheckItemTeamRel>().eq("check_item_id",testProductItemParamVo.getTestProductItem().getCheckItemId()));
            //设置检测项所属科室
            if (testProductItemParamVo.getItemIds()!=null&&testProductItemParamVo.getItemIds().size()>0){
                for (Integer itemId : testProductItemParamVo.getItemIds()) {
                    testCheckItemTeamRelService.save(new TestCheckItemTeamRel(testProductItemParamVo.getTestProductItem().getCheckItemId(),itemId,testProductItemParamVo.getTestProductItem().getProductId()));
                }
            }
            //删除检测项绑定的报告原始记录sheet
            testProductItemDao.deleteItemSheetRel(testProductItemParamVo.getTestProductItem().getCheckItemId());
            //设置检测项绑定的报告原始记录sheet
            if(testProductItemParamVo.getSheetIndex() != null && testProductItemParamVo.getSheetIndex().size()>0){
                for (int i = 0; i < testProductItemParamVo.getSheetIndex().size(); i++) {
                    List<ItemSheetRelEntity> relList = Lists.newArrayList();
                    Integer checkItemId = testProductItemParamVo.getTestProductItem().getCheckItemId();
                    Integer sheetIndex = testProductItemParamVo.getSheetIndex().get(i);
                    ItemSheetRelEntity relEntity = new ItemSheetRelEntity();
                    relEntity.setCheckItemId(checkItemId);
                    relEntity.setSheetIndex(sheetIndex);
                    relList.add(relEntity);
                    testProductItemDao.addItemSheetRel(relList);
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
        TestProductItem testProductItem=getById(idList.get(0));
        testProductItem.setDelFlag(1);
        if (testProductItem.getIcon()!=null){
            sysOssService.delAnnounce(testProductItem.getIcon());
        }
        if (this.updateById(testProductItem)) {
            if (this.delChildren(idList.get(0))) {
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername() + "删除产品检测项" + idList.get(0) + "成功!", Const.DETECTION_MANAGEMENT_LOG, true);
                return ResultUtil.success("删除成功");
            } else {
                logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername() + "删除产品检测项" + idList.get(0) + "失败!", Const.DETECTION_MANAGEMENT_LOG, false);
                return ResultUtil.error("删除失败");
            }
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername() + "删除产品检测项" + idList.get(0) + "失败!", Const.DETECTION_MANAGEMENT_LOG, false);
            return ResultUtil.error("删除失败");
        }
    }

    @Override
    public Result disableStatusTestProductItem(List<Long> idList) {
        TestProductItem testProductItem=getById(idList.get(0));
        testProductItem.setStatus("1");
        if (this.updateById(testProductItem)){
            if (this.disableStatus(idList.get(0))){
                return ResultUtil.success("禁用成功");
            }else {
                return ResultUtil.error("禁用失败");
            }
        }else {
            return ResultUtil.error("禁用失败");
        }
    }

    @Override
    public Result enableStatusTestProductItem(List<Long> idList) {
        TestProductItem testProductItem=getById(idList.get(0));
        testProductItem.setStatus("0");
        if (this.updateById(testProductItem)){
            if (this.enableStatus(testProductItem.getCheckItemPid().longValue())){
                return ResultUtil.success("启用成功");
            }else {
                return ResultUtil.error("启用失败");
            }
        }else {
            return ResultUtil.error("启用失败");
        }
    }

    public boolean delChildren(Long id){
        List<TestProductItem> testProductItemList=list(new QueryWrapper<TestProductItem>().eq("check_item_pid",id));
        for (TestProductItem testProductItem : testProductItemList) {
            testProductItem.setDelFlag(1);
            testProductItem.setUpdateTime(new Date());
            if(!this.updateById(testProductItem)){
                return false;
            }
            this.delChildren(testProductItem.getCheckItemId().longValue());
        }
        return true;
    }


    public boolean disableStatus(Long id){
        List<TestProductItem> testProductItemList=list(new QueryWrapper<TestProductItem>().eq("check_item_pid",id));
        for (TestProductItem testProductItem : testProductItemList) {
            testProductItem.setStatus("1");
            testProductItem.setUpdateTime(new Date());
            if(!this.updateById(testProductItem)){
                return false;
            }
            this.disableStatus(testProductItem.getCheckItemId().longValue());
        }
        return true;
    }
    public boolean enableStatus(Long id){
        TestProductItem testProductItem=getById(id);
        if (testProductItem==null){
            return true;
        }
        testProductItem.setStatus("0");
        if (this.updateById(testProductItem)){
            return this.enableStatus(testProductItem.getCheckItemPid().longValue());
        }else {
            return false;
        }
    }

    @Override
    public TestProductItemParamVo getItemParamVo(TestProductItem testProductItem) {
        TestProductItemParamVo testProductItemParamVo=new TestProductItemParamVo();
        testProductItemParamVo.setTestProductItem(testProductItem);
        List<TestProductItemMethodRel>  testProductItemMethodRels=testProductItemMethodRelService.list(new QueryWrapper<TestProductItemMethodRel>().eq("check_item_id",testProductItem.getCheckItemId()));
        List<TestProductItemStandardFileRel>  testProductItemStandardFileRels=testProductItemStandardFileRelService.list(new QueryWrapper<TestProductItemStandardFileRel>().eq("check_item_id",testProductItem.getCheckItemId()));
        List<TestProductItemInstrumentTypeRel>  testProductItemInstrumentTypeRels=testProductItemInstrumentTypeRelService.list(new QueryWrapper<TestProductItemInstrumentTypeRel>().eq("check_item_id",testProductItem.getCheckItemId()));
        List<TestCheckItemTeamRel>  testCheckItemTeamRels=testCheckItemTeamRelService.list(new QueryWrapper<TestCheckItemTeamRel>().eq("check_item_id",testProductItem.getCheckItemId()));
        List<Integer> MethodRel=new ArrayList<>();
        List<Integer> StandardFileRel=new ArrayList<>();
        List<Integer> TypeRel=new ArrayList<>();
        List<Integer> ItemRel=new ArrayList<>();
        for (TestProductItemMethodRel testProductItemMethodRel : testProductItemMethodRels) {
            MethodRel.add(testProductItemMethodRel.getMethodId());
        }
        for (TestProductItemStandardFileRel testProductItemStandardFileRel : testProductItemStandardFileRels) {
            StandardFileRel.add(testProductItemStandardFileRel.getStandardFileId());
        }
        for (TestProductItemInstrumentTypeRel testProductItemInstrumentTypeRel : testProductItemInstrumentTypeRels) {
            TypeRel.add(testProductItemInstrumentTypeRel.getIntrusmentTypeId());
        }
        for (TestCheckItemTeamRel testCheckItemTeamRel : testCheckItemTeamRels) {
            ItemRel.add(testCheckItemTeamRel.getTeamId());
        }
        testProductItemParamVo.setMethodIds(MethodRel);
        testProductItemParamVo.setStandardIds(StandardFileRel);
        testProductItemParamVo.setTypeIds(TypeRel);
        testProductItemParamVo.setItemIds(ItemRel);
        return testProductItemParamVo;
    }

    @Override
    public List<TestProductItemSelVo> getTestProductSelVoList(TestProductItem productItem) {
        //初始化
        List<TestProductItemSelVo> testProductItemSelVos=new ArrayList<>();
        QueryWrapper<TestProductItem> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("del_flag",0);
        if (productItem.getProductId()!=null){
            queryWrapper.eq("product_id",productItem.getProductId());
        }
        if (productItem.getCheckItemId()!=null){
            queryWrapper.eq("check_item_id",productItem.getCheckItemId());
        }
        //查询产品检测项
        List<TestProductItem> testProductItems=this.list(queryWrapper);
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
            //设置检测项所属科室
            List<TestCheckItemTeamRel> testCheckItemTeamRels=testCheckItemTeamRelService.list(new QueryWrapper<TestCheckItemTeamRel>().eq("check_item_id",testProductItem.getCheckItemId()));
            List<String> ItemTeamString=new ArrayList<>();
            for (TestCheckItemTeamRel testCheckItemTeamRel : testCheckItemTeamRels) {
                ItemTeamString.add(testTeamService.getById(testCheckItemTeamRel.getTeamId()).getName());
            }
            testProductItemSelVo.setTeamList(ItemTeamString);
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
        if (testProductItem.getStatus()!=null){
            queryWrapper.eq("status",testProductItem.getStatus());
        }
        if (testProductItem.getCheckItemPid()!=null){
            queryWrapper.eq("check_item_pid",testProductItem.getCheckItemPid());
        }else {
            queryWrapper.eq("check_item_pid",0);
        }
        queryWrapper.orderByDesc("create_time");
        List<TestProductItem> list=this.list(queryWrapper);
            for (TestProductItem productItem : list) {
                TestProductItemTreeVo treeVo=new TestProductItemTreeVo();
                BeanUtils.copyProperties(productItem,treeVo);
                TestProductItem testProductItem1=new TestProductItem();
                testProductItem1.setProductId(productItem.getProductId());
                testProductItem1.setCheckItemPid(productItem.getCheckItemId());
                if (testProductItem.getStatus()!=null){
                    testProductItem1.setStatus(testProductItem.getStatus());
                }
                treeVo.setChildren(this.getTreeList(testProductItem1));
                treeVos.add(treeVo);
            }
        return treeVos;
    }

    @Override
    public List<LabelValueVo> getProductTemplateSheet(Integer productId) throws Exception {
        List<LabelValueVo> result = Lists.newArrayList();
        String fileName = testProductItemDao.getProductFileName(productId);
        if(fileName == null){
            return result;
        }
        InputStream fileStream = MinIoUtil.getFileStream("report-original-template", fileName);
        XSSFWorkbook workbook = new XSSFWorkbook(fileStream);
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            String sheetName = workbook.getSheetAt(i).getSheetName();
            if("指标选择".contains(sheetName) || "技术指标".contains(sheetName) || "报告第1页".contains(sheetName) ||
                    "报告第2页".contains(sheetName)){
                continue;
            }
            LabelValueVo vo = new LabelValueVo(sheetName,Long.parseLong(i+""));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<Integer> getSheetIndex(Integer checkItemId) {
        return testProductItemDao.getSheetIndex(checkItemId);
    }
}

