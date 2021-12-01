package com.lims.manage.erp.service.impl;

import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.*;
import com.lims.manage.erp.entity.EntrustEntity;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.vo.*;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.vo.CheckItemDetailVo;
import com.lims.manage.erp.vo.CheckItemInfoVo;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.LabelValueVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

import java.util.List;

@Service
public class EntrustServiceImpl implements EntrustService {

    @Autowired
    private EntrustEntityMapper entityMapper;
    @Autowired
    TestCompanyDao testCompanyDao;
    @Autowired
    TestCustomerDao testCustomerDao;
    @Autowired
    SampleEntityMapper sampleEntityMapper;
    @Autowired
    TestProductDao testProductDao;

    @Autowired
    private ProductItemEntityMapper itemEntityMapper;
    /**
     * 新增委托任务
     * @param vo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addEntrust(EntrustAddVo vo) {
        //存放委托基本信息==》test_entrusted
        EntrustEntity basisInfo = new EntrustEntity(vo);
        basisInfo.setId(GenID.getID());
        //设置委托编号
        Integer code = null;
        String currentTime = DateUtil.getTodayString().substring(0,6);
        //获取当前最大样品编号
        Integer entrustNum = entityMapper.selectMaxNo();
        if (entrustNum>0){
            String substring = entrustNum.toString().substring(0, 6);
            if (substring.equals(currentTime)){
                code = entrustNum+1;
            }else {
                code = Integer.parseInt(currentTime+"0001");
            }
        }else {
            code = Integer.parseInt(currentTime+"0001");
        }
        basisInfo.setEntrustmentNo(code);
        //附件存在上传附件到服务器
        if (vo.getFile() != null){
            String upload = MinIoUtil.upload(BucketsConst.buckets_entrust_enclosure, vo.getFile(), "file:" + code);
            basisInfo.setFileUrl(upload);
        }
        entityMapper.insert(basisInfo);
        //存放委托单样品信息==》test_entrusted_sample_details_rel，上传附件

        //存在委托单样品下检测项信息==》test_entrusted_sample_checkitem_rel，上传附件

        //存放委托单样品，使用依据信息test_entrusted_sample_standard_file_rel

        //更新委托单收费记录信息


        return true;
    }

    @Override
    public List<CheckItemDetailVo> getAllItemByProductId(Integer productId) {
        return itemEntityMapper.getAllItemByProductId(productId);
    }

    @Override
    public List<CheckItemInfoVo> getCheckItemInfoVo(List<Integer> ids) {
        return itemEntityMapper.getItemInfo(ids);
    }


    @Override
    public  Map<String,List<LabelValueVo>> returnEntrustData() {

        Map<String,List<LabelValueVo>> map = new HashMap<>();
        // type =1 委托单位
       List<LabelValueVo> EntrustCompany = testCompanyDao.selectEntrustCompanyList(1);
        // type =2 见证单位
        List<LabelValueVo> witnessCompany = testCompanyDao.selectEntrustCompanyList(2);
        List<TestInitDataEntity> ReturnBasisData  = testCompanyDao.selectEntrustBasis();
        // 团队信息
        List<LabelValueVo> arryTeam = testCompanyDao.selectTestTeam();
//        1 = 委托方式： 2=取样方式 ： 3=检测目的：4 ： 取报告方式 5：样品外观  6：签章类型： 7：用户来源： 8：设备类型  9：支付方式
        // 委托方式 type =1
        List<LabelValueVo> arryEntrust = new ArrayList<>();
        // 取样方式 type =2
        List<LabelValueVo> arrySampling = new ArrayList<>();
        // 3=检测目的
        List<LabelValueVo> arryCheckout = new ArrayList<>();
        // 4=取报告方式
        List<LabelValueVo> arryGetReport = new ArrayList<>();
        // 5：样品外观
        List<LabelValueVo> arrySampleAppearance = new ArrayList<>();
        // 6：签章类型：
        List<LabelValueVo> arrySeal = new ArrayList<>();
        // 8：设备类型
        List<LabelValueVo> arryEquipment = new ArrayList<>();
        // 11：支付方式
        List<LabelValueVo> arryPayment = new ArrayList<>();
        for(TestInitDataEntity testInitDataEntity:ReturnBasisData){
            LabelValueVo labelValueVo = new LabelValueVo();
            labelValueVo.setLabel(testInitDataEntity.getName());
            labelValueVo.setValue(Long.valueOf(testInitDataEntity.getId()));
            switch (testInitDataEntity.getType()) {
                case 1:  arryEntrust.add(labelValueVo); break;
                case 2:   arrySampling.add(labelValueVo); break;
                case 3:   arryCheckout.add(labelValueVo); break;
                case 4:   arryGetReport.add(labelValueVo); break;
                case 5:   arrySampleAppearance.add(labelValueVo); break;
                case 6:   arrySeal.add(labelValueVo); break;
                case 11:   arryPayment.add(labelValueVo); break;
                default:break;
            }
        }
        map.put("entrustCompany",EntrustCompany);
        map.put("witnessCompany",witnessCompany);
        map.put("arryEntrust",arryEntrust);
        map.put("arrySampling",arrySampling);
        map.put("arryCheckout",arryCheckout);
        map.put("arryGetReport",arryGetReport);
        map.put("arrySampleAppearance",arrySampleAppearance);
        map.put("arrySeal",arrySeal);
        map.put("arryTeam",arryTeam);
        map.put("arryPayment",arryPayment);
        return map;
    }

    @Override
    public List<TestCustomerJsonEntity> returnTestCustomerEntityList(Integer companyId) {

        return testCompanyDao.selectPeopleInformation(companyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addCompanyData(TestCompanyJsonEntity testCompanyEntity) {
        TestCompanyEntity testCompanyEntity1 = new TestCompanyEntity();
        testCompanyEntity1.setCompanyName(testCompanyEntity.getCompanyName());
        testCompanyEntity1.setType(testCompanyEntity.getType());
        int statusNumber = testCompanyDao.insert(testCompanyEntity1);
        if(statusNumber>=1){
            TestCustomerEntity testCustomerEntity = new TestCustomerEntity();
            testCustomerEntity.setCompanyId(testCompanyEntity1.getCompanyId());
            testCustomerEntity.setContacts(testCompanyEntity.getContacts());
            testCustomerEntity.setPhone(testCompanyEntity.getContactWay());
           int addCustomer = testCustomerDao.insertTestCustomer(testCustomerEntity);
           if(addCustomer>=1){
               return true;
           }
            return false;
        }
        return false;
    }

    @Override
    public List<SampleEntity> getSampleDataList(SampleEntity sampleEntity) {

        return sampleEntityMapper.selectSampleList(sampleEntity);
    }

    @Override
    public List<LabelValueVo> selectProductList(String productName) {

        return testProductDao.selectProductList(productName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer addSampleData(SampleAddParamVo addParamVo) {
        int result = 0;
        //查询产品名称
        String productName = testProductDao.getProductNameById(addParamVo.getSampleName());
        //处理详情数据
        List<SampleAddDetailVo> details = addParamVo.getDetails();
        //获取数据库当前年份最大的样品编号
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        Date now = new Date();
        String maxNumber = testProductDao.getMaxNumber(sdf.format(now));
        int newMax;
        if(maxNumber == null){
            newMax = 0;
        }else{
            newMax = Integer.parseInt(maxNumber);
        }
        if(details.size()>0){
            for (int i = 0; i < details.size(); i++) {
                //生成样品编号
                StringBuilder code = new StringBuilder("YP-"+sdf.format(now)+"-");
                String sampleCode ;
                if(addParamVo.getSampleGroups()>1){
                    //组数大于1，将前n个命名为
                    if(i%addParamVo.getQuantityPerGroup() == 0){
                        newMax = newMax+1;
                    }
                    String num = String.format("%0" + 4 + "d", newMax);
                    StringBuilder prefix = code.append(num);
                    String suffix = String.format("%0" + 2 + "d", i%addParamVo.getQuantityPerGroup()+1);
                    sampleCode = prefix + "_" + suffix;
                }else{
                    //生成样品编号
                    String num = String.format("%0" + 4 + "d", newMax+1);
                    StringBuilder prefix = code.append(num);
                    String suffix = String.format("%0" + 2 + "d", i+1);
                    if(addParamVo.getQuantityPerGroup()>1){
                        sampleCode = prefix + "_" + suffix;
                    }else{
                        sampleCode = prefix.toString();
                    }
                }
                //保存样品图片
//                String picture = details.get(i).getPicture();
                //保存样品信息
                SampleEntity sampleEntity = new SampleEntity(addParamVo,details.get(i),productName,sampleCode,null);
                result = sampleEntityMapper.insert(sampleEntity);

            }
        }
        return result;
    }
}
