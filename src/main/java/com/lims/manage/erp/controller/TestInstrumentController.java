package com.lims.manage.erp.controller;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.aspose.cells.Cells;
import com.aspose.cells.Worksheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.annotation.Log;
import com.lims.manage.erp.entity.DeviceEntity;
import com.lims.manage.erp.entity.InstrumentRecordEntity;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TestInstrument;
import com.lims.manage.erp.enums.BusinessType;
import com.lims.manage.erp.mapper.DeviceEntityMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.SysOssService;
import com.lims.manage.erp.service.TestInstrumentService;
import com.lims.manage.erp.util.*;
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
import org.springframework.web.multipart.MultipartFile;

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
import java.util.Map;

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
    @Resource
    private SysOssService sysOssService;

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
     * 修改设备使用记录
     * @param paramVo
     * @return
     */
    @PostMapping("/updateInstrumentRecord")
    public Result updateInstrumentRecord(@RequestBody InstrumentRecordEntity paramVo) {
        if (paramVo.getId() == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }
        testInstrumentService.updateInstrumentRecord(paramVo);
        return ResultUtil.success("修改设备使用记录成功！");
    }

    /**
     * 删除设备使用记录
     * @param recordId
     * @return
     */
    @GetMapping("/deleteInstrumentRecord")
    public Result updateInstrumentRecord(Long recordId) {
        if (recordId == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        }
        testInstrumentService.deleteInstrumentRecord(recordId);
        return ResultUtil.success("删除设备使用记录成功！");
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
            workbook.setSheetName(0, stringObjectHashMap.get("deviceInfo").toString().replace("/", "-") + "使用记录");
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
        return ResultUtil.success("查询设备仪器列表成功！", allDevice);
    }

    /**
     * 通过实验室id 查询设备下拉列表
     *
     * @param laboratoryId
     * @return
     */
    @GetMapping("/getDropDownDeviceList")
    public Result getDropDownDeviceList(Integer laboratoryId) {
        if (laboratoryId == null) {
            return ResultUtil.error("缺少必要参数！");
        }
        LambdaQueryWrapper<TestInstrument> deviceWrapper = new LambdaQueryWrapper<>();
        deviceWrapper.select(TestInstrument::getId, TestInstrument::getCode, TestInstrument::getName);
        deviceWrapper.eq(TestInstrument::getLaboratoryId, laboratoryId);
        List<TestInstrument> list = testInstrumentService.list(deviceWrapper);
        return ResultUtil.success(list);
    }

    /**
     * 进行仪器与实验室授权
     *
     * @param laboratoryId
     * @param ids
     * @return
     */
    @GetMapping("/impowerInstrumentsAndLaboratories")
    public Result impowerInstrumentsAndLaboratories(Integer laboratoryId, Integer ids[]) {

        return testInstrumentService.impowerInstrumentsAndLaboratories(laboratoryId, ids);
    }

    /**
     * 进行实验室与仪器移除
     *
     * @param ids
     * @return
     */
    @GetMapping("/removeInstrumentsAndLaboratories")
    public Result removeInstrumentsAndLaboratories(Integer laboratoryId, Integer ids[]) {

        return testInstrumentService.removeInstrumentsAndLaboratories(laboratoryId, ids);
    }

    /**
     * 新增设备
     *
     * @param record TODO: 已过时 前后端废弃
     * @return
     */
    @Log(title = "新增设备", businessType = BusinessType.INSERT)
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
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername()
                    + "添加设备仪器" + record.getId() + "失败!", Const.INSTRUMENT_MANAGEMENT_LOG, false);
            return ResultUtil.success("设备添加失败!");
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername()
                + "添加设备仪器" + record.getId() + "成功!", Const.INSTRUMENT_MANAGEMENT_LOG, true);
        return ResultUtil.success("设备添加成功！", record);
    }

    /**
     * 修改设备
     *
     * @param record TODO: 已过时 前后端废弃
     * @return
     */
    @Log(title = "修改设备", businessType = BusinessType.UPDATE)
    @PostMapping("updateDevice")
    public Result updateDevice(@RequestBody DeviceEntity record) {
        if (record.getId() == null || record.getName() == null || record.getCode() == null) {
            return ResultUtil.error("缺少必要参数！");
        }
        boolean save = testInstrumentService.update(record);
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (!save) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername()
                    + "修改设备仪器" + record.getId() + "失败!", Const.INSTRUMENT_MANAGEMENT_LOG, false);
            return ResultUtil.success("设备修改失败!");
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername()
                + "修改设备仪器" + record.getId() + "成功!", Const.INSTRUMENT_MANAGEMENT_LOG, true);
        return ResultUtil.success("设备修改成功！", record);
    }

    /**
     * 新增设备
     *
     * @return
     */
    @Log(title = "新增设备", businessType = BusinessType.INSERT)
    @PostMapping("addDeviceFile")
    public Result addDeviceFile(@RequestParam("json") String json, MultipartFile file) {
        if (StrUtil.isEmptyIfStr(json)) {
            return ResultUtil.error("数据为空");
        }
        DeviceEntity record = JSON.parseObject(json, DeviceEntity.class);
        if (record.getName() == null || record.getCode() == null) {
            return ResultUtil.error("缺少必要参数！");
        }
        if (file != null && StringUtils.isNotEmpty(file.getOriginalFilename())) {
            Map<String, Object> mapObject = sysOssService.postAnnounce(file);
            String fileUrl = (String) mapObject.get("fileUrl");
            record.setPicture(fileUrl);
        }
        boolean save = testInstrumentService.addDevice(record, null, null, null);
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (!save) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername()
                    + "添加设备仪器" + record.getId() + "失败!", Const.INSTRUMENT_MANAGEMENT_LOG, false);
            return ResultUtil.success("设备添加失败!");
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername()
                + "添加设备仪器" + record.getId() + "成功!", Const.INSTRUMENT_MANAGEMENT_LOG, true);
        return ResultUtil.success("设备添加成功！", record);
    }

    /**
     * 修改设备
     *
     * @param json
     * @param file
     * @return
     */
    @Log(title = "修改设备", businessType = BusinessType.UPDATE)
    @PostMapping("updateDeviceFile")
    public Result updateDevice(@RequestParam("json") String json, MultipartFile file) {

        if (StrUtil.isEmptyIfStr(json)) {
            return ResultUtil.error("数据为空");
        }
        DeviceEntity record = JSON.parseObject(json, DeviceEntity.class);

        if (record.getId() == null || record.getName() == null || record.getCode() == null) {
            return ResultUtil.error("缺少必要参数！");
        }
        if (file != null && StringUtils.isNotEmpty(file.getOriginalFilename())) {
            Map<String, Object> mapObject = sysOssService.postAnnounce(file);
            String fileUrl = (String) mapObject.get("fileUrl");
            record.setPicture(fileUrl);
            // 获取信息
            TestInstrument data = testInstrumentService.getById(record.getId());
            if (StringUtils.isNotEmpty(data.getPicture())) {
                // 进行移除附件
                sysOssService.delAnnounce(data.getPicture());
            }
        }
        boolean save = testInstrumentService.update(record);
        SysUserEntity userInfo = ShiroUtils.getUserInfo();
        if (!save) {
            logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername()
                    + "修改设备仪器" + record.getId() + "失败!", Const.INSTRUMENT_MANAGEMENT_LOG, false);
            return ResultUtil.success("设备修改失败!");
        }
        logManagerService.addOpSysLog(ShiroUtils.getUserInfo(), "用户：" + userInfo.getUsername()
                + "修改设备仪器" + record.getId() + "成功!", Const.INSTRUMENT_MANAGEMENT_LOG, true);
        return ResultUtil.success("设备修改成功！", record);
    }

    /**
     * 仪器图片上传
     */
    @RequestMapping("/uploading/{id}")
    public Result uploading(MultipartFile file) {

        // 处理file文件后缀
        // 常规图片后缀名。
        String[] nameSuffixS = Const.nameSuffixS;
        // true 正常运行。 flase 返回数组中不存在格式。
        Boolean flag = false;

        String name = file.getOriginalFilename();
        String[] strings = name.split("\\.");
        String nameSuffix = strings[strings.length - 1];
        for (int i = 0; i < nameSuffixS.length; i++) {
            if (nameSuffixS[i].equalsIgnoreCase(nameSuffix)) {
                // 变动
                flag = true;
            }
        }

        if (flag == false) {
            return ResultUtil.error("文件上传失败 图片后缀不在通用规则中！");
        }
        return ResultUtil.success("图片上传成功");
    }

    /**
     * 删除设备
     *
     * @param idList
     * @return
     */
    @Log(title = "删除设备", businessType = BusinessType.DELETE)
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
    @Log(title = "打印设备标签", businessType = BusinessType.EXPORT)
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
    public void printDeviceLables( HttpServletResponse response) throws Exception{
        List<Integer> list = Lists.newArrayList();
        //获取excel表格的设备
        //读取excel
        com.aspose.cells.Workbook workbook1 = new com.aspose.cells.Workbook("D:\\Users\\Administrator\\Desktop\\20241105需要导入lims仪器清单.xlsx");
        Worksheet worksheet1 = workbook1.getWorksheets().get(0);
        Cells cells1 = worksheet1.getCells();
        //遍历excel，根据编号查询
        int num1 = 2;

        while (num1<=80) {
            String codeIndex = "B" + num1;
            String code = cells1.get(codeIndex).getValue().toString();
            LambdaQueryWrapper<TestInstrument> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.eq(TestInstrument::getCode,code);
            List<TestInstrument> instruments = testInstrumentService.list(queryWrapper);
            list.add(instruments.get(0).getId());
            num1++;
        }
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
    @Log(title = "导入仪器设备", businessType = BusinessType.IMPORT)
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

    /**
     * 更新或者插入仪器设备
     */
    @GetMapping("importDevice20231228")
    @Transactional(rollbackFor = Exception.class)
    public void importOrUpdateDevice() throws Exception{
        List<DeviceEntity> list = Lists.newArrayList();
        List<DeviceEntity> updateList = Lists.newArrayList();


        //读取excel
        com.aspose.cells.Workbook workbook1 = new com.aspose.cells.Workbook("D:\\Users\\Administrator\\Desktop\\20241105需要导入lims仪器清单.xlsx");
        Worksheet worksheet1 = workbook1.getWorksheets().get(0);
        Cells cells1 = worksheet1.getCells();
        //遍历excel，根据编号查询
        int num1 = 2;

        while (num1<=80){
            DeviceEntity deviceEntity = new DeviceEntity();
            String codeIndex = "B"+num1;
            String fileState = "C" +num1;
            String nameIndex = "D"+num1;
            String modeIndex = "E"+num1;
            String manufacturerIndex = "F"+num1;
            String serial_numberIndex = "G"+num1;
            String price = "H"+num1;
            String purchase_dateIndex = "I"+num1;
            String isIndex = "J"+num1;//是否检定校准
            String affirm_wayIndex = "K"+num1;//确认方式
            String calibration_corporation = "L"+num1;//鉴定校准单位
            String appraisal_date = "M"+num1;//检定校准日期
            String calibration_period = "N"+num1;//检定周期
            String expire_date = "O"+num1;//鉴定校准失效日期
            //合并
            String use_dept = "P"+num1;//设备使用维护部门
            String team = "V"+num1; //使用科室

            String calibration_number = "Q"+num1;//鉴定校准证书编号
            String range = "R"+num1;//里程
            String level = "S"+num1;//精度
            String calibration_param = "T"+num1;
            //合并
            String place = "X"+num1;
            String store_place = "U"+num1;

            String device_admin = "W"+num1;
            String code = cells1.get(codeIndex).getValue() != null?cells1.get(codeIndex).getValue().toString():"";
            deviceEntity.setCode(code);
            deviceEntity.setFilesState(cells1.get(fileState).getValue() != null?cells1.get(fileState).getValue().toString():"");
            String name = cells1.get(nameIndex).getValue() != null?cells1.get(nameIndex).getValue().toString():"";
            deviceEntity.setName(name);
            String model = cells1.get(modeIndex).getValue()!= null?cells1.get(modeIndex).getValue().toString():"";
            deviceEntity.setModel(model);
            deviceEntity.setManufacturer(cells1.get(manufacturerIndex).getValue()!= null?cells1.get(manufacturerIndex).getValue().toString():"");

            String sn = cells1.get(serial_numberIndex).getValue()!=null?cells1.get(serial_numberIndex).getValue().toString():"";
            deviceEntity.setSerialNumber(sn);
            deviceEntity.setPrice(cells1.get(price).getValue()!=null?cells1.get(price).getValue().toString():"");
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
            deviceEntity.setCalibrationPeriod(cells1.get(calibration_period).getValue()!=null?cells1.get(calibration_period).getValue().toString():"");
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Object value = cells1.get(expire_date).getValue();
                if (value != null){
                    String string = cells1.get(expire_date).getValue().toString();
                    Date date = simpleDateFormat.parse(string);
                    deviceEntity.setExpireDate(date);
                }
            }catch (Exception e){

            }
            String v1 = cells1.get(use_dept).getValue()!=null?cells1.get(use_dept).getValue().toString():"";
            String v2 = cells1.get(team).getValue()!=null?cells1.get(team).getValue().toString():"";
            deviceEntity.setUseDept(v1+v2);
            deviceEntity.setCalibrationNumber(cells1.get(calibration_number).getValue()!=null?cells1.get(calibration_number).getValue().toString():"");
            deviceEntity.setRange(cells1.get(range).getValue()!=null?cells1.get(range).getValue().toString():"");
            deviceEntity.setLevel(cells1.get(level).getValue()!=null?cells1.get(level).getValue().toString():"");
            deviceEntity.setCalibrationParam(cells1.get(calibration_param).getValue()!=null?cells1.get(calibration_param).getValue().toString():"");
            String v3= cells1.get(place).getValue()!=null?cells1.get(place).getValue().toString():"";
            String v4= cells1.get(store_place).getValue()!=null?cells1.get(store_place).getValue().toString():"";
            deviceEntity.setStorePlace(v3+v4);
            deviceEntity.setCalibrationParam(cells1.get(device_admin).getValue()!=null?cells1.get(device_admin).getValue().toString():"");

            deviceEntity.setStatus("0");
            deviceEntity.setDelFlag(0);
            deviceEntity.setCreateTime(new Date());

            LambdaQueryWrapper<TestInstrument>  queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TestInstrument::getCode,code);
            queryWrapper.eq(TestInstrument::getSerialNumber,sn);
            queryWrapper.eq(TestInstrument::getModel,model);
            queryWrapper.eq(TestInstrument::getName,name);
            TestInstrument one = testInstrumentService.getOne(queryWrapper);


            //判断excel设备编号在数据库不存在做插入，
            // 如果存在判断机身出场编号是否存在，如果库和表一致做更新，如果不一致做插入
            if (one == null){
                deviceEntity.setId(snowflakeIdGenerator.nextId());
                list.add(deviceEntity);
            }else {
                deviceEntity.setId(one.getId());
                updateList.add(deviceEntity);
            }
            num1++;
        }
        //保存或者更新数据
        for (DeviceEntity deviceEntity:list){
            deviceEntityMapper.insert(deviceEntity);
        }
        for (DeviceEntity deviceEntity:updateList){
            deviceEntityMapper.updateByIf(deviceEntity);
        }
        System.out.println("仪器设备插入更新成功");
    }

    /**
     * 导出仪器设备
     * @throws Exception
     */
    @GetMapping("exportDevice")
    @Transactional(rollbackFor = Exception.class)
    public void exportDevice(HttpServletResponse response) throws Exception{
        List<DeviceEntity> allDevice = deviceEntityMapper.getAllDevice(null);
        //读取excel
        com.aspose.cells.Workbook workbook1 = new com.aspose.cells.Workbook();
        Worksheet worksheet1 = workbook1.getWorksheets().get(0);
        Cells cells1 = worksheet1.getCells();
        //遍历excel，根据编号查询
        int num1 = 2;
        for (DeviceEntity deviceEntity :allDevice){
            String idIndex = "A"+num1;
            String codeIndex = "B"+num1;
            String fileState = "C" +num1;
            String nameIndex = "D"+num1;
            String modeIndex = "E"+num1;
            String manufacturerIndex = "F"+num1;
            String serial_numberIndex = "G"+num1;
            String price = "H"+num1;
            String purchase_dateIndex = "I"+num1;
            String isIndex = "J"+num1;//是否检定校准
            String affirm_wayIndex = "K"+num1;//确认方式
            String calibration_corporation = "L"+num1;//鉴定校准单位
            String appraisal_date = "M"+num1;//检定校准日期
            String calibration_period = "N"+num1;//检定周期
            String expire_date = "O"+num1;//鉴定校准失效日期
            //合并
            String use_dept = "P"+num1;//设备使用维护部门
            String team = "V"+num1; //使用科室

            String calibration_number = "Q"+num1;//鉴定校准证书编号
            String range = "R"+num1;//里程
            String level = "S"+num1;//精度
            String calibration_param = "T"+num1;
            //合并
            String store_place = "U"+num1;

            String device_admin = "W"+num1;
            cells1.get(idIndex).setValue(deviceEntity.getId());
            cells1.get(codeIndex).setValue(deviceEntity.getCode());
            cells1.get(fileState).setValue(deviceEntity.getFilesState());
            cells1.get(nameIndex).setValue(deviceEntity.getName());
            cells1.get(modeIndex).setValue(deviceEntity.getModel());
            cells1.get(manufacturerIndex).setValue(deviceEntity.getManufacturer());
            cells1.get(serial_numberIndex).setValue(deviceEntity.getSerialNumber());
            cells1.get(price).setValue(deviceEntity.getPrice());
            if (deviceEntity.getPurchaseDate() != null){
                cells1.get(purchase_dateIndex).setValue(DateUtil.formatDate(deviceEntity.getPurchaseDate()));
            }
            cells1.get(isIndex).setValue(deviceEntity.getIsCalibration());
            cells1.get(affirm_wayIndex).setValue(deviceEntity.getAffirmWay());
            cells1.get(calibration_corporation).setValue(deviceEntity.getCalibrationCorporation());
            if (deviceEntity.getAppraisalDate() != null){
                cells1.get(appraisal_date).setValue(DateUtil.formatDate(deviceEntity.getAppraisalDate()));
            }
            cells1.get(calibration_period).setValue(deviceEntity.getCalibrationPeriod());
            if (deviceEntity.getExpireDate() != null){
                cells1.get(expire_date).setValue(DateUtil.formatDate(deviceEntity.getExpireDate()));
            }
            cells1.get(use_dept).setValue(deviceEntity.getUseDept());
            cells1.get(team).setValue("");
            cells1.get(calibration_number).setValue(deviceEntity.getCalibrationNumber());
            cells1.get(range).setValue(deviceEntity.getRange());
            cells1.get(level).setValue(deviceEntity.getLevel());
            cells1.get(calibration_param).setValue(deviceEntity.getCalibrationParam());
            cells1.get(store_place).setValue(deviceEntity.getStorePlace());
            cells1.get(device_admin).setValue(deviceEntity.getDeviceAdmin());
            num1++;
        }
        workbook1.save("D:\\Users\\Administrator\\Desktop\\人员档案\\lims仪器.xlsx");
    }
}

