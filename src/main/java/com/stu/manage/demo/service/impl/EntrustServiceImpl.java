package com.stu.manage.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Maps;
import com.stu.manage.demo.entity.*;
import com.stu.manage.demo.mapper.*;
import com.stu.manage.demo.service.EntrustService;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class EntrustServiceImpl implements EntrustService {
    @Autowired
    private EntrustMapper entrustMapper;
    @Autowired
    private JtEntrustInfoMapper jtEntrustInfoMapper;
    @Autowired
    private JtEntrustCheckInfoMapper jtEntrustCheckInfoMapper;
    @Autowired
    private JtSampleObjectMapper jtSampleObjectMapper;
    @Autowired
    private JtEntrustCheckItemMapper jtEntrustCheckItemMapper;
    @Autowired
    private JtSampleInfoMapper jtSampleInfoMapper;
    @Autowired
    private JtEntrustProductMapper jtEntrustProductMapper;
    @Autowired
    private JtReportInfoMapper jtReportInfoMapper;

    @Override
    public EntrustInfo onceMore(int entrustId) {
        EntrustInfo entrustInfo = entrustMapper.onceMore(entrustId);
        List<SampleInfoVo> sampleInfo = entrustMapper.getSampleInfo(entrustId);
        List<CheckItemInfoVo> checkItemInfo = entrustMapper.getCheckItemInfo(entrustId);

        for (int i = 0; i < sampleInfo.size(); i++) {
            SampleInfoVo sampleInfoVo = sampleInfo.get(i);
            List<CheckItemInfoVo> items = Lists.newArrayList();
            for (int j = 0; j < checkItemInfo.size(); j++) {
                CheckItemInfoVo checkItemInfoVo = checkItemInfo.get(j);
                if(sampleInfoVo.getProductId() == checkItemInfoVo.getProductId()){
                    items.add(checkItemInfoVo);
                }
            }
            sampleInfoVo.setItems(items);
        }

        EntrustInfo result = new EntrustInfo(entrustInfo,sampleInfo);

        return result;
    }

    @Override
    public List<CheckItemCostVo> getCheckItemsByProductId(int productId) {
        return entrustMapper.getCheckItemsByProductId(productId);
    }

    @Override
    public List<ProductVo> getCheckBasisByProductId(int productId) {
        return entrustMapper.getCheckBasisByProductId(productId);
    }

    @Override
    public StatusEntity status(Integer id) {
        StatusEntity statusEntity = new StatusEntity();
        //获取委托单状态
        LambdaQueryWrapper<EntrustStat> wrapper = new LambdaQueryWrapper();
        wrapper.eq(EntrustStat::getId, id);
        EntrustStat stat = entrustMapper.selectOne(wrapper);
        if (stat != null){
            statusEntity.setStatus(stat.getStatus());
        }
        //获取委托单下，样品检测状态
        List<SampleStatus> list = entrustMapper.getSampleStat(id);
        //获取任务状态和任务流程审批状态
        List<SampleStatus> ll = entrustMapper.getTaskStat(id);
        if (!CollectionUtils.isEmpty(ll) && !CollectionUtils.isEmpty(list)){
            for (SampleStatus status :ll) {
                for (SampleStatus sampleStatus:list) {
                    if (sampleStatus.getSampleId()==status.getSampleId()){
                        status.setReportStat(sampleStatus.getReportStat());
                    }
                }
            }
            statusEntity.setList(ll);
        }
        return statusEntity;
    }

    @Override
    public Integer addEntrust(JtEntrustInfo jtEntrustInfo) {
        JtEntrustInfo jtEntrustInfos = new JtEntrustInfo();
        jtEntrustInfos.setEntrustNumber("2021090023");// (getEntrustNumber() 委托号
        // 委托单基本信息
        // TODO: 2021/6/15 委托单状态 0 未分任务 1分完任务
        jtEntrustInfos.setEntrustStatus(0);
//        jtEntrustInfoMapper.insertSelective(jtEntrustInfo);
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public synchronized Integer addEntrustInfo(Map<String, Object> map) {

        Object entrustInfo = map.get("entrustInfo");
        Object sampleList = map.get("sampleList");
        Object entrustCheckInfo = map.get("entrustCheckInfo");
        JtEntrustInfo jtEntrustInfo= JSONObject.parseObject(JSON.toJSONString(entrustInfo),JtEntrustInfo.class);
        jtEntrustInfo.setEntrustNumber(getEntrustNumber());//getEntrustNumber()
        // TODO: 2021/6/15 委托单状态 0 未分任务 1分完任务
        jtEntrustInfo.setEntrustStatus(0);
        // 1、得到公司名称 进行查询 增加id 2、没有公司名称 新增进行 id 补充
        Company company = jtEntrustInfoMapper.selectCompanyName(jtEntrustInfo.getComName());
        if(company.getCount()!=0)
        {
            jtEntrustInfo.setEntrustCompanyId(company.getComId());
        }
        else {

            company.setComName(jtEntrustInfo.getComName());
            company.setComContactPerson(jtEntrustInfo.getEntrustPeople());
            company.setComContactPhone(jtEntrustInfo.getEntrustPeoplePhone());
            jtEntrustInfoMapper.insertCompany(company);
            jtEntrustInfo.setEntrustCompanyId(company.getComId());
        }
        // 客户委托信息 代码补充基本信息
        jtEntrustInfo.setContactAddress(company.getComAddress());
        jtEntrustInfo.setContactPeople(company.getComContactPerson());
        jtEntrustInfo.setContactTel(company.getComContactPhone());
        //待完善
        jtEntrustInfo.setProductStandardSource("GB待完善");
        jtEntrustInfo.setReportGetCompany("——");
        jtEntrustInfo.setContactAddress("——");
        jtEntrustInfoMapper.insertSelective(jtEntrustInfo);
        System.out.println("存储委托基本信息"+jtEntrustInfo);
        // 后台补充委托基本价格信息
        JtEntrustCheckInfo jtEntrustCheckInfo = new JtEntrustCheckInfo();
        if(jtEntrustInfo.getEntrustId()!=null)
        {
            jtEntrustCheckInfo.setEntrustId(jtEntrustInfo.getEntrustId());
            jtEntrustCheckInfo.setSampleStatusDesc("待补充");
            jtEntrustCheckInfo.setSampleStay(1);
            jtEntrustCheckInfo.setCost("0");
            jtEntrustCheckInfo.setPayMode(2);
            jtEntrustCheckInfo.setPay("0");
            jtEntrustCheckInfo.setAcceptUserId(Integer.parseInt(jtEntrustInfo.getAcceptUserId()));//受理人默认为 程萍 160；
            jtEntrustCheckInfo.setSource(1);
            jtEntrustCheckInfo.setAcceptTime(new Date());
            jtEntrustCheckInfo.setEndPlanDate(new Date());
            jtEntrustCheckInfo.setStandardCost("0");
            jtEntrustCheckInfoMapper.insertSelective(jtEntrustCheckInfo);//任务来源

        }
        System.out.println("存储委托价格信息默认值"+jtEntrustCheckInfo);
        // 保存委托和产品关系
        JtEntrustProduct jtEntrustProduct = new JtEntrustProduct();
        jtEntrustProduct.setEntrustId(jtEntrustCheckInfo.getEntrustId());
//        jtEntrustProduct.setProductId(jtSampleObject.getProductId());
        jtEntrustProduct.setProductId(2568);
        jtEntrustProductMapper.insertSelective(jtEntrustProduct);
        // 增加样品基本信息 有可能是多个。
        if(sampleList!=null) {
            List<JtSampleObject> jtSampleObjects=JSON.parseArray(JSON.toJSONString(sampleList),JtSampleObject.class);
//            System.out.println("=================");
//            List<JtSampleObject> jtSampleObjects = (List<JtSampleObject>) sampleList.get("sampleList");
//            System.out.println("====="+jtSampleObjects.size());
//            List<JtSampleObject> jtSampleObjects = JSONObject.parseArray(sampleList.toString(), JtSampleObject.class);
//            JtSampleObject jtSampleObject = JSONObject.parseObject(sampleList.toString(), JtSampleObject.class);
//            System.out.println(jtSampleObject.toString());
            //-------------------------------------------------
            for (JtSampleObject jtSampleObject:jtSampleObjects){
                System.out.println("展示数据===="+jtSampleObject);
                // 后台补充业务id
                jtSampleObject.setReceiveUserId(Integer.parseInt(jtEntrustInfo.getAcceptUserId()));//受理人默认为 程萍 160；
                jtSampleObject.setReceiveTime(new Date());

                //后台补充
                jtSampleObject.setStoragePlace("——");
                jtSampleObject.setSampleObjectNote("——");
                jtSampleObject.setSampleBatch("——");
                jtSampleObjectMapper.insertSelective(jtSampleObject);
                //解析 产品下的 数据
                if(null!=jtSampleObject.getCheckList()){
                    for (JtEntrustCheckItem jtEntrustCheckItem:jtSampleObject.getCheckList()) {
                        System.out.println("解析 产品下的 检测项数据 "+jtEntrustCheckItem);
                        jtEntrustCheckItem.setProductId(jtSampleObject.getProductId());
                        jtEntrustCheckItem.setSampleId(jtSampleObject.getSampleObjectId());
                        jtEntrustCheckItem.setEntrustId(jtEntrustInfo.getEntrustId());
                        jtEntrustCheckItemMapper.insertSelective(jtEntrustCheckItem);
                    }
                }

                // 设置一个样品下存放的关系
                JtSampleInfo jtSampleInfo = new JtSampleInfo();
                jtSampleInfo.setSampleId(jtSampleObject.getSampleObjectId());
                jtSampleInfo.setEntrustId(jtEntrustInfo.getEntrustId());
                jtSampleInfo.setProductId(jtSampleObject.getProductId());
//                jtSampleInfo.setSampleNumber(createSampleNumber());// 样品编码补充
                jtSampleInfo.setSampleNumber("YP-2021-0119");// 样品编码补充

                jtSampleInfo.setSampleType(jtSampleObject.getSampleType());
                jtSampleInfo.setEntrustCompanyId(jtEntrustInfo.getEntrustCompanyId());
                jtSampleInfo.setSampleStatus("——");//补充信息
                jtSampleInfo.setSampleObjectId(jtSampleObject.getSampleObjectId());
                jtSampleInfo.setLifecycle(3);
                jtSampleInfoMapper.insertSelective(jtSampleInfo);

                //补充样品id信息信息：
                JtReportInfo jtReportInfo = new JtReportInfo();
                jtReportInfo.setFlowStatus(4);
                jtReportInfo.setSampleId(jtSampleObject.getSampleObjectId());

                jtReportInfoMapper.insertSelective(jtReportInfo);

            }

        }
        return 1;
    }

    @Override
    public Map<String, List<ProductVo>> getSelectLists() {
        Map<String,List<ProductVo>> result = Maps.newHashMap();
        result.put("entrustWay",jtEntrustInfoMapper.getEntrustTheWay());
        result.put("checkPurpose",jtEntrustInfoMapper.getcheckPurpose());
        result.put("receiveWay",jtEntrustInfoMapper.getreceiveWay());
        result.put("reportGetWay",jtEntrustInfoMapper.getreportGetWay());
        return result;
    }

    /**
     * 自动生产委托编号
     */
    private String getEntrustNumber(){
        StringBuilder stringBuffer=new StringBuilder();
        Calendar calendar=Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        stringBuffer.append(year).append(month>9?month:"0"+month);
        int count=jtEntrustInfoMapper.selectEntrustNumberForCount(stringBuffer.toString());
        stringBuffer.append(String.format("%04d",count+1));
        return stringBuffer.toString();
    }
    /**
     * 自动生成样品编号
     */
    public  String createSampleNumber(){
        StringBuilder stringBuffer=new StringBuilder();
        Calendar calendar=Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        stringBuffer.append("YP-").append(year).append("-");
        JtSampleInfo jtSampleInfo=jtEntrustInfoMapper.selectSampleNumberForCount(stringBuffer.toString());
        if(jtSampleInfo==null){
            stringBuffer.append("0001");
        }else{
            String number=jtSampleInfo.getSampleNumber().substring(jtSampleInfo.getSampleNumber().length()-4);
            int integer=Integer.parseInt(number);
            stringBuffer.append(String.format("%04d",integer+1));
        }
        return stringBuffer.toString();
    }
}
