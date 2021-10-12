package com.stu.manage.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.stu.manage.demo.entity.CheckItemCostVo;
import com.stu.manage.demo.entity.CheckItemInfoVo;
import com.stu.manage.demo.entity.Company;
import com.stu.manage.demo.entity.EntrustBaseInfo;
import com.stu.manage.demo.entity.EntrustStat;
import com.stu.manage.demo.entity.FileHomeLocation;
import com.stu.manage.demo.entity.FileInfo;
import com.stu.manage.demo.entity.FileVersionInfo;
import com.stu.manage.demo.entity.JtEntrustCheckInfo;
import com.stu.manage.demo.entity.JtEntrustCheckItem;
import com.stu.manage.demo.entity.JtEntrustInfo;
import com.stu.manage.demo.entity.JtEntrustProduct;
import com.stu.manage.demo.entity.JtReportInfo;
import com.stu.manage.demo.entity.JtSampleInfo;
import com.stu.manage.demo.entity.JtSampleObject;
import com.stu.manage.demo.entity.ProductVo;
import com.stu.manage.demo.entity.SampleInfoVo;
import com.stu.manage.demo.entity.SampleStatus;
import com.stu.manage.demo.entity.StatusEntity;
import com.stu.manage.demo.http.HttpClientUtil;
import com.stu.manage.demo.mapper.EntrustMapper;
import com.stu.manage.demo.mapper.JtEntrustCheckInfoMapper;
import com.stu.manage.demo.mapper.JtEntrustCheckItemMapper;
import com.stu.manage.demo.mapper.JtEntrustInfoMapper;
import com.stu.manage.demo.mapper.JtEntrustProductMapper;
import com.stu.manage.demo.mapper.JtReportInfoMapper;
import com.stu.manage.demo.mapper.JtSampleInfoMapper;
import com.stu.manage.demo.mapper.JtSampleObjectMapper;
import com.stu.manage.demo.service.EntrustService;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
public class EntrustServiceImpl implements EntrustService {
    Logger logger = LoggerFactory.getLogger(EntrustServiceImpl.class);

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
    public EntrustBaseInfo onceMore(String entrustNumber) {
        EntrustBaseInfo entrustInfo = entrustMapper.onceMore(entrustNumber);
        List<SampleInfoVo> sampleInfo = entrustMapper.getSampleInfo(entrustNumber);
        List<CheckItemInfoVo> checkItemInfo = entrustMapper.getCheckItemInfo(entrustNumber);

        for (int i = 0; i < sampleInfo.size(); i++) {
            SampleInfoVo sampleInfoVo = sampleInfo.get(i);
            List<CheckItemInfoVo> items = Lists.newArrayList();
            for (int j = 0; j < checkItemInfo.size(); j++) {
                CheckItemInfoVo checkItemInfoVo = checkItemInfo.get(j);
                if(sampleInfoVo.getProductId() == checkItemInfoVo.getProductId()){
                    items.add(checkItemInfoVo);
                }
            }
            sampleInfoVo.setCheckList(items);
        }

        EntrustBaseInfo result = new EntrustBaseInfo(entrustInfo,sampleInfo);

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
        if (!CollectionUtils.isEmpty(list) && CollectionUtils.isEmpty(ll)){
            statusEntity.setList(list);
        }
        if (!CollectionUtils.isEmpty(ll) && !CollectionUtils.isEmpty(list)){
            for (SampleStatus status :ll) {
                for (SampleStatus sampleStatus:list) {
                    if (sampleStatus.getSampleId()==status.getSampleId()){
                        status.setReportStat(sampleStatus.getReportStat());
                        status.setSampleId(sampleStatus.getSampleId());
                        status.setSampleCode(sampleStatus.getSampleCode());
                        status.setSampleName(sampleStatus.getSampleName());
                    }
                }
            }
            statusEntity.setList(ll);
        }
        return statusEntity;
    }

