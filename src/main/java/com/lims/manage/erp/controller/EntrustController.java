package com.lims.manage.erp.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.constant.BucketsConst;
import com.lims.manage.erp.entity.EntrustEntity;
import com.lims.manage.erp.entity.EntrustHistoryEntity;
import com.lims.manage.erp.entity.EntrustHistoryTaskEntity;
import com.lims.manage.erp.entity.SampleEntity;
import com.lims.manage.erp.entity.TaskEntity;
import com.lims.manage.erp.entity.TestCompanyJsonEntity;
import com.lims.manage.erp.entity.TestCustomerJsonEntity;
import com.lims.manage.erp.mapper.EntrustEntityMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.ReportService;
import com.lims.manage.erp.util.AsposeUtil;
import com.lims.manage.erp.util.FileAndFolderUtil;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.*;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/entrust/")
public class EntrustController {

    @Autowired
    private EntrustService entrustService;
    @Autowired
    private LogManagerService logManagerService;
    @Autowired
    private EntrustEntityMapper entrustEntityMapper;
    @Autowired
    private ReportService reportService;

    /**
     * 新增委托 废弃
     *
     * @param json
     * @param file
     * @return
     */
    @RequestMapping("/addEntrust")
    //@RequiresPermissions("entrust:entrust:addEntrust")
    public Result addEntrust(@RequestParam("json") String json, MultipartFile[] file) {
        EntrustAddVo entrust = JSON.parseObject(json, EntrustAddVo.class);
        Boolean isSuccess = entrustService.addEntrust(entrust, file);
        if (isSuccess) {
            return ResultUtil.success();
        } else {
            return ResultUtil.error(678, "新增委托失败！");
        }
    }

    /**
     * 新增委托 使用中丁
     *
     * 丁 7月5日 : 返回字符串效验信息。
     * @param json
     * @param file
     * @return
     */
    @RequestMapping("/addEntrust_Test")
    //@RequiresPermissions("entrust:entrust:addEntrust")
    public Result addEntrustTest(@RequestParam("json") String json, MultipartFile[] file) {
        EntrustAddVo entrust = JSON.parseObject(json, EntrustAddVo.class);
         return ResultUtil.success(entrustService.addEntrustTest0620(entrust, file));
    }


    /**
     * 修改委托 废弃
     *
     * @param json
     * @param file
     * @return
     */
    @RequestMapping("/updateEntrust")
//    @RequiresPermissions("entrust:entrust:updateEntrust")
    public Result updateEntrust(@RequestParam("json") String json, MultipartFile[] file) {
        EntrustAddVo entrust = JSON.parseObject(json, EntrustAddVo.class);
        Boolean isSuccess = entrustService.updateEntrust(entrust, file);
        if (isSuccess) {
            return ResultUtil.success("修改成功");
        } else {
            return ResultUtil.error(678, "修改委托失败！");
        }
    }

    /**
     * 修改委托测试丁 线上使用
     *
     * @param json
     * @param file
     * @return
     */
    @RequestMapping("/updateEntrust_test_new")
//    @RequiresPermissions("entrust:entrust:updateEntrust")
    public Result updateEntrustTestNew(@RequestParam("json") String json, MultipartFile[] file) {
        EntrustAddVo entrust = JSON.parseObject(json, EntrustAddVo.class);
        Boolean isSuccess = entrustService.updateEntrustTestNew(entrust, file);
        if (isSuccess) {
            return ResultUtil.success("修改委托成功");
        } else {
            return ResultUtil.error(678, "修改委托失败！");
        }
    }

    /**
     * 修改委托样品信息
     *
     * @param
     * @return
     */
    @PostMapping("/updateEntrust_test_new_sample")
//    @RequiresPermissions("entrust:entrust:updateEntrust")
    public Result updateEntrustTestNewSample(@RequestBody EntrustAddVo entrust) {
        Boolean isSuccess = entrustService.updateEntrustCheckItem(entrust);
        if (isSuccess) {
            return ResultUtil.success("修改委托下样品成功");
        } else {
            return ResultUtil.error(678, "修改委托下样品失败！");
        }
    }

    /**
     * 废弃委托单信息
     *
     * @return
     */
    @PostMapping("/abandonEntrust")
    public Result abandonEntrust(@RequestBody EntrustEntity entrustEntity) {
        // 获取当前登录用户id。
        Date date = new Date();
        if (ShiroUtils.getUserInfo() != null) {
            entrustEntity.setOperateUser(ShiroUtils.getUserInfo().getUserId());
        }
        entrustEntity.setOperateDate(date);
        return ResultUtil.success(entrustService.abandonEntrust(entrustEntity));
    }

