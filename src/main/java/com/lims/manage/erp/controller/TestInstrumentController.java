package com.lims.manage.erp.controller;


import cn.hutool.core.util.StrUtil;
import com.aspose.cells.Cells;
import com.aspose.cells.Worksheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.DeviceEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestInstrument;
import com.lims.manage.erp.mapper.DeviceEntityMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.TestInstrumentService;
import com.lims.manage.erp.util.Const;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.util.SnowflakeIdGenerator;
import com.lims.manage.erp.vo.ExportParamVo;
import com.lims.manage.erp.vo.InstrumentRecordParamVo;
import com.lims.manage.erp.vo.LabelValueVo;
import com.lims.manage.erp.vo.TestInstrumentVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 仪器设备(TestInstrument)表控制层
 *
 * @author makejava
 * @since 2022-02-25 10:05:48
 */
@RestController
@RequestMapping("testInstrument")
@Api(value = "仪器设备管理", tags = {"仪器设备管理"})
public class TestInstrumentController extends ApiController {
    /**
     * 服务对象
     */
    @Resource
    private TestInstrumentService testInstrumentService;
    @Resource
    private LogManagerService logManagerService;
    @Autowired
    private DeviceEntityMapper deviceEntityMapper;
    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @GetMapping("/getAllOld")
    public Result getAllOld(TestInstrument testInstrument) {
        QueryWrapper<TestInstrument> queryWrapper = new QueryWrapper<>(testInstrument);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.eq("del_flag", 0);
        return ResultUtil.success(this.testInstrumentService.list(queryWrapper));
    }

    /**
     * 分页查询所有数据
     *
     * @param page           分页对象
     * @param testInstrument 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    @ApiOperation("分页查询仪器设备信息")
    public Result selectAll(Page<TestInstrumentVo> page, TestInstrument testInstrument) {
        QueryWrapper<TestInstrument> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("i.del_flag", 0);
        if (testInstrument.getName() != null) {
            queryWrapper.like("i.name", testInstrument.getName());
        }
        if (testInstrument.getCode() != null) {
            queryWrapper.like("i.code", testInstrument.getCode());
        }
        queryWrapper.orderByDesc("i.create_time");
        return ResultUtil.success(this.testInstrumentService.getPageList(page, queryWrapper));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    @ApiOperation("根据ID查询仪器设备信息")
    public Result selectOne(@PathVariable Serializable id) {
        if (id != null && id != "") {
            TestInstrument testInstrument = this.testInstrumentService.getOne(new QueryWrapper<TestInstrument>().eq("id", id).eq("del_flag", 0));
            return ResultUtil.success(testInstrument);
        } else {
            return ResultUtil.error("参数为空");
        }
    }

    /**
     * 查询设备下拉列表
     * @param search
     * @return
     */
    @GetMapping("/getDeviceList")
    public Result getDeviceList(String search) {
        List<LabelValueVo> deviceList = testInstrumentService.getDeviceList(search);
        return ResultUtil.success(deviceList);
    }

    /**
     * 新增数据
     *
     * @param testInstrument 实体对象
     * @return 新增结果
     */
    @PostMapping("/add_old")
    @ApiOperation("添加仪器设备")
    public Result add_old(@RequestBody TestInstrument testInstrument) {
        if (StrUtil.isEmptyIfStr(testInstrument)) {
            return ResultUtil.error("数据为空");
        }
        return this.testInstrumentService.addInstrument_old(testInstrument);
    }

