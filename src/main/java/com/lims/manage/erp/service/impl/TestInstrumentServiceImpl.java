package com.lims.manage.erp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Worksheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.DeviceEntityMapper;
import com.lims.manage.erp.mapper.InstrumentRecordEntityMapper;
import com.lims.manage.erp.mapper.TestInstrumentDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.*;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.PDFHelper3;
import com.lims.manage.erp.util.QRCodeUtil;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.InstrumentRecordListVo;
import com.lims.manage.erp.vo.InstrumentRecordParamVo;
import com.lims.manage.erp.vo.LabelValueVo;
import com.lims.manage.erp.vo.TestInstrumentVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * 仪器设备(TestInstrument)表服务实现类
 *
 * @author makejava
 * @since 2022-02-25 10:05:51
 */
@Service("testInstrumentService")
public class TestInstrumentServiceImpl extends ServiceImpl<TestInstrumentDao, TestInstrument> implements TestInstrumentService {
    @Resource
    private TestLaboratoryService testLaboratoryService;
    @Resource
    private TestInstrumentDao testInstrumentDao;
    @Resource
    private LogManagerService logManagerService;
    @Resource
    private TestInstrumentAppraisalRecordService testInstrumentAppraisalRecordService;
    @Autowired
    private InstrumentRecordEntityMapper instrumentRecordEntityMapper;
    @Autowired
    private DeviceEntityMapper deviceEntityMapper;