    @Override
    public List<String> sendMessage() {
        //TODO 后续推送模块有了替换此处
        String url = "http://qxsdcloud.com:18083/data/juheApi";
        Map<String,String> map = new HashMap<>();
        Map<String,String> headerMap = new HashMap<>();
        map.put("key","8299e5e44e3d47e58eda9a65f4abecb2");
        map.put("userKey","sfx");
        map.put("querys","{'type':'top'}");

        headerMap.put("Content-Type","application/json;charset=UTF-8");
        Pair<Integer, String> postJson = HttpClientUtil.postJson(url, JSON.toJSONString(map), headerMap);
        Integer left = postJson.getLeft();
        String right = postJson.getRight();
        List<String> list = new ArrayList<>();
        if (left == 200){
            JSONObject jsonObject = JSON.parseObject(right);
            JSONObject result = (JSONObject) jsonObject.get("result");
            List<JSONObject> objectList = (List<JSONObject>) result.get("data");
            objectList.forEach(data -> {
                list.add(data.get("title").toString());
            });
        }
        return list;
    }

    @Override
    public PageInfo ownerTask(String type, Integer userId, String adminId, Long startTime, Long endTime, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<StatusEntity> list = entrustMapper.ownerTask(type, userId, adminId, new Date(startTime), new Date(endTime));
        logger.info("查询的数据:{}",JSON.toJSONString(list));
        PageInfo pageInfo = new PageInfo<>(list);
        for (StatusEntity entity:list) {
            StatusEntity status = this.status(Integer.parseInt(entity.getEntrustId()));
            if (status != null){
                entity.setList(status.getList());
            }
        }
        return pageInfo;
    }