    /**
     * 修改数据
     *
     * @param testInstrument 实体对象
     * @return 修改结果
     */
    @PostMapping("/edit")
    @ApiOperation("修改仪器设备")
    public Result update(@RequestBody TestInstrument testInstrument) {
        if (StrUtil.isEmptyIfStr(testInstrument)) {
            return ResultUtil.error("数据为空");
        }
        return this.testInstrumentService.updInstrument(testInstrument);
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @PostMapping("/del")
    @ApiOperation("删除仪器设备")
    public Result delete(@RequestBody List<Long> idList) {
        if (idList.size() != 0) {
            return this.testInstrumentService.delInstruments(idList);
        } else {
            return ResultUtil.error("数据为空");
        }
    }

    @PostMapping("/getInstrumentRecord")
    public Result getInstrumentRecord(@RequestBody InstrumentRecordParamVo paramVo) {
        if (paramVo == null || paramVo.getPageNum() == null || paramVo.getPageSize() == null || paramVo.getInstrumentId() == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }
        PageInfo instrumentRecord = testInstrumentService.getInstrumentRecord(paramVo);
        return ResultUtil.success(instrumentRecord);
    }

    /**
     * 下载单个设备的使用记录
     *
     * @param paramVo
     * @param response
     */
    @PostMapping("/exportInstrumentRecord")
    public void exportInstrumentRecord(@RequestBody InstrumentRecordParamVo paramVo, HttpServletResponse response) {
        HashMap<String, Object> stringObjectHashMap = testInstrumentService.exportInstrumentRecord(paramVo);
        XLSTransformer transformer = new XLSTransformer();
        InputStream fileStream = MinIoUtil.getFileStream("device-record", "record.xls");
        Workbook workbook;
        try {
            workbook = transformer.transformXLS(fileStream, stringObjectHashMap);
            workbook.setSheetName(0, stringObjectHashMap.get("deviceInfo").toString() + "使用记录");
            response.reset();
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode(stringObjectHashMap.get("deviceInfo") + "使用记录.xlsx", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.close();
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载多个设备的使用记录
     *
     * @param
     * @param response
     */
    @PostMapping("/batchExportInstrumentRecord")
    public void batchExportInstrumentRecord(@RequestBody ExportParamVo vo, HttpServletResponse response) {
        HashMap<String, Object> stringObjectHashMap = testInstrumentService.batchExportInstrumentRecord(vo.getDeviceId());
        XLSTransformer transformer = new XLSTransformer();
        InputStream fileStream = MinIoUtil.getFileStream("device-record", "records" + vo.getDeviceId().size() + ".xls");
        Workbook workbook;
        try {
            workbook = transformer.transformXLS(fileStream, stringObjectHashMap);
            for (int i = 0; i < vo.getDeviceId().size(); i++) {
                workbook.setSheetName(i, stringObjectHashMap.get("deviceInfo" + i).toString() + "使用记录");
            }
            response.reset();
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setContentType("application/x-msdownload");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode(stringObjectHashMap.get("deviceInfo0") + "等仪器的使用记录.xlsx", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.close();
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * 整理设备
     */
    @GetMapping("checkDevice")
    public void checkDevice() {
        this.testInstrumentService.checkDevice();
    }

    /**
     * 查询设备仪器列表
     * @param deviceEntity
     * @return
     */
    @PostMapping("/getAllDevice")
    public Result getAllDevice(@RequestBody DeviceEntity deviceEntity) {
        if (deviceEntity == null) {
            return ResultUtil.error("缺少必要参数！");
        }
        if (deviceEntity.getPageNum() == null || deviceEntity.getPageSize() == null) {
            return ResultUtil.error("缺少分页参数！");
        }
        PageInfo<DeviceEntity> allDevice = testInstrumentService.getAllDevice(deviceEntity);
        return ResultUtil.success("查询设备仪器列表成功！",allDevice);
    }

    /**
     * 新增设备
     * @param record
     * @return
     */
    @PostMapping("addDevice")
//    public Result addDevice(@RequestParam("json") String json, MultipartFile picture, MultipartFile contract, MultipartFile invoice) {
    public Result addDevice(@RequestBody DeviceEntity record) {
//        DeviceEntity record = JSON.parseObject(json, DeviceEntity.class);
        if (record.getName() == null || record.getCode() == null) {
            return ResultUtil.error("缺少必要参数！");
        }
        boolean save = testInstrumentService.addDevice(record, null, null, null);
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (!save) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()
                    +"添加设备仪器"+record.getId()+"失败!", Const.INSTRUMENT_MANAGEMENT_LOG,false);
            return ResultUtil.success("设备添加失败!");
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户："+userInfo.getUsername()
                +"添加设备仪器"+record.getId()+"成功!", Const.INSTRUMENT_MANAGEMENT_LOG, true);
        return ResultUtil.success("设备添加成功！", record);
    }

    /**
     * 修改设备
     * @param record
     * @return
     */
    @PostMapping("updateDevice")
    public Result updateDevice(@RequestBody DeviceEntity record) {
        if (record.getId() == null || record.getName() == null || record.getCode() == null) {
            return ResultUtil.error("缺少必要参数！");
        }
        boolean save = testInstrumentService.update(record);
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (!save) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()
                    +"修改设备仪器"+record.getId()+"失败!", Const.INSTRUMENT_MANAGEMENT_LOG,false);
            return ResultUtil.success("设备修改失败!");
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户："+userInfo.getUsername()
                +"修改设备仪器"+record.getId()+"成功!", Const.INSTRUMENT_MANAGEMENT_LOG, true);
        return ResultUtil.success("设备修改成功！", record);
    }

    /**
     * 删除设备
     * @param idList
     * @return
     */
    @PostMapping("deleteDevice")
    public Result deleteDevice(@RequestParam("idList") List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return ResultUtil.error("请选择要删除的设备！");
        }
        boolean save = testInstrumentService.deleteDevice(idList);
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (!save) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"用户："+userInfo.getUsername()
                    +"删除设备仪器"+idList+"失败!", Const.INSTRUMENT_MANAGEMENT_LOG,false);
            return ResultUtil.success("设备删除失败!");
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户："+userInfo.getUsername()
                +"删除设备仪器"+idList+"成功!", Const.INSTRUMENT_MANAGEMENT_LOG, true);
        return ResultUtil.success("设备删除成功！", idList);
    }