    @Resource
    private SysOssService sysOssService;
    @Override
    public Result addInstrument_old(TestInstrument testInstrument) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testInstrument.getLaboratoryId()!=null){
            TestLaboratory testLaboratory=testLaboratoryService.getById(testInstrument.getLaboratoryId());
            if (StrUtil.isEmptyIfStr(testLaboratory)){
                return ResultUtil.error("没有这个实验室！");
            }
        }else {
            return ResultUtil.error("所在实验室参数为空！");
        }
        QueryWrapper<TestInstrument> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("laboratory_id",testInstrument.getLaboratoryId());
        queryWrapper.eq("name",testInstrument.getName());
        if (this.list(queryWrapper).size()>0){
            return ResultUtil.error("该实验室已有该设备！");
        }
        testInstrument.setStatus("0");
        testInstrument.setDelFlag(0);
        testInstrument.setCreateTime(new Date());
        testInstrument.setUpdateTime(null);
        if (this.save(testInstrument)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加设备仪器"+testInstrument.getId()+"成功!", Const.INSTRUMENT_MANAGEMENT_LOG,true);
            return ResultUtil.success("设备添加成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"添加设备仪器失败!", Const.INSTRUMENT_MANAGEMENT_LOG,false);
            return ResultUtil.error("添加失败");
        }
    }

    @Override
    public Result updInstrument(TestInstrument testInstrument) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        if (testInstrument.getId()==null){
            return ResultUtil.error("修改对象ID为空！");
        }
        if (testInstrument.getLaboratoryId()!=null){
            TestLaboratory testLaboratory=testLaboratoryService.getById(testInstrument.getLaboratoryId());
            if (StrUtil.isEmptyIfStr(testLaboratory)){
                return ResultUtil.error("没有这个实验室！");
            }
        }
        QueryWrapper<TestInstrument> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("laboratory_id",testInstrument.getLaboratoryId());
        queryWrapper.eq("name",testInstrument.getName());
        queryWrapper.ne("id",testInstrument.getId());
        queryWrapper.eq("del_flag",0);
        if (this.list(queryWrapper).size()>0){
            return ResultUtil.error("该实验室已有该设备！");
        }
        testInstrument.setUpdateTime(new Date());
        List<TestInstrumentAppraisalRecord> testInstrumentAppraisalRecords=testInstrumentAppraisalRecordService.list(new QueryWrapper<TestInstrumentAppraisalRecord>().eq("code",testInstrument.getId()));
        if (testInstrumentAppraisalRecords!=null&&testInstrumentAppraisalRecords.size()!=0){
            for (TestInstrumentAppraisalRecord testInstrumentAppraisalRecord : testInstrumentAppraisalRecords) {
                testInstrumentAppraisalRecord.setName(testInstrument.getName());
                testInstrumentAppraisalRecord.setUpdateTime(new Date());
            }
            testInstrumentAppraisalRecordService.updateBatchById(testInstrumentAppraisalRecords);
        }
        if (this.updateById(testInstrument)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改设备仪器"+testInstrument.getId()+"成功!", Const.INSTRUMENT_MANAGEMENT_LOG,true);
            return ResultUtil.success("修改成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"修改设备仪器"+testInstrument.getId()+"失败!", Const.INSTRUMENT_MANAGEMENT_LOG,false);
            return ResultUtil.error("修改失败");
        }
    }

    @Override
    public Result delInstruments(List<Long> idList) {
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if(userInfo==null){
            return ResultUtil.error("token 已过期！");
        }
        List<TestInstrument> testInstrumentList=new ArrayList<>();
        for (Long aLong : idList) {
            TestInstrument testInstrument=new TestInstrument();
            testInstrument.setUpdateTime(new Date());
            testInstrument.setDelFlag(1);
            testInstrument.setId(aLong.intValue());
            String url1=this.getById(aLong).getPicture();
            if (url1!=null){
                sysOssService.delAnnounce(url1);
            }
            String url2=this.getById(aLong).getContractUrl();
            if (url2!=null){
                sysOssService.delAnnounce(url2);
            }
            String url3=this.getById(aLong).getInvoiceUrl();
            if (url3!=null){
                sysOssService.delAnnounce(url3);
            }
            testInstrumentList.add(testInstrument);
        }
        String idStr=idList.toString();
        if (this.updateBatchById(testInstrumentList)){
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除设备仪器"+idStr+"成功!", Const.INSTRUMENT_MANAGEMENT_LOG,true);
            return ResultUtil.success("删除成功");
        }else {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()+"删除设备仪器"+idStr+"失败!", Const.INSTRUMENT_MANAGEMENT_LOG,false);
            return ResultUtil.error("删除失败");
        }
    }

    @Override
    public IPage<TestInstrumentVo> getPageList(Page<TestInstrumentVo> page, QueryWrapper<TestInstrument> queryWrapper) {
        return testInstrumentDao.getPageList(page,queryWrapper);
    }

    @Override
    public PageInfo getInstrumentRecord(InstrumentRecordParamVo paramVo) {
        PageHelper.clearPage();
        PageHelper.startPage(paramVo.getPageNum(),paramVo.getPageSize());
        List<InstrumentRecordListVo> instrumentRecord = instrumentRecordEntityMapper.getInstrumentRecord(paramVo);
        PageInfo<InstrumentRecordListVo> pageInfo = new com.github.pagehelper.PageInfo<>(instrumentRecord);
        return pageInfo;
    }

    @Override
    public HashMap<String,Object> exportInstrumentRecord(InstrumentRecordParamVo paramVo) {
        HashMap<String,Object> result = Maps.newHashMap();
        List<InstrumentRecordListVo> instrumentRecord = instrumentRecordEntityMapper.getInstrumentRecord(paramVo);
        result.put("recordList",instrumentRecord);
        String instrumentInfo = instrumentRecordEntityMapper.getInstrumentInfo(paramVo.getInstrumentId());
        result.put("deviceInfo",instrumentInfo);
        return result;
    }

    @Override
    public HashMap<String, Object> batchExportInstrumentRecord(List<Long> instrumentIds) {
        HashMap<String, Object> result = Maps.newHashMap();
        String key = "recordList";
        String deviceInfo = "deviceInfo";
        for (int i = 0; i < instrumentIds.size(); i++) {
            InstrumentRecordParamVo paramVo = new InstrumentRecordParamVo();
            paramVo.setInstrumentId(instrumentIds.get(i));
            List<InstrumentRecordListVo> instrumentRecord = instrumentRecordEntityMapper.getInstrumentRecord(paramVo);
            result.put(key+i,instrumentRecord);
            String instrumentInfo = instrumentRecordEntityMapper.getInstrumentInfo(instrumentIds.get(i));
            result.put(deviceInfo+i,instrumentInfo);
        }
        return result;
    }

    @Override
    public void checkDevice() {
        long start = System.currentTimeMillis();
        int id = 16666;
        List<DeviceEntity> saveList = Lists.newArrayList();
        List<DeviceEntity> updateList = Lists.newArrayList();
        List<DeviceEntity> newDevices = deviceEntityMapper.getNewDevices();
        List<DeviceEntity> oldDevices = deviceEntityMapper.getOldDevices();
        Map<Integer,DeviceEntity> maps = Maps.newHashMap();
        for (int i = 0; i < oldDevices.size(); i++) {
            DeviceEntity deviceEntity = oldDevices.get(i);
            maps.put(deviceEntity.getId(),deviceEntity);
        }
        System.out.println("新设备共有:"+ newDevices.size());
        int total = 0;
        for (int i = 0; i < newDevices.size(); i++) {
            DeviceEntity deviceEntity = newDevices.get(i);
            String code = deviceEntity.getCode();
            String name = deviceEntity.getName();
            String model = deviceEntity.getModel();
            String serialNumber = deviceEntity.getSerialNumber();
            List<DeviceEntity> oldDevicesByCode = deviceEntityMapper.getOldDevicesByCode(code);
            System.out.println("设备编号为：【"+code+"】，共找到【"+oldDevicesByCode.size()+"】条旧设备信息！");
            if(CollectionUtils.isEmpty(oldDevicesByCode)){//未找到旧设备，新增
                id = id + i;
//                deviceEntity.setOldId(id);
                saveList.add(deviceEntity);
            }else if(oldDevicesByCode.size() > 1){//找到多条，比较其他信息
                for (int j = 0; j < oldDevicesByCode.size(); j++) {
                    DeviceEntity oldDevice = oldDevicesByCode.get(j);
                    String oldDeviceName = oldDevice.getName();
                    String oldDeviceModel = oldDevice.getModel();
                    String oldDeviceSerialNumber = oldDevice.getSerialNumber();
                    if(code.equals("LQ-301-55") || code.equals("2把") || code.equals("TG-107-44")
                        ||code.equals("TG-107-99") || code.equals("LQ-203-11") || code.equals("LQ-305-60")
                            ||code.equals("LQ-201-20") || code.equals("LQ-309-13") || code.equals("-")){//比较name
                        if(name.equals(oldDeviceName)){
                            Integer oldId = oldDevice.getId();
//                            deviceEntity.setOldId(oldId);
                            updateList.add(deviceEntity);
//                            System.out.println("新设备name=【"+name+"】与旧设备name【"+oldDeviceName+"】匹配成功！");
                            break;
                        }else{
//                            System.out.println("新设备name=【"+name+"】与旧设备name【"+oldDeviceName+"】不匹配！");
                        }
                    }else if(code.equals("LQ-301-31（2个-1）") || code.equals("LQ-301-31（2个-2）")){//比较name和model
                        if(name.equals(oldDeviceName) && model.equals(oldDeviceModel)){
                            Integer oldId = oldDevice.getId();
//                            deviceEntity.setOldId(oldId);
                            updateList.add(deviceEntity);
//                            System.out.println("新设备name=【"+name+"】--model=【"+model+"与旧设备name【"+oldDeviceName+"】--model=【"+oldDeviceModel+"】匹配成功！");
                            break;
                        }else{
//                            System.out.println("新设备name=【"+name+"】--model=【"+model+"与旧设备name【"+oldDeviceName+"】--model=【"+oldDeviceModel+"】不匹配！");
                        }
                    }else if(code.equals("LQ-301-31") || code.equals("LQ-301-31（3个-1）")
                            || code.equals("LQ-301-31（3个-2）") ||code.equals("LQ-301-31（3个-3）")
                            || code.equals("JKY-YH-0004") || code.equals("LQ-301-31（4个-1）")
                            ||code.equals("LQ-301-31（4个-2）") ||code.equals("LQ-301-31（4个-3）")
                            || code.equals("LQ-301-31（4个-4）")){//比较model
                        if(model.equals(oldDeviceModel)){
                            Integer oldId = oldDevice.getId();
//                            deviceEntity.setOldId(oldId);
                            updateList.add(deviceEntity);
//                            System.out.println("新设备model=【"+model+"与旧设备model=【"+oldDeviceModel+"】匹配成功！");
                            break;
                        }else{
//                            System.out.println("新设备model=【"+model+"与旧设备model=【"+oldDeviceModel+"】不匹配！");
                        }
                    }else{//比较serial_number
                        if(serialNumber.equals(oldDeviceSerialNumber)){
                            Integer oldId = oldDevice.getId();
//                            deviceEntity.setOldId(oldId);
                            updateList.add(deviceEntity);
//                            System.out.println("新设备serialNumber=【"+serialNumber+"】与旧设备oldDeviceSerialNumber=【"+oldDeviceSerialNumber+"】匹配成功！");
                            break;
                        }else{
//                            System.out.println("新设备serialNumber=【"+serialNumber+"】与旧设备oldDeviceSerialNumber=【"+oldDeviceSerialNumber+"】不匹配！");
                        }
                    }
                }
            }else if(oldDevicesByCode.size() == 1){//只找到一条旧设备的直接匹配
                DeviceEntity oldDevice = oldDevicesByCode.get(0);
                String oldDeviceCode = oldDevice.getCode();
                Integer oldId = oldDevice.getId();
//                deviceEntity.setOldId(oldId);
                updateList.add(deviceEntity);
//                System.out.println("新设备code=【"+code+"】与旧设备code=【"+oldDeviceCode+"】匹配成功！");
            }else{
                System.out.println("新设备code=【"+code+"】");
            }
        }
//        System.out.println("剩下的设备（应该有8个！）："+maps);
        System.out.println("新增设备条数为：【"+saveList.size()+"】条！");
        System.out.println("修改设备条数为：【"+updateList.size()+"】条！");
        updateList.addAll(saveList);
        deviceEntityMapper.batchInsert(updateList);
        long end = System.currentTimeMillis();
        long l = (end - start) / 1000;
        System.out.println("共计耗时【"+l+"】秒！");
    }

    @Override
    public PageInfo<DeviceEntity> getAllDevice(DeviceEntity deviceEntity) {
        PageHelper.startPage(deviceEntity.getPageNum(), deviceEntity.getPageSize());
        List<DeviceEntity> allDevice = deviceEntityMapper.getAllDevice(deviceEntity);
        PageInfo<DeviceEntity> pageInfo = new PageInfo<>(allDevice);
        return pageInfo;
    }

    @Override
    public boolean addDevice(DeviceEntity record, MultipartFile picture, MultipartFile contract, MultipartFile invoice) {
        //上传附件
//        if (picture != null && !"blob".equals(picture.getOriginalFilename())) {
//            String upload = upload(picture);
//            record.setPicture(upload);
//        }
//        if (contract != null && !"blob".equals(contract.getOriginalFilename())) {
//            String upload = upload(contract);
//            record.setContractUrl(upload);
//        }
//        if (invoice != null && !"blob".equals(invoice.getOriginalFilename())) {
//            String upload = upload(invoice);
//            record.setInvoice(upload);
//        }
        Integer newId = deviceEntityMapper.getNewId();
        record.setId(newId);
        record.setStatus("0");
        record.setDelFlag(0);
        record.setCreateTime(new Date());
        int insert = deviceEntityMapper.insert(record);
        if (insert > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean update(DeviceEntity record) {
        int update = deviceEntityMapper.updateByPrimaryKeySelective(record);
        if (update > 0) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDevice(List<Long> idList) {
        int i = deviceEntityMapper.deleteByIds(idList);
        if (i > 0) {
            return true;
        }
        return false;
    }

    @Override
    public ServletOutputStream printDeviceLable(Integer id, TestInstrument testInstrument, HttpServletResponse response) throws Exception {
        ServletOutputStream outputStream = response.getOutputStream();
        PDFHelper3.getLicense();
        //填冲数据
        InputStream fileStream = MinIoUtil.getFileStream("sample-tag", "device-lable.xlsx");
        InputStream imageStrem = MinIoUtil.getFileStream("sample-tag", "logo.png");
        com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook(fileStream);
        Worksheet worksheet = workbook.getWorksheets().get(0);
        //填充数据
        worksheet.getCells().get("I6").setValue(testInstrument.getName());//设备名称
        worksheet.getCells().get("I11").setValue(testInstrument.getCode());//设备编号
        worksheet.getCells().get("I14").setValue(testInstrument.getModel());//设备型号
        worksheet.getCells().get("I17").setValue(testInstrument.getSerialNumber());//出厂编号
        //设置二维码
        BufferedImage bufferedImage = QRCodeUtil.getBufferedImage(id+"");

        InputStream stream = bufferedImageToInputStream(bufferedImage);
        //设置二维码和logo
        worksheet.getPictures().add(1,3,imageStrem,30,30);
        worksheet.getPictures().add(11,19,stream,16,16);
        workbook.save(outputStream, SaveFormat.XLSX);
        return outputStream;
    }

    @Override
    public List<LabelValueVo> getDeviceList(String search) {
        List<LabelValueVo> deviceList = deviceEntityMapper.getDeviceList(search);
        return deviceList;
    }

    private String upload(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String upload = MinIoUtil.upload("", file, fileName);
        String[] split = upload.split("\\?");//去掉返回的URL中的参数
        String s = split[0];
        String decode = null;
        try {
            decode = URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decode;
    }

    public InputStream bufferedImageToInputStream(BufferedImage image){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", os);
            InputStream input = new ByteArrayInputStream(os.toByteArray());
            return input;
        } catch (IOException e) {
            log.error("提示:",e);
        }
        return null;
    }
}