    /**
     * @return
     */
    @GetMapping("get_Basics")
    public Result ReturnBasicsData() {
        return ResultUtil.success(entrustService.returnEntrustData());
    }

    /**
     * @param companyId
     * @return
     */
    @GetMapping("get_entrusted_unit")
    public Result methodDispay(Integer companyId) {
        List<TestCustomerJsonEntity> collectList = entrustService.returnTestCustomerEntityList(companyId);
        if (collectList.isEmpty()) {
            return ResultUtil.error(201, "用户信息不存在");
        }
        return ResultUtil.success(collectList);
    }

    /**
     * 新增客户
     *
     * @param testCompanyEntity
     * @return
     */
    @PostMapping("add_new_company")
    public Result methodPost(@RequestBody TestCompanyJsonEntity testCompanyEntity) {
        if (testCompanyEntity.getCompanyName() != null && testCompanyEntity.getType() != null) {
            if (entrustEntityMapper.getCompanyName(testCompanyEntity.getCompanyName(), Integer.parseInt(testCompanyEntity.getType())) != null) {
                return ResultUtil.error(201, "单位名称已存在");
            }
        } else {
            return ResultUtil.error(201, "缺少必填参数");
        }
        boolean BooleStatus = entrustService.addCompanyData(testCompanyEntity);
        if (BooleStatus) {
            return ResultUtil.success();
        }
        return ResultUtil.error(201, "增加数据失败");
    }

    /**
     * 新增委托单位
     *
     * @param testCompanyEntity
     * @return
     */
    @PostMapping("add_new_company_two")
    public Result methodPostTwo(@RequestBody TestCompanyJsonEntity testCompanyEntity) {
        if (testCompanyEntity.getCompanyName() != null && testCompanyEntity.getType() != null) {
            if (entrustEntityMapper.getCompanyName(testCompanyEntity.getCompanyName(), Integer.parseInt(testCompanyEntity.getType())) != null) {
                return ResultUtil.error(201, "单位名称已存在");
            }
        } else {
            return ResultUtil.error(201, "缺少必填参数");
        }
        boolean BooleStatus = entrustService.addCompanyDataTwo(testCompanyEntity);
        if (BooleStatus) {
            return ResultUtil.success();
        }
        return ResultUtil.error(201, "增加数据失败");
    }

    /**
     * 查询检测项详情：检测项名称，检测项方法，规格型号，检测依据
     *      旧版不带检测依据
     * @param itemIds
     * @return
     */
    @RequestMapping("/getItemDetailOld")
    public Result getItemDetailOld(@RequestBody CheckItemParamVo itemIds) {
        if (itemIds == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            return ResultUtil.success(entrustService.getCheckItemInfoVo(itemIds.getIds()));
        }
    }

    /**
     * 查询检测项 方法 依据
     *
     * @param itemId
     * @return
     */
    @RequestMapping("/getItemMethodStandard")
    public Result getItemMethodStandard(Integer itemId) {
        if (itemId == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            return ResultUtil.success(entrustService.getItemMethodStandard(itemId));
        }
    }

    /**
     * 查询历史委托
     * 1、角色过滤 “客户代表”、“市场部业务员”
     * 2、“客户”指定自身委托
     *
     * @param entrustHistoryEntity
     * @return
     */
    @RequestMapping("/get_entrust_history")
//    @RequiresPermissions("test:entrust:get_entrust_history")
    public Result getEntrustHistoryList(EntrustHistoryEntity entrustHistoryEntity) throws ParseException {
        if (entrustHistoryEntity.getPageNum() == null || entrustHistoryEntity.getPageSize() == null) {
            return ResultUtil.error("缺少分页参数");
        }
        if (entrustHistoryEntity.getState() == null) {
            entrustHistoryEntity.setState(0);
        }
        if (entrustHistoryEntity.getState() != 0 && entrustHistoryEntity.getState() != 144 && entrustHistoryEntity.getState() != 1 && entrustHistoryEntity.getState()!=200 ||  entrustHistoryEntity.getState() ==null) {
            return ResultUtil.error("必填参数状态有误");
        }
        return ResultUtil.success(entrustService.getEntrustHistoryList(entrustHistoryEntity));
    }