    /**
     * 打印设备标签
     * @param id
     * @param response
     */
    @GetMapping("printDeviceLable")
    public void printDeviceLable(Integer id, HttpServletResponse response){
        if (id == null){
            return;
        }
        //根据设备id获取设备详情
        LambdaQueryWrapper<TestInstrument> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(TestInstrument::getId,id);
        TestInstrument testInstrument = testInstrumentService.getOne(queryWrapper);
        if (testInstrument != null){
            response.reset();
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setContentType("application/x-msdownload");
            response.setCharacterEncoding("UTF-8");
            try {
                String fileName = URLEncoder.encode(testInstrument.getName()+"_"+testInstrument.getCode()+".xlsx", "UTF-8");
                response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
                ServletOutputStream outputStream = testInstrumentService.printDeviceLable(id,testInstrument, response);
                outputStream.flush();
                outputStream.close();
            }catch (Exception e){
                logger.error("打印设备标签异常:{}",e);
            }
        }

    }

    @GetMapping("printDeviceLables")
    public void printDeviceLables( HttpServletResponse response){
        List<Integer> list = testInstrumentService.getAllIds();
        for (Integer id :list){
            if (id != null){
                //根据设备id获取设备详情
                LambdaQueryWrapper<TestInstrument> queryWrapper = new LambdaQueryWrapper();
                queryWrapper.eq(TestInstrument::getId,id);
                TestInstrument testInstrument = testInstrumentService.getOne(queryWrapper);
                if (testInstrument != null){
                    try {
                        Thread.sleep(20);
                         testInstrumentService.printDeviceLables(id,testInstrument, response);
                    }catch (Exception e){
                        logger.error("打印设备标签异常:{}",e);
                    }
                }
            }
        }
    }

