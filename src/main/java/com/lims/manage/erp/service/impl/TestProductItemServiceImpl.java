package com.lims.manage.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.*;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.*;

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
    @Resource
    private TestOriginalRecordTemplateDao testOriginalRecordTemplateDao;
    @Resource
    private ItemOriginalRecordTemplateMapper itemOriginalRecordTemplateMapper;
    @Autowired
    private TestCompanyDao testCompanyDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addTestProductItem(TestProductItemParamVo testProductItemParamVo) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        if (testProductItemParamVo.getTestProductItem().getCheckItemPid() == null) {
            testProductItemParamVo.getTestProductItem().setCheckItemPid(0);
        }
        if (testProductItemParamVo.getTestProductItem().getCheckItemName() == null) {
            return ResultUtil.error("检测项目名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestProductItem>().eq("product_id", testProductItemParamVo.getTestProductItem().getProductId()).eq("del_flag", 0).eq("check_item_pid", testProductItemParamVo.getTestProductItem().getCheckItemPid()).eq("check_item_name", testProductItemParamVo.getTestProductItem().getCheckItemName())) != null) {
            return ResultUtil.error("同层检测项名称不能重复");
        }
        testProductItemParamVo.getTestProductItem().setDelFlag(0);
        testProductItemParamVo.getTestProductItem().setCreateTime(new Date());
        if (testProductItemParamVo.getTemplateSet() != null) {
            testProductItemParamVo.getTestProductItem().setReportModelId(testProductItemParamVo.getTemplateSet());
        }
        if (this.save(testProductItemParamVo.getTestProductItem())) {
            //设置检查项检测依据
            if (testProductItemParamVo.getStandardIds() != null && testProductItemParamVo.getStandardIds().size() > 0) {
                for (Integer StandardId : testProductItemParamVo.getStandardIds()) {
                    testProductItemStandardFileRelService.save(new TestProductItemStandardFileRel(testProductItemParamVo.getTestProductItem().getCheckItemId(), StandardId));
                }
            }
            //设置检查项检测设备
            if (testProductItemParamVo.getTypeIds() != null && testProductItemParamVo.getTypeIds().size() > 0) {
                for (Integer TypeId : testProductItemParamVo.getTypeIds()) {
                    testProductItemInstrumentTypeRelService.save(new TestProductItemInstrumentTypeRel(testProductItemParamVo.getTestProductItem().getCheckItemId(), TypeId));
                }
            }
/*            //设置检测项所属科室
            if (testProductItemParamVo.getItemIds()!=null&&testProductItemParamVo.getItemIds().size()>0){
                for (Integer itemId : testProductItemParamVo.getItemIds()) {
                    testCheckItemTeamRelService.save(new TestCheckItemTeamRel(testProductItemParamVo.getTestProductItem().getCheckItemId(),itemId,testProductItemParamVo.getTestProductItem().getProductId()));
                }
            }*/
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
            // 设置收费定价
            if (CollectionUtil.isNotEmpty(testProductItemParamVo.getMethodIds())) {
                for (TestProductItemMethodRel testProductItemMethodRel : testProductItemParamVo.getMethodIds()) {
                    testProductItemMethodRel.setCheckItemId(testProductItemParamVo.getTestProductItem().getCheckItemId());
                    if (CollectionUtil.isNotEmpty(testProductItemMethodRel.getStandardIds())) {
                        StringBuffer stringBuffer = new StringBuffer();
                        for (Integer standardId : testProductItemMethodRel.getStandardIds()) {
                            stringBuffer.append(standardId);
                            stringBuffer.append(",");
                        }
                        // 存储收费定价的依据信息id
                        testProductItemMethodRel.setStandardSet(stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString());
                        LambdaQueryWrapper<TestStandardFile> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.in(TestStandardFile::getId, testProductItemMethodRel.getStandardIds());
                        List<TestStandardFile> standardList = testStandardFileService.list(queryWrapper);
                        if (CollectionUtil.isNotEmpty(standardList)) {
                            StringBuffer standardFileBuffer = new StringBuffer();
                            for (TestStandardFile standardFile : standardList) {
                                standardFileBuffer.append(standardFile.getCode() + "《" + standardFile.getName() + "》");
                                standardFileBuffer.append(",");
                            }
                            // 存储收费定价的依据信息 code+standName
                            testProductItemMethodRel.setStandardName(standardFileBuffer.deleteCharAt(standardFileBuffer.length() - 1).toString());
                        }
                    }
                    testProductItemMethodRelService.save(testProductItemMethodRel);
                }
            }
            // 设置检测项对应线下原始记录
            if (CollectionUtil.isNotEmpty(testProductItemParamVo.getReportModelId())) {
                for (Integer templateId : testProductItemParamVo.getReportModelId()) {
                    TestItemOriginalRecordTemplateRel record = new TestItemOriginalRecordTemplateRel();
                    record.setOriginalRecordTemplateId(templateId);
                    record.setCheckItemId(testProductItemParamVo.getTestProductItem().getCheckItemId());
                    itemOriginalRecordTemplateMapper.insert(record);
                }
            }
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername() + "添加产品检测项" + testProductItemParamVo.getTestProductItem().getCheckItemId() + "成功!", Const.DETECTION_MANAGEMENT_LOG, true);
            return ResultUtil.success("添加成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加产品检测项失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("添加检查项失败，未知异常!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updTestProductItem(TestProductItemParamVo testProductItemParamVo) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        if (testProductItemParamVo.getTestProductItem().getCheckItemId() == null) {
            return ResultUtil.error("缺少修改对象");
        }
        if (testProductItemParamVo.getTestProductItem().getCheckItemName() == null) {
            return ResultUtil.error("检测项目名称不能为空");
        }
        if (this.getOne(new QueryWrapper<TestProductItem>().eq("check_item_pid", testProductItemParamVo.getTestProductItem().getCheckItemPid()).eq("product_id", testProductItemParamVo.getTestProductItem().getProductId()).ne("check_item_id", testProductItemParamVo.getTestProductItem().getCheckItemId()).eq("del_flag", 0).eq("check_item_name", testProductItemParamVo.getTestProductItem().getCheckItemName())) != null) {
            return ResultUtil.error("同层检测项名称不能重复");
        }
        testProductItemParamVo.getTestProductItem().setUpdateTime(new Date());
        if (testProductItemParamVo.getTemplateSet() != null) {
            testProductItemParamVo.getTestProductItem().setReportModelId(testProductItemParamVo.getTemplateSet());
        } else {
            testProductItemParamVo.getTestProductItem().setReportModelId(null);
        }
        if (this.updateById(testProductItemParamVo.getTestProductItem())) {
            //删除原有检测依据
            testProductItemStandardFileRelService.remove(new QueryWrapper<TestProductItemStandardFileRel>().eq("check_item_id", testProductItemParamVo.getTestProductItem().getCheckItemId()));
            //设置检查项检测依据
            if (testProductItemParamVo.getStandardIds() != null && testProductItemParamVo.getStandardIds().size() > 0) {
                for (Integer StandardId : testProductItemParamVo.getStandardIds()) {
                    testProductItemStandardFileRelService.save(new TestProductItemStandardFileRel(testProductItemParamVo.getTestProductItem().getCheckItemId(), StandardId));
                }
            }
            //删除原有检测设备
            testProductItemInstrumentTypeRelService.remove(new QueryWrapper<TestProductItemInstrumentTypeRel>().eq("check_item_id", testProductItemParamVo.getTestProductItem().getCheckItemId()));
            //设置检查项检测设备
            if (testProductItemParamVo.getTypeIds()!=null&&testProductItemParamVo.getTypeIds().size()>0){
                for (Integer TypeId : testProductItemParamVo.getTypeIds()) {
                    testProductItemInstrumentTypeRelService.save(new TestProductItemInstrumentTypeRel(testProductItemParamVo.getTestProductItem().getCheckItemId(),TypeId));
                }
            }
/*            //删除原有检测项所属科室
            testCheckItemTeamRelService.remove(new QueryWrapper<TestCheckItemTeamRel>().eq("check_item_id",testProductItemParamVo.getTestProductItem().getCheckItemId()));
            //设置检测项所属科室
            if (testProductItemParamVo.getItemIds()!=null&&testProductItemParamVo.getItemIds().size()>0){
                for (Integer itemId : testProductItemParamVo.getItemIds()) {
                    testCheckItemTeamRelService.save(new TestCheckItemTeamRel(testProductItemParamVo.getTestProductItem().getCheckItemId(),itemId,testProductItemParamVo.getTestProductItem().getProductId()));
                }
            }*/
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
            // 清除收费定价
            LambdaQueryWrapper<TestProductItemMethodRel> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(TestProductItemMethodRel::getCheckItemId, testProductItemParamVo.getTestProductItem().getCheckItemId());
            testProductItemMethodRelService.remove(lambdaQueryWrapper);
            // 设置收费定价
            if (CollectionUtil.isNotEmpty(testProductItemParamVo.getMethodIds())) {
                for (TestProductItemMethodRel testProductItemMethodRel : testProductItemParamVo.getMethodIds()) {
                    testProductItemMethodRel.setCheckItemId(testProductItemParamVo.getTestProductItem().getCheckItemId());
                    testProductItemMethodRelService.save(testProductItemMethodRel);
                }
            }
            // 清除检测项对应的线下原始记录
            LambdaQueryWrapper<TestItemOriginalRecordTemplateRel> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(TestItemOriginalRecordTemplateRel::getCheckItemId, testProductItemParamVo.getTestProductItem().getCheckItemId());
            itemOriginalRecordTemplateMapper.delete(recordWrapper);
            // 设置检测项对应线下原始记录
            if (CollectionUtil.isNotEmpty(testProductItemParamVo.getReportModelId())) {
                for (Integer templateId : testProductItemParamVo.getReportModelId()) {
                    TestItemOriginalRecordTemplateRel record = new TestItemOriginalRecordTemplateRel();
                    record.setOriginalRecordTemplateId(templateId);
                    record.setCheckItemId(testProductItemParamVo.getTestProductItem().getCheckItemId());
                    itemOriginalRecordTemplateMapper.insert(record);
                }
            }
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername() + "修改产品检测项" + testProductItemParamVo.getTestProductItem().getCheckItemId() + "成功!", Const.DETECTION_MANAGEMENT_LOG, true);
            return ResultUtil.success("修改成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改产品检测项"+testProductItemParamVo.getTestProductItem().getCheckItemId()+"失败!", Const.DETECTION_MANAGEMENT_LOG,false);
            return ResultUtil.error("修改检查项失败，未知异常!");
        }
    }

    @Override
    public Result delTestProductItemOlderVersion(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        TestProductItem testProductItem = getById(idList.get(0));
        testProductItem.setDelFlag(1);
        if (testProductItem.getIcon() != null) {
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
        } else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername() + "删除产品检测项" + idList.get(0) + "失败!", Const.DETECTION_MANAGEMENT_LOG, false);
            return ResultUtil.error("删除失败");
        }
    }

    /**
     * TODO：23年11月30日 进行检测项删除操作的重构
     * 1、异常操作时，进行事务回滚。
     * 2、直接删除、并且需要把绑定关系解除后进行删除。
     * 3、日志的记录信息
     *
     * @param idList
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result delTestProductItem(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return ResultUtil.error("token 已过期！");
        }
        // 查询检测项信息 与 检测项信息表 绑定关系
        Integer count = testProductItemDao.selectCheckitemNumberCount(idList);
        if (count > 0) {
            return ResultUtil.error("删除失败，检测项基础信息与业务信息参与绑定");
        }
        for (Long checkItemId : idList) {
            //删除原有检测依据
            testProductItemStandardFileRelService.remove(new QueryWrapper<TestProductItemStandardFileRel>().eq("check_item_id", checkItemId));
            //删除原有检测设备
            testProductItemInstrumentTypeRelService.remove(new QueryWrapper<TestProductItemInstrumentTypeRel>().eq("check_item_id", checkItemId));
            //删除检测项绑定的报告原始记录sheet
            testProductItemDao.deleteItemSheetRel(checkItemId.intValue());
            // 删除 检测项所属团队
//            testCheckItemTeamRelService.remove(new QueryWrapper<TestCheckItemTeamRel>().eq("check_item_id",checkItemId));
            // 清除收费定价
            LambdaQueryWrapper<TestProductItemMethodRel> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(TestProductItemMethodRel::getCheckItemId, checkItemId);
            testProductItemMethodRelService.remove(lambdaQueryWrapper);
            // 清除检测项对应的线下原始记录
            LambdaQueryWrapper<TestItemOriginalRecordTemplateRel> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(TestItemOriginalRecordTemplateRel::getCheckItemId, checkItemId);
            itemOriginalRecordTemplateMapper.delete(recordWrapper);
            // 删除检测项信息
            this.removeById(checkItemId);
        }
        return ResultUtil.error("删除成功");

    }

    @Override
    public Result disableStatusTestProductItem(List<Long> idList) {
        TestProductItem testProductItem = getById(idList.get(0));
        testProductItem.setStatus("1");
        if (this.updateById(testProductItem)) {
            if (this.disableStatus(idList.get(0))) {
                return ResultUtil.success("禁用成功");
            } else {
                return ResultUtil.error("禁用失败");
            }
        } else {
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
        //设置检查项方法
        List<TestProductItemMethodRel> productItemMethodRels = testProductItemMethodRelService.list(new QueryWrapper<TestProductItemMethodRel>().eq("check_item_id", testProductItem.getCheckItemId()));
        List<TestProductItemMethodRel> ItemMethodRelList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(productItemMethodRels)) {
            for (TestProductItemMethodRel testProductItemMethodRel : productItemMethodRels) {
                List<Integer> standardIds = new ArrayList<>();
                if (StringUtils.isNotEmpty(testProductItemMethodRel.getStandardSet())) {
                    String[] standardIdArray = testProductItemMethodRel.getStandardSet().split("\\,");
                    for (int i = 0; i < standardIdArray.length; i++) {
                        standardIds.add(Integer.valueOf(standardIdArray[i]));
                    }
                }
                testProductItemMethodRel.setStandardIds(standardIds);
                ItemMethodRelList.add(testProductItemMethodRel);
            }
        }
        testProductItemParamVo.setMethodIds(ItemMethodRelList);
        // 查看线下原始记录信息
        List<TestOriginalRecordTemplate> templateSet = itemOriginalRecordTemplateMapper.selectOriginalRecordList(testProductItem.getCheckItemId());
        // 补充信息
        List<LabelValueVo> templateSheet = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(templateSet)) {
            List<Integer> templateIntegerSet = new ArrayList<>();
            for (TestOriginalRecordTemplate template : templateSet) {
                LabelValueVo labelValueVo = new LabelValueVo();
                labelValueVo.setValue(template.getId().longValue());
                labelValueVo.setLabel(template.getCode() + "《" + template.getName() + "》");
                templateSheet.add(labelValueVo);
                templateIntegerSet.add(template.getId());
            }
            testProductItemParamVo.setReportModelId(templateIntegerSet);
            testProductItemParamVo.setTemplateSheet(templateSheet);
        } else {
            testProductItemParamVo.setReportModelId(new ArrayList<>());
            testProductItemParamVo.setTemplateSheet(new ArrayList<>());
        }
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
        //遍历检测项详情
        for (TestProductItem testProductItem : testProductItems) {
            TestProductItemSelVo testProductItemSelVo = new TestProductItemSelVo();
            //设置检查项基础信息
            testProductItemSelVo.setTestProductItem(testProductItem);
            //设置检测项模板
            if (testProductItem.getReportModelId() != null) {
                testProductItemSelVo.setReportName(testReportTemplateService.getById(testProductItem.getReportModelId()).getReportName());
            }
            //设置检查项方法
            List<TestProductItemMethodRel> testProductItemMethodRels = testProductItemMethodRelService.list(new QueryWrapper<TestProductItemMethodRel>().eq("check_item_id", testProductItem.getCheckItemId()));
            List<TestProductItemMethodRel> ItemMethodRelList = new ArrayList<>();
            if (CollectionUtil.isNotEmpty(testProductItemMethodRels)) {
                for (TestProductItemMethodRel testProductItemMethodRel : testProductItemMethodRels) {
                    ItemMethodRelList.add(testProductItemMethodRel);
                }
            }
            testProductItemSelVo.setMethodList(ItemMethodRelList);
            //设置检查项设备
            List<TestProductItemInstrumentTypeRel> testProductItemInstrumentTypeRels = testProductItemInstrumentTypeRelService.list(new QueryWrapper<TestProductItemInstrumentTypeRel>().eq("check_item_id", testProductItem.getCheckItemId()));
            List<String> ItemInstrumentTypeList = new ArrayList<>();
            if (CollectionUtil.isNotEmpty(testProductItemInstrumentTypeRels)) {
                for (TestProductItemInstrumentTypeRel testProductItemInstrumentTypeRel : testProductItemInstrumentTypeRels) {
                    ItemInstrumentTypeList.add(typeService.getById(testProductItemInstrumentTypeRel.getIntrusmentTypeId()).getName());
                }
            }
            testProductItemSelVo.setTypeList(ItemInstrumentTypeList);
            //设置检测项依据
            List<TestProductItemStandardFileRel> testProductStandardFileRels = testProductItemStandardFileRelService.list(new QueryWrapper<TestProductItemStandardFileRel>().eq("check_item_id", testProductItem.getCheckItemId()));
            List<String> StandarString = new ArrayList<>();
            if (CollectionUtil.isNotEmpty(testProductStandardFileRels)) {
                for (TestProductItemStandardFileRel testProductItemStandardFileRel : testProductStandardFileRels) {
                    StandarString.add(testStandardFileService.getById(testProductItemStandardFileRel.getStandardFileId()).getName());
                }
            }
            testProductItemSelVo.setItemStandardList(StandarString);
            // 查看线下原始记录信息
            List<TestOriginalRecordTemplate> templateSet = itemOriginalRecordTemplateMapper.selectOriginalRecordList(testProductItem.getCheckItemId());
            if (CollectionUtil.isNotEmpty(templateSet)) {
                for (TestOriginalRecordTemplate template : templateSet) {
                    template.setFileUrl(null);
                }
                testProductItemSelVo.setTemplateSet(templateSet);
            } else {
                testProductItemSelVo.setTemplateSet(new ArrayList<>());
            }
/*            //设置检测项所属科室
            List<TestCheckItemTeamRel> testCheckItemTeamRels = testCheckItemTeamRelService.list(new QueryWrapper<TestCheckItemTeamRel>().eq("check_item_id", testProductItem.getCheckItemId()));
            List<String> ItemTeamString = new ArrayList<>();
            if (CollectionUtil.isNotEmpty(testCheckItemTeamRels)) {
                for (TestCheckItemTeamRel testCheckItemTeamRel : testCheckItemTeamRels) {
                    ItemTeamString.add(testTeamService.getById(testCheckItemTeamRel.getTeamId()).getName());
                }
            }
            testProductItemSelVo.setTeamList(ItemTeamString);
 */
            //追加进Vo集合
            testProductItemSelVos.add(testProductItemSelVo);
        }
        return testProductItemSelVos;
    }

    @Override
    public List<TestProductItemTreeVo> getTreeList(TestProductItem testProductItem) {
        List<TestProductItemTreeVo> treeVos = new ArrayList<>();
        QueryWrapper<TestProductItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);
        if (testProductItem.getProductId() != null) {
            queryWrapper.eq("product_id", testProductItem.getProductId());
        }
        // 0,正常，1,冻结
        if (testProductItem.getStatus() != null) {
            queryWrapper.eq("status", testProductItem.getStatus());
        }
        // 检测项名称
        if (testProductItem.getCheckItemName() != null) {
            queryWrapper.eq("check_item_name", testProductItem.getCheckItemName());
        }
        if (testProductItem.getCheckItemPid() != null) {
            queryWrapper.eq("check_item_pid", testProductItem.getCheckItemPid());
        } else {
            queryWrapper.eq("check_item_pid", 0);
        }
        queryWrapper.orderByDesc("create_time");
        List<TestProductItem> list = this.list(queryWrapper);
        for (TestProductItem productItem : list) {
            TestProductItemTreeVo treeVo = new TestProductItemTreeVo();
            BeanUtils.copyProperties(productItem, treeVo);
            TestProductItem testProductItem1 = new TestProductItem();
            testProductItem1.setProductId(productItem.getProductId());
            testProductItem1.setCheckItemPid(productItem.getCheckItemId());
            if (testProductItem.getStatus() != null) {
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
            sheetName = sheetName.replaceAll(" ", "");
            if("指标选择".contains(sheetName) || "技术指标".contains(sheetName) || "报告第1页".contains(sheetName) ||
                    "报告第2页".contains(sheetName)){
                continue;
            }
            LabelValueVo vo = new LabelValueVo(sheetName, Long.parseLong(i + ""));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<Integer> getSheetIndex(Integer checkItemId) {
        return testProductItemDao.getSheetIndex(checkItemId);
    }

    @Override
    public Map<String, List<LabelValueVo>> returnEntrustData() {

        Map<String, List<LabelValueVo>> map = new HashMap<>();
        PageHelper.clearPage();
        List<TestInitDataEntity> ReturnBasisData = testCompanyDao.selectEntrustBasis();
        // 6：检测项签章类型：
        List<LabelValueVo> arrySeal = new ArrayList<>();
        for (TestInitDataEntity testInitDataEntity : ReturnBasisData) {
            LabelValueVo labelValueVo = new LabelValueVo();
            labelValueVo.setLabel(testInitDataEntity.getName());
            labelValueVo.setValue(Long.valueOf(testInitDataEntity.getId()));
            switch (testInitDataEntity.getType()) {
                case 16:
                    arrySeal.add(labelValueVo);
                    break;
                default:
                    break;
            }
        }
        map.put("arrySeal", arrySeal);
        return map;
    }

    @Override
    public void updateHourById(Integer id, Integer hour) {
        testProductItemDao.updateHourById(id,hour);
    }
}