    /**
     * 历史委托信息详情查询
     *
     * @param entrustmentId
     * @return
     */
    @RequestMapping("/get_entrust_history_detail")
    public Result getEntrustHistoryDetail(Long entrustmentId) {
        return ResultUtil.success(entrustService.getEntrustHistoryDetail(entrustmentId));
    }

    /**
     * 再来一单（复制委托单详情）
     * @param entrustmentId
     * @return
     */
    @RequestMapping("/get_another_list")
    public Result getEntrustHistoryDetailAnotherList(Long entrustmentId) {
        return ResultUtil.success(entrustService.getAnotherListCopy(entrustmentId));
    }

    /**
     * 分布委托信息详情查询
     *
     * @param entrustmentId
     * @return
     */
    @RequestMapping("/get_entrust_distribution_detail")
    public Result getEntrustDistributionDetail(Long entrustmentId) {
        return ResultUtil.success(entrustService.getEntrustDistributionDetail(entrustmentId));
    }

    /**
     * 根据检测项ID查询可以做该检测项的团队
     *
     * @param checkItemId
     * @return
     */
    @GetMapping("/getDept")
    public Result getDept(Integer checkItemId) {
        return ResultUtil.success(entrustService.getDept(checkItemId));
    }

    /**
     * 查询历史委托信息详情
     *修改委托时 修改样品时 查询详情 线上使用 丁。
     * @param id
     * @return
     */
    @GetMapping("/get_entrust_history_detail_test")
    public Result getEntrustHistoryDetailTest(Long id) {
        if(org.springframework.util.StringUtils.isEmpty(id)){
            return ResultUtil.error("必传参数id = "+id);
        }
        return ResultUtil.success(entrustService.getEntrustHistoryDetailTest(id));
    }

    /**
     * 委托单发布，转为任务
     *
     * @param entity
     * @return
     */
    @PostMapping("publishTask")
    //@RequiresPermissions("entrust:task:publishTask")
    public Result publishTask(@RequestBody TaskEntity entity) {
        if (entity.getEntrustmentId() == null) {
            return ResultUtil.error(-1, "缺少必要参数");
        }
        //核查委托单位、委托人、委托人联系方式、样品信息、检测项信息是否完整
        EntrustAddVo vo = entrustService.getEntrustHistoryDetail(entity.getEntrustmentId());
        if (StringUtils.isEmpty(vo.getEntrustCompany()) || StringUtils.isEmpty(vo.getEntrustPeople())) {
            return ResultUtil.error(-1, "请检查委托人信息是否完整！");
        }
        List<SampleEntity> samples = vo.getSamples();
        if (CollectionUtils.isEmpty(samples)) {
            return ResultUtil.error(-1, "请检查委托单样品信息是否完整！");
        }
        if (!CollectionUtils.isEmpty(samples)) {
            for (SampleEntity sampleEntity : samples) {
                if (CollectionUtils.isEmpty(sampleEntity.getJudgmentBasisVos())) {
                    return ResultUtil.error(-1, "请检查委托单样品下检测项信息是否完整！");
                }
            }
        }
        Boolean flag = entrustService.publishTask(entity);
        if (flag) {
            /*logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"账户："+ShiroUtils.getUserInfo().getUsername()+"发布任务成功编号为："+vo.getEntrustmentNo(),
                    Const.ENTRUST_PUBLISH,true);*/
            return ResultUtil.success("委托发布成功！");
        } else {
            return ResultUtil.error(-1, "委托发布失败！");
        }
    }