    /**
     * 导入仪器设备
     */
    @GetMapping("importDevice")
    @Transactional(rollbackFor = Exception.class)
    public void importDevice(HttpServletResponse response) throws Exception{
        //读取excel
        com.aspose.cells.Workbook workbook1 = new com.aspose.cells.Workbook("D:\\Users\\Administrator\\Desktop\\需打印设备标识卡.xlsx");
        Worksheet worksheet1 = workbook1.getWorksheets().get(0);
        Cells cells1 = worksheet1.getCells();
        //遍历excel，根据编号查询
        int num1 = 2;
        List<DeviceEntity> list = Lists.newArrayList();
        while (num1<=45){
            DeviceEntity deviceEntity = new DeviceEntity();
            String codeIndex = "A"+num1;
            String nameIndex = "C"+num1;
            String modeIndex = "D"+num1;
            String manufacturerIndex = "E"+num1;
            String serial_numberIndex = "F"+num1;
            String purchase_dateIndex = "G"+num1;
            String isIndex = "H"+num1;
            String affirm_wayIndex = "I"+num1;
            String calibration_corporation = "J"+num1;
            String appraisal_date = "K"+num1;
            String use_dept = "N"+num1;
            String range = "P"+num1;
            String level = "Q"+num1;
            String calibration_param = "R"+num1;
            String store_place = "S"+num1;

            deviceEntity.setCode(cells1.get(codeIndex).getValue() != null?cells1.get(codeIndex).getValue().toString():"");
            deviceEntity.setName(cells1.get(nameIndex).getValue() != null?cells1.get(nameIndex).getValue().toString():"");
            deviceEntity.setModel(cells1.get(modeIndex).getValue()!= null?cells1.get(modeIndex).getValue().toString():"");
            deviceEntity.setManufacturer(cells1.get(manufacturerIndex).getValue()!= null?cells1.get(manufacturerIndex).getValue().toString():"");
            deviceEntity.setSerialNumber(cells1.get(serial_numberIndex).getValue()!=null?cells1.get(serial_numberIndex).getValue().toString():"");
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Object value = cells1.get(purchase_dateIndex).getValue();
                if (value != null){
                    String string = cells1.get(purchase_dateIndex).getValue().toString();
                    Date date = simpleDateFormat.parse(string);
                    deviceEntity.setPurchaseDate(date);
                }
            }catch (Exception e){

            }
            deviceEntity.setIsCalibration(cells1.get(isIndex).getValue()!=null?cells1.get(isIndex).getValue().toString():"");
            deviceEntity.setAffirmWay(cells1.get(affirm_wayIndex).getValue()!=null?cells1.get(affirm_wayIndex).getValue().toString():"");
            deviceEntity.setCalibrationCorporation(cells1.get(calibration_corporation).getValue()!=null?cells1.get(calibration_corporation).getValue().toString():"");
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Object value = cells1.get(appraisal_date).getValue();
                if (value != null){
                    String string = cells1.get(appraisal_date).getValue().toString();
                    Date date = simpleDateFormat.parse(string);
                    deviceEntity.setAppraisalDate(date);
                }
            }catch (Exception e){

            }
            deviceEntity.setUseDept(cells1.get(use_dept).getValue()!=null?cells1.get(use_dept).getValue().toString():"");
            deviceEntity.setRange(cells1.get(range).getValue()!=null?cells1.get(range).getValue().toString():"");
            deviceEntity.setLevel(cells1.get(level).getValue()!=null?cells1.get(level).getValue().toString():"");
            deviceEntity.setCalibrationParam(cells1.get(calibration_param).getValue()!=null?cells1.get(calibration_param).getValue().toString():"");
            deviceEntity.setStorePlace(cells1.get(store_place).getValue()!=null?cells1.get(store_place).getValue().toString():"");
            deviceEntity.setFilesState("正常");
            deviceEntity.setId(snowflakeIdGenerator.nextId());
            deviceEntity.setStatus("0");
            deviceEntity.setDelFlag(0);
            deviceEntity.setCreateTime(new Date());
            list.add(deviceEntity);
            num1++;
        }
        //导入
        for (DeviceEntity deviceEntity :list){
            deviceEntityMapper.insert(deviceEntity);
        }
        //打印
        for (DeviceEntity deviceEntity :list){
            TestInstrument testInstrument = new TestInstrument();
            testInstrument.setName(deviceEntity.getName());//设备名称
            testInstrument.setCode(deviceEntity.getCode());//设备编号
            testInstrument.setModel(deviceEntity.getModel());//设备型号
            testInstrument.setSerialNumber(deviceEntity.getSerialNumber());//出厂编号
            try {
                Thread.sleep(20);
                testInstrumentService.printDeviceLables(deviceEntity.getId(),testInstrument, response);
            }catch (Exception e){
                logger.error("打印设备标签异常:{}",e);
            }
        }
    }

}