    @Override
    public Map<String, String> count(String adminId) {
        Map<String,String> map = new HashMap();
        String currentEntrustNum = entrustMapper.countCurrent(adminId);
        String historyEntrustNum = entrustMapper.countHistory(adminId);
        map.put("historyEntrustNum",currentEntrustNum);
        map.put("currentEntrustNum",historyEntrustNum);
        return map;
    }

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
    public synchronized String addEntrustInfo(Map<String, Object> map) {
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
        // 客户委托信息 代码补充基本信息 判断是否为邮寄 则进行补充
        if(jtEntrustInfo.getReportGetWay().equals("邮寄")==false){
            jtEntrustInfo.setContactAddress("——");
            jtEntrustInfo.setContactPeople("——");
            jtEntrustInfo.setContactTel("——");
            jtEntrustInfo.setReportGetCompany("——");
        }
        // 客户来源
        //完善 产品标准
        if(sampleList!=null) {
            List<JtSampleObject> jtSampleObjects = JSON.parseArray(JSON.toJSONString(sampleList), JtSampleObject.class);
            List<String> strlist = new ArrayList<>();
            for (JtSampleObject jtSampleObject:jtSampleObjects) {
                strlist.add(jtSampleObject.getProductStandardSource());
            }
            jtEntrustInfo.setProductStandardSource(returnStr(strlist));
        }
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
            //受理人默认为 程萍 160；
            jtEntrustCheckInfo.setAcceptUserId(Integer.parseInt(jtEntrustInfo.getAcceptUserId()));
            jtEntrustCheckInfo.setSource(1);
            jtEntrustCheckInfo.setAcceptTime(new Date());
            jtEntrustCheckInfo.setEndPlanDate(new Date());
            jtEntrustCheckInfo.setStandardCost("0");
            //任务来源
            jtEntrustCheckInfoMapper.insertSelective(jtEntrustCheckInfo);
        }
        System.out.println("存储委托价格信息默认值"+jtEntrustCheckInfo);
        // 增加样品基本信息 有可能是多个。
        if(sampleList!=null) {
            List<JtSampleObject> jtSampleObjects=JSON.parseArray(JSON.toJSONString(sampleList),JtSampleObject.class);

            for (JtSampleObject jtSampleObject:jtSampleObjects){

                // 保存委托和产品关系
                JtEntrustProduct jtEntrustProduct = new JtEntrustProduct();
                jtEntrustProduct.setEntrustId(jtEntrustCheckInfo.getEntrustId());
                jtEntrustProduct.setProductId(jtSampleObject.getProductId());
                jtEntrustProductMapper.insertSelective(jtEntrustProduct);

                System.out.println("展示数据===="+jtSampleObject);
                // 后台补充业务id1
                jtSampleObject.setReceiveUserId(Integer.parseInt(jtEntrustInfo.getAcceptUserId()));//受理人默认为 程萍 160；
                jtSampleObject.setReceiveTime(new Date());

                //后台补充
                jtSampleObject.setStoragePlace("0");
                jtSampleObject.setSampleObjectNote("0");
                jtSampleObject.setSampleBatch("0");
                jtSampleObjectMapper.insertSelective(jtSampleObject);

                // 设置一个样品下存放的关系
                JtSampleInfo jtSampleInfo = new JtSampleInfo();
//                jtSampleInfo.setSampleId(jtSampleObject.getSampleObjectId());
                jtSampleInfo.setEntrustId(jtEntrustInfo.getEntrustId());
                jtSampleInfo.setProductId(jtSampleObject.getProductId());
//                jtSampleInfo.setSampleNumber(createSampleNumber());// 样品编码补充
                jtSampleInfo.setSampleNumber(createSampleNumber());// 样品编码补充
                jtSampleInfo.setSampleType(jtSampleObject.getSampleType());
                jtSampleInfo.setEntrustCompanyId(jtEntrustInfo.getEntrustCompanyId());
                jtSampleInfo.setSampleStatus("——");//补充信息
                jtSampleInfo.setSampleObjectId(jtSampleObject.getSampleObjectId());
                jtSampleInfo.setLifecycle(3);
                // 根据产品id 得到 产品名
                JtSampleInfo entityProductName = jtSampleInfoMapper.getProductOtherName(jtSampleObject.getProductId());
                jtSampleInfo.setProductOtherName(entityProductName.getProductOtherName());
                jtSampleInfoMapper.insertSelective(jtSampleInfo);
                //解析 产品下的 检测项数据
                if(null!=jtSampleObject.getCheckList()){
                    List<JtEntrustCheckItem> GetEntrustCheckItemList = new ArrayList<>();
                    for (JtEntrustCheckItem jtEntrustCheckItem:jtSampleObject.getCheckList()) {
                        System.out.println("解析 产品下的 检测项数据 "+jtEntrustCheckItem);
                        jtEntrustCheckItem.setProductId(jtSampleObject.getProductId());
//                        样品信息
                        jtEntrustCheckItem.setSampleId(jtSampleInfo.getSampleId());
                        jtEntrustCheckItem.setEntrustId(jtEntrustInfo.getEntrustId());
                        // 根据检测项id 获取价格信息 进行补充；
                        JtEntrustCheckItem dataCost = jtEntrustCheckItemMapper.selectCheckItem(jtEntrustCheckItem.getCheckItemId());
                        if(dataCost!=null)
                        {
                            jtEntrustCheckItem.setProceedsPrice(String.valueOf(dataCost.getCost()));
                            jtEntrustCheckItem.setTotalProceedsPrice(String.valueOf(dataCost.getCost()));
                            jtEntrustCheckItem.setReceivablePrice(String.valueOf(dataCost.getCost()));
                            jtEntrustCheckItem.setTotalReceivablePrice(String.valueOf(dataCost.getCost()));
                            jtEntrustCheckItem.setCheckItemOtherName(dataCost.getCheckItemOtherName());
                        }
                        else
                        {
                            jtEntrustCheckItem.setProceedsPrice(String.valueOf(0));
                            jtEntrustCheckItem.setTotalProceedsPrice(String.valueOf(0));
                            jtEntrustCheckItem.setReceivablePrice(String.valueOf(0));
                            jtEntrustCheckItem.setTotalReceivablePrice(String.valueOf(0));
                            jtEntrustCheckItem.setCheckItemOtherName(null);
                        }
                        GetEntrustCheckItemList.add(jtEntrustCheckItem);
                        // 单个新增
//                        jtEntrustCheckItemMapper.insertSelective(jtEntrustCheckItem);
                    }
                    // 实现批量增加到数据库中
                    jtEntrustCheckItemMapper.insertSelectiveList(GetEntrustCheckItemList);
                }
                //补充样品id信息信息：
                JtReportInfo jtReportInfo = new JtReportInfo();
                jtReportInfo.setFlowStatus(4);
                jtReportInfo.setSampleId(jtSampleInfo.getSampleId());
                // 单个新增
                jtReportInfoMapper.insertSelective(jtReportInfo);
                // 成功返回委托编号
            }
        }
        return  jtEntrustInfo.getEntrustNumber();
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
    public String getEntrustNumber(){
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

    /**
     * 设置多个产品下 产品标准输出
      */
    String returnStr(List<String> strlist)
    {
        HashSet<String> hashSet = new HashSet<>();
        for (String str:strlist) {
            // 根据,(逗号)进行分割
            String[] split = str.split(",");
            for(int i = 0; i < split.length; i++) {
                System.out.println(split[i]);
                hashSet.add(split[i]);
            }
        }
        String strAdd = "";
        for (String strs:hashSet) {
            strAdd+=strs+",";
        }
        // 去掉最后符号,
        strAdd = strAdd.substring(0, strAdd.length() -1);
        return strAdd;
    }

    @Override
    public void downloadReport(HttpServletResponse response, Integer sampleId, String version,String entrustId,String reportPath) throws IOException {
        //根据sampleId获取文件id
        String fileId = entrustMapper.getFileIdBySampleId(sampleId,entrustId);
        FileInfo fileInfo = entrustMapper.queryByfId(fileId);
        new FileHomeLocation().responseFile(response, getFilePath(fileId, null,reportPath), fileInfo.getFileName());
    }

    public String getFilePath(String fileId, Integer version,String reportPath) throws IOException {
        FileInfo fileInfo = entrustMapper.queryByfId(fileId);
        // 需要处理文件版本为题
        // 默认下载 最大版本的
        FileVersionInfo fileVersionInfo;
        String infoPath = fileInfo.getPath();
        String[] strings = null;
        if (infoPath.contains("/")){
            strings = fileInfo.getPath().split("/");
        }else {
            strings = fileInfo.getPath().split("\\\\");
        }
        if (strings != null){
            reportPath = reportPath + strings[strings.length-1];
        }
        logger.info("下载报告的路径为:{}",reportPath);
        if (version != null) {
            fileVersionInfo = entrustMapper.selectFileVersionByVersion(fileId, version);
        } else {
            fileVersionInfo = entrustMapper.selectNewFileVersion(fileId);
        }
        // 通过
        if ("1".equals(fileVersionInfo.getIsWithUpload())) {
            reportPath = reportPath.substring(0, reportPath.lastIndexOf(".")) + "_" + fileVersionInfo.getFileVersion() + reportPath.substring(reportPath.lastIndexOf('.'));
        } else {
            if (version != null) {
                reportPath = reportPath.substring(0, reportPath.lastIndexOf(".")) + "_" + fileVersionInfo.getFileVersion() + reportPath.substring(reportPath.lastIndexOf('.'));

            }
        }
        return reportPath;
    }

    /**
     * 通过配置文件的键 读取配置文件
     *
     * @param fileName 配置文件键的名字
     * @return 返回配置文件键对应的值
     * @throws IOException
     */
    public String fileLocation(String fileName) throws IOException {
        FileHomeLocation fileHomeLocation = new FileHomeLocation();
        return fileHomeLocation.fileLocation(fileName);
    }
}