    @PostMapping("distributionTask")
    public Result distributionTask(@RequestBody TaskVo entity) {
        if (entity.getEntrustmentId() == null) {
            return ResultUtil.error(-1, "缺少必要参数");
        }
        //核查委托单位、委托人、委托人联系方式、样品信息、检测项信息是否完整
        EntrustAddVo vo = entrustService.getEntrustHistoryDetail(entity.getEntrustmentId());
        if(vo.getState() == 1){
            return ResultUtil.error(-1, "任务已被发布，请重新确认信息！");
        }
        if (StringUtils.isEmpty(vo.getEntrustCompany()) || StringUtils.isEmpty(vo.getEntrustPeople())) {
            return ResultUtil.error(-1, "请检查委托人信息是否完整！");
        }
        List<SampleEntity> samples = vo.getSamples();
        if (CollectionUtils.isEmpty(samples)) {
            return ResultUtil.error(-1, "请检查委托单样品信息是否完整！");
        }
        if (!CollectionUtils.isEmpty(samples)) {
            for (SampleEntity sampleEntity : samples) {
                if (CollectionUtils.isEmpty(sampleEntity.getJudgmentBasisVos())) {
                    return ResultUtil.error(-1, "请检查委托单样品下检测项信息是否完整！");
                }
            }
        }
        if (!CollectionUtils.isEmpty(entity.getCheckItemDeptVoList())) {
            for (CheckItemDeptVo checkItemDeptVo : entity.getCheckItemDeptVoList()) {
                if (checkItemDeptVo.getDeptId() == null) {
                    return ResultUtil.error(-1, "请确认所有检测项是否分配科室！");
                }
            }
        }
        // 下单时间=orderTime (委托单转任务单的时间)
        entity.setOrderTime(new Date(System.currentTimeMillis()));
        //要求完成时间
//        entity.setRequiredCompletionTime(new java.sql.Date(vo.getRequestDate().getTime()));
        // 丁连春：任务单完成时间 以委托单下单时间为准
        entity.setRequiredCompletionTime(vo.getRequestDate());
        Boolean flag = entrustService.distributionTask412(entity);
        if (flag) {
            /*logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"账户："+ShiroUtils.getUserInfo().getUsername()+"发布任务成功编号为："+vo.getEntrustmentNo(),
                    Const.ENTRUST_PUBLISH,true);*/
            return ResultUtil.success("委托分配成功！");
        } else {
            return ResultUtil.error(-1, "委托分配失败！");
        }
    }

    /**
     * 委托单任务待发布列表
     *
     * @param entrustHistoryEntity
     * @return
     */
    @RequestMapping("/releasedList")
//    @RequiresPermissions("test:entrust:releasedList")
    public Result getEntrustReleasedList(EntrustHistoryTaskEntity entrustHistoryEntity) throws ParseException {
        if (entrustHistoryEntity.getPageNum() == null || entrustHistoryEntity.getPageSize() == null) {
            return ResultUtil.error("缺少分页参数");
        }
        return ResultUtil.success(entrustService.getEntrustReleasedList(entrustHistoryEntity));
    }

    /**
     * 委托单下载
     *
     * @param entrustId
     * @return
     */
    @RequestMapping("downloadEntrust")
    public void downloadEntrust(Long entrustId, HttpServletResponse response) {
        String message = entrustService.getMessage();
        String[] strings = message.split("/");
        String fileName = strings[1];
        try {
            MinioClient client = MinIoUtil.minioClient;
            InputStream object = client.getObject(strings[0], fileName);
            //填充数据
            EntrustAddVo detail = entrustService.getEntrustHistoryDetail(entrustId);
            log.debug("====aaa:{}",JSON.toJSONString(detail));
            XWPFDocument document = entrustService.downloadEntrust(detail, object);
            response.reset();
            response.setHeader("Access-Control-Expose-Headers","Content-Disposition");
            response.setContentType("application/x-msdownload");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + detail.getEntrustmentNo()+".docx");
            OutputStream outputStream = response.getOutputStream();
            document.write(outputStream);
            outputStream.close();
        } catch (Exception ex) {
            log.info("导出失败：{}", ex);
        }
    }

    /**
     * 委托预览
     * @param entrustId
     * @param response
     */
    @RequestMapping("previewEntrust")
    public void preview(Long entrustId, HttpServletResponse response) {
        String message = entrustService.getMessage();
        String[] strings = message.split("/");
        String fileName = strings[1];
        try {
            MinioClient client = MinIoUtil.minioClient;
            InputStream object = client.getObject(strings[0], fileName);
            //填充数据
            //校验是否是委托单id
            Long id = entrustService.checkEntrustId(entrustId);
            if (id == null){
                Long entrustIdById = reportService.getEntrustIdById(entrustId);
                entrustId = entrustIdById;
            }
            EntrustAddVo detail = entrustService.getEntrustHistoryDetail(entrustId);
            log.debug("====aaa:{}",JSON.toJSONString(detail));
            XWPFDocument document = entrustService.downloadEntrust(detail, object);
            //相应pdf
            ByteArrayOutputStream b1 = AsposeUtil.word2pdf4(document);
            InputStream inputStream = FileAndFolderUtil.parseOut(b1);
            ServletOutputStream outputStream = response.getOutputStream();
            int i = IOUtils.copy(inputStream, outputStream);   // copy流数据,i为字节数
            inputStream.close();
            outputStream.close();
        } catch (Exception ex) {
            log.info("导出失败：", ex.getMessage());
        }
    }

    /**
     * 查询委托单位上一次工程名称、工程部位
     *
     * @param name
     * @return
     */
    @GetMapping("/getHistoryData")
    public Result getHistoryData(String name) {
        if (name == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            HistoryEntrustDataVo historyData = entrustService.getHistoryData(name);
            if (historyData == null) {
                historyData = new HistoryEntrustDataVo();
            }
            return ResultUtil.success(historyData);
        }
    }


    /**
     * 查询委托单位上一次项目名称、部位
     * 包括 unitData 单位联系人集合
     *
     * @param companyName
     * @param type
     * @return
     */
    @GetMapping("/getHistoryData_two")
    public Result getHistoryDataTwo(String companyName, String type) {
        if (companyName == null || type == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            HistoryEntrustDataVo historyData = entrustService.getHistoryData(companyName, Integer.parseInt(type));
            if (historyData == null) {
                historyData = new HistoryEntrustDataVo();
            }
            return ResultUtil.success(historyData);
        }
    }

    /**
     * 查询产品所有的检测项及检测项的检测依据
     *
     * @param productId
     * @return
     */
    @RequestMapping("/getCheckItemBasis")
    public Result getCheckItemBasis(Integer productId) {
        if (productId == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            return ResultUtil.success(entrustService.getCheckItemBasis(productId));
        }
    }

    /**
     * 查询检测项详情：检测项名称，检测项方法，规格型号，检测依据
     *
     * @param itemIds
     * @return
     */
    @RequestMapping("/getItemDetail")
    public Result getItemDetail(@RequestBody CheckItemParamVo itemIds) {
        if (itemIds == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            return ResultUtil.success(entrustService.getCheckItemInfo(itemIds.getIds()));
        }
    }

    /**
     *
     * @param entrustmentId
     * @return
     */
    @GetMapping("/getReportTeams")
    public Result getReportTeams(Long entrustmentId) {
        if (entrustmentId == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            List<LabelValueVo> reportTeams = entrustService.getReportTeams(entrustmentId);
            if(CollectionUtils.isEmpty(reportTeams)){
                return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), "未找到当前委托分配的团队信息！");
            }
            return ResultUtil.success(reportTeams);
        }
    }

    @RequestMapping("/updateReportTeam")
//    public Result updateReportTeam(@RequestParam(value = "entrustmentId") Long entrustmentId,@RequestParam(value = "deptIds") List<Integer> deptIds) {
    public Result updateReportTeam(@RequestBody UpdateIssueReportVo vo) {
        if (vo.getEntrustmentId() == null || vo.getDeptIds() == null || vo.getDeptIds().size()<1) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            int i = entrustService.updateReportTeam(vo.getEntrustmentId(), vo.getDeptIds());
            if(i<1){
                return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), "修改出报告科室失败！");
            }
            return ResultUtil.success("修改出报告科室成功！");
        }
    }

    /**
     * 新增委托_（针对 再来一单的数据保存）
     *
     * @param json
     * @param file
     * @return
     */
    @RequestMapping("/addEntrust_copy")
    public Result addEntrustCopy(@RequestParam("json") String json, MultipartFile[] file) {
        EntrustAddVo entrust = JSON.parseObject(json, EntrustAddVo.class);
        return ResultUtil.success(entrustService.addEntrustCopy(entrust, file));
    }

    /**
     * 查询任务来源
     * @return
     */
    @GetMapping("/getTaskSource")
    public Result getTaskSource() {
        String[] arr = {"省内","云南","甘肃","广西","新疆","西藏","杭州","江西","安徽","检测七所","生产管理办"};
        List<LabelValueVo> taskSource = Lists.newArrayList();
        for (int i = 0; i < arr.length; i++) {
            Long value = 1L+i;
            LabelValueVo vo = new LabelValueVo();
            vo.setValue(value);
            vo.setLabel(arr[i]);
            taskSource.add(vo);
        }
        return ResultUtil.success(taskSource);
    }

    /**
     * 委托是否发布
     * @param entrustId
     * @return
     */
    @GetMapping("/isPublish")
    public Result isPublish(Long entrustId) {
        if (entrustId == null) {
            return ResultUtil.error(ResultEnum.VERIFY_FAIL_NINE.getCode(), ResultEnum.VERIFY_FAIL_NINE.getMsg());
        } else {
            Boolean publish = entrustService.isPublish(entrustId);
            return ResultUtil.success(publish);
        }
    }
}
