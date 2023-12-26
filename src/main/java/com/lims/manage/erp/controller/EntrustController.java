package com.lims.manage.erp.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aspose.words.Document;
import com.aspose.words.ImportFormatMode;
import com.aspose.words.SaveFormat;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.*;
import com.lims.manage.erp.mapper.EntrustEntityMapper;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultEnum;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.EntrustService;
import com.lims.manage.erp.service.LogManagerService;
import com.lims.manage.erp.service.ReportService;
import com.lims.manage.erp.util.AsposeUtil;
import com.lims.manage.erp.util.DateUtil;
import com.lims.manage.erp.util.DingNotifyUtils;
import com.lims.manage.erp.util.FileAndFolderUtil;
import com.lims.manage.erp.util.MinIoUtil;
import com.lims.manage.erp.util.ShiroUtils;
import com.lims.manage.erp.vo.CheckItemDeptVo;
import com.lims.manage.erp.vo.CheckItemParamVo;
import com.lims.manage.erp.vo.ClientOrderdetailVo;
import com.lims.manage.erp.vo.EntrustAddVo;
import com.lims.manage.erp.vo.HistoryEntrustDataVo;
import com.lims.manage.erp.vo.LabelValueVo;
import com.lims.manage.erp.vo.TaskVo;
import com.lims.manage.erp.vo.TestEntrustedTaskRelVo;
import com.lims.manage.erp.vo.UpdateIssueReportVo;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sun.security.util.Debug;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
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
    @Autowired
    private QiYueSuoEntity qiYueSuoEntity;
    @Autowired
    private DingNotifyUtils dingNotifyUtils;
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
    public Result addEntrustTest(@RequestParam("jsonParam") String jsonParam, MultipartFile[] file) {
        try {
            EntrustAddVo entrust = JSON.parseObject(jsonParam, EntrustAddVo.class);
            return ResultUtil.success(entrustService.addEntrustTest0620(entrust, file));
        }
        catch (Exception e){
            // 日志输出。
            Debug.println("新增委托日志异常输出\t",e+"");
            return ResultUtil.error("新建委托失败,请联系管理员！！！");
        }

    }

    /**
     * 预委托
     * @param json
     * @param file
     * @return
     */
    @RequestMapping("/addPreEntrust")
    public Result addPreEntrust(@RequestParam("json") String json, MultipartFile[] file) {
        try {
            EntrustAddVo entrust = JSON.parseObject(json, EntrustAddVo.class);
            return ResultUtil.success(entrustService.addPreEntrust(entrust, file));
        }
        catch (Exception e){
            // 日志输出。
            Debug.println("新增委托日志异常输出\t",e+"");
            return ResultUtil.error("新建委托失败,请联系管理员！！！");
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
    public Result updateEntrustTestNew(@RequestParam("json") String json, MultipartFile[] file) throws ParseException {
        EntrustAddVo entrust = JSON.parseObject(json, EntrustAddVo.class);
        // 通过委托单状态 auditState（未受理）不能修改信息
        PageHelper.clearPage();
        if(!entrustService.efficacyState(entrust.getId())){
            return ResultUtil.error(678, "修改委托失败！委托单未受理");
        }
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
        // 通过委托单状态 auditState（未受理）不能修改信息
        PageHelper.clearPage();
        if(!entrustService.efficacyState(entrust.getId())){
            return ResultUtil.error(678, "修改委托失败！委托单未受理");
        }
        // 通过委托单id 效验样品id 是否存在
        if(!CollectionUtils.isEmpty(entrust.getSamples())){
            if(!entrustService.verifySampleIsUsed(entrust.getId(),entrust.getSamples())){
                return ResultUtil.error(678, "修改失败！！！样品数据已经被绑定");
            }
        }
        String strSuccess = entrustService.updateEntrustCheckItem(entrust);
        if (strSuccess!=null) {
            // 业务是：如果原任务单全部参数被删除，原任务单作废。
            // 任务单作废 流转信息删除
            entrustService.verifyTaskListExists(entrust.getId());
            return ResultUtil.success(strSuccess);
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
            PageHelper.clearPage();
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

//    /**
//     * 委托单发布，转为任务
//     *
//     * @param entity
//     * @return
//     */
//    @PostMapping("publishTask")
//    //@RequiresPermissions("entrust:task:publishTask")
//    public Result publishTask(@RequestBody TaskEntity entity) {
//        if (entity.getEntrustmentId() == null) {
//            return ResultUtil.error(-1, "缺少必要参数");
//        }
//        //核查委托单位、委托人、委托人联系方式、样品信息、检测项信息是否完整
//        EntrustAddVo vo = entrustService.getEntrustHistoryDetail(entity.getEntrustmentId());
//        if (StringUtils.isEmpty(vo.getEntrustCompany()) || StringUtils.isEmpty(vo.getEntrustPeople())) {
//            return ResultUtil.error(-1, "请检查委托人信息是否完整！");
//        }
//        List<SampleEntity> samples = vo.getSamples();
//        if (CollectionUtils.isEmpty(samples)) {
//            return ResultUtil.error(-1, "请检查委托单样品信息是否完整！");
//        }
//        if (!CollectionUtils.isEmpty(samples)) {
//            for (SampleEntity sampleEntity : samples) {
//                if (CollectionUtils.isEmpty(sampleEntity.getJudgmentBasisVos())) {
//                    return ResultUtil.error(-1, "请检查委托单样品下检测项信息是否完整！");
//                }
//            }
//        }
//        Boolean flag = entrustService.publishTask(entity);
//        if (flag) {
//            /*logManagerService.addOpSysLog(ShiroUtils.getUserInfo(),"账户："+ShiroUtils.getUserInfo().getUsername()+"发布任务成功编号为："+vo.getEntrustmentNo(),
//                    Const.ENTRUST_PUBLISH,true);*/
//            return ResultUtil.success("委托发布成功！");
//        } else {
//            return ResultUtil.error(-1, "委托发布失败！");
//        }
//    }


//    @PostMapping("distributionTask")
//    public Result distributionTask(@RequestBody TaskVo entity) {
//        if (entity.getEntrustmentId() == null) {
//            return ResultUtil.error( "缺少必要参数");
//        }
//        //核查委托单位、委托人、委托人联系方式、样品信息、检测项信息是否完整
//        EntrustAddVo vo = entrustService.getEntrustHistoryDetail(entity.getEntrustmentId());
//        if(vo.getState() == 1){
//            return ResultUtil.error("任务已被发布，请重新确认信息！");
//        }
//        if (StringUtils.isEmpty(vo.getEntrustCompany()) || StringUtils.isEmpty(vo.getEntrustPeople())) {
//            return ResultUtil.error( "请检查委托人信息是否完整！");
//        }
//        List<SampleEntity> samples = vo.getSamples();
//        if (CollectionUtils.isEmpty(samples)) {
//            return ResultUtil.error( "请检查委托单样品信息是否完整！");
//        }
//        if (!CollectionUtils.isEmpty(samples)) {
//            for (SampleEntity sampleEntity : samples) {
//                if (CollectionUtils.isEmpty(sampleEntity.getJudgmentBasisVos())) {
//                    return ResultUtil.error("请检查委托单样品下检测项信息是否完整！");
//                }
//            }
//        }
//        if (CollectionUtils.isEmpty(entity.getCheckItemDeptVoList())) {
//             return ResultUtil.error( "检测项为空不能发布！！！");
//        }
//        if (!CollectionUtils.isEmpty(entity.getCheckItemDeptVoList())) {
//            for (CheckItemDeptVo checkItemDeptVo : entity.getCheckItemDeptVoList()) {
//                if (checkItemDeptVo.getDeptId() == null) {
//                    return ResultUtil.error( "请确认所有检测项是否分配科室！");
//                }
//            }
//        }
//        // 效验： 任务发布时指向任务单A提示A任务单已完成，分配失败。
//        Boolean status = entrustService.verifyDistributionTask(entity);
//        if(!status){
//            return ResultUtil.error( "分配失败！！！任务单状态已完成试验。");
//        }
//        // 丁连春：任务单完成时间 以委托单下单时间为准
//        entity.setRequiredCompletionTime(vo.getRequestDate());
//        // 任务单下单日期等于委托单受理日期
//        entity.setOrderTime(vo.getAcceptanceDate());
//        // 任务单提供资料等于委托单提供资料
//        if(!org.springframework.util.StringUtils.isEmpty(vo.getPresentInformation())){
//            entity.setPresentInformation(vo.getPresentInformation());
//        }else {
//            entity.setPresentInformation("--");
//        }
//        Boolean flag = entrustService.distributionTask320(entity);
//        if (flag) {
//            return ResultUtil.success("委托分配成功！");
//        } else {
//            return ResultUtil.error( "委托分配失败！");
//        }
//    }

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
        EntrustAddVo detail = entrustService.getEntrustHistoryDetail(entrustId);
        String message = entrustService.getMessage();
        String[] strings = message.split("/");
        String fileName = strings[1];
        //20230314及之前的单子，单位名称用老的BD20210021-old.docx
        String dayString = DateUtil.getDayString(detail.getAcceptanceDate().getTime());
        if (Integer.parseInt(dayString)<20230313){
            fileName = "BD20210021-old.docx";
        }
        //2023七月1号之后用新的委托模板
        if (Integer.parseInt(dayString)>= 20230801){
            fileName = "033检验委托单.docx";
        }
        XWPFDocument document = null;
        try {
            MinioClient client = MinIoUtil.minioClient;
            InputStream object = client.getObject(strings[0], fileName);
            //填充数据
            log.debug("====aaa:{}",JSON.toJSONString(detail));
            if ("033检验委托单.docx".equals(fileName)){
                document = entrustService.downloadEntrustNew(detail, object);
            }else {
                document = entrustService.downloadEntrust(detail, object);
            }
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
        //校验是否是委托单id
        Long id = entrustService.checkEntrustId(entrustId);
        if (id == null){
            Long entrustIdById = reportService.getEntrustIdById(entrustId);
            entrustId = entrustIdById;
        }
        EntrustAddVo detail = entrustService.getEntrustHistoryDetail(entrustId);
        String message = entrustService.getMessage();
        String[] strings = message.split("/");
        String fileName = strings[1];
        //20230314及之前的单子，单位名称用老的BD20210021-old.docx
        String dayString = DateUtil.getDayString(detail.getAcceptanceDate()==null?System.currentTimeMillis():detail.getAcceptanceDate().getTime());
        if (Integer.parseInt(dayString)<20230313){
            fileName = "BD20210021-old.docx";
        }
        //2023七月1号之后用新的委托模板
        if (Integer.parseInt(dayString)>= 20230801){
            fileName = "033检验委托单.docx";
        }
        XWPFDocument document = null;
        try {
            MinioClient client = MinIoUtil.minioClient;
            InputStream object = client.getObject(strings[0], fileName);
            //填充数据
            log.debug("====aaa:{}",JSON.toJSONString(detail));
            if ("033检验委托单.docx".equals(fileName)){
                document = entrustService.downloadEntrustNew(detail, object);
            }else {
                document = entrustService.downloadEntrust(detail, object);
            }
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
            if(CollectionUtils.isEmpty(itemIds.getIds())){
                return ResultUtil.success(new ArrayList<>());
            }
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
    @RequestMapping("/addEntrust_copy_old")
    public Result addEntrust_copy_old(@RequestParam("json") String json, MultipartFile[] file) {
        try {
            EntrustAddVo entrust = JSON.parseObject(json, EntrustAddVo.class);
            return ResultUtil.success(entrustService.addEntrustCopy(entrust, file));
        }
        catch (Exception e){
            // 日志输出。
            Debug.println("新增委托再来一单日志异常输出\t",e+"");
            return ResultUtil.error("再来一单新建委托失败,请联系管理员！！！");
        }

    }

    /**
     * 保存再来一单
     * @param json
     * @param file
     * @return
     */
    @RequestMapping("/addEntrust_copy")
    public Result addEntrustCopy(@RequestParam("json") String json, MultipartFile[] file) {
        try {
            EntrustAddVo entrust = JSON.parseObject(json, EntrustAddVo.class);
            return ResultUtil.success(entrustService.addEntrustCopy1016(entrust, file));
        }
        catch (Exception e){
            // 日志输出。
            Debug.println("新增委托再来一单日志异常输出\t",e+"");
            return ResultUtil.error("再来一单新建委托失败,请联系管理员！！！");
        }

    }

    /**
     * 查询任务来源
     * @return
     */
    @GetMapping("/getTaskSource")
    public Result getTaskSource() {
        String[] arr = {"省内","云南","甘肃","广西","新疆","西藏","杭州","江西","安徽","检测七所","生产管理办","宁夏","比对试验","模拟试验"};
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

    /**
     * 委托单附件单个上传
     */
    @RequestMapping("/uploading/{id}")
    public Result uploading(@PathVariable("id") Long id, MultipartFile[] file) {
        if (id == null && "".equals(id)) {
            return ResultUtil.error("缺少必填参数！");
        }
        if (file.length == 0) {
            return ResultUtil.error("委托file文件为空！");
        }
        for(MultipartFile multipartFile:file){
            entrustService.uploading(id,multipartFile);
        }
        return ResultUtil.error("样品文件上传成功");
    }

    /**
     * 删除文件id
     */
    @RequestMapping("/removeding/{id}")
    public Result removeding(@PathVariable("id") Integer id) {
        entrustService.removeding(id);
        return ResultUtil.success("样品文件删除成功");
    }

    /**
     * 修改任务流转要求
     */
    @PostMapping("/updateTestEntrustedTaskRelEntity")
    public Result updateTestEntrustedTaskRelEntity(@RequestBody TestEntrustedTaskRelEntity testEntrustedTaskRelEntity){
        if(testEntrustedTaskRelEntity.getId()==null){
            return ResultUtil.error("缺少必填参数！");
        }
        // 操作人id 与name 为空
        testEntrustedTaskRelEntity.setUserId(null);
        testEntrustedTaskRelEntity.setAddressName(null);
        entrustService.updateTestEntrustedTaskRelEntity(testEntrustedTaskRelEntity);
        return ResultUtil.success("修改任务流转要求成功！！！");
    }
    /**
     * 删除任务流转要求
     */
    @GetMapping("/removeTestEntrustedTask/{id}")
    public Result removeTestEntrustedTask(@PathVariable("id") Integer id) {
        entrustService.removeTestEntrustedTask(id);
        return ResultUtil.success("删除任务流转要求成功！！！");
    }

    /**
     * 新增任务流转要求
     */
    @PostMapping("/addTestEntrustedTaskRelEntity")
    public Result addTestEntrustedTaskRelEntity(@RequestBody TestEntrustedTaskRelEntity testEntrustedTaskRelEntity){
        if(testEntrustedTaskRelEntity.getEntrustId()==null){
            return ResultUtil.error("缺少必填参数！");
        }
        // 操作人id 与name 存入
        SysUserEntity userEntity = ShiroUtils.getUserInfo();
        testEntrustedTaskRelEntity.setUserId(userEntity.getUserId());
        testEntrustedTaskRelEntity.setAddressName(userEntity.getName());
        entrustService.addTestEntrustedTaskRelEntity(testEntrustedTaskRelEntity);
        return ResultUtil.success("新增任务流转要求成功！！！");
    }

    /**
     * 通过委托单id 获取流转单信息集合
     * @param id 委托单id
     * @return
     */
    @GetMapping("/getEntrustTaskRelList/{id}")
    public Result getEntrustTaskRelList(@PathVariable("id") Long id) {
        return ResultUtil.success(entrustService.getEntrustTaskRelList(id));
    }

    /**
     * 支持批量修改
     */
    @PostMapping("/updateEntrustedTaskRelEntityList")
    public Result updateEntrustedTaskRelEntityList(@RequestBody List<TestEntrustedTaskRelEntity> list){
       if(CollectionUtils.isEmpty(list)){
           return ResultUtil.error("批量修改数据集不能为空");
       }
        // 操作人id 与name null
       for(TestEntrustedTaskRelEntity testEntrustedTaskRelEntity:list){
           testEntrustedTaskRelEntity.setUserId(null);
           testEntrustedTaskRelEntity.setAddressName(null);
           testEntrustedTaskRelEntity.setUpdateDate(new Date());
       }
        entrustService.updateEntrustedTaskRelEntityList(list);
        return ResultUtil.success("批量修改成功！！！");
    }

    /**
     * 当天任务统计
     */
    @GetMapping("/getTaskStatisticsList")
    public Result taskStatisticsList(TestEntrustedTaskRelVo testEntrustedTaskRelVo){
        if (testEntrustedTaskRelVo.getPageNum() == null || testEntrustedTaskRelVo.getPageSize() == null) {
            return ResultUtil.error("缺少分页参数");
        }
        return ResultUtil.success(entrustService.taskStatisticsList2(testEntrustedTaskRelVo));
    }

    /**
     * 客户委托查询
     */
    @GetMapping("/getClientList")
    public Result getClientList(ClientOrderdetailVo clientOrderdetailVo){
        if (clientOrderdetailVo.getPageNum() == null || clientOrderdetailVo.getPageSize() == null ) {
            return ResultUtil.error("缺少分页参数或必填参数");
        }
        return ResultUtil.success(entrustService.getClientList(clientOrderdetailVo));
    }
    /**
     * 客户委托查询-统计价格
     */
    @GetMapping("/getClientListSumPrice")
    public Result getClientListSumPrice(ClientOrderdetailVo clientOrderdetailVo){

        return entrustService.getClientListSumPrice(clientOrderdetailVo);
    }

    /**
     * 客户委托查询 导出
     * @param clientOrderdetailVo
     * @param response
     * @throws IOException
     */
    @GetMapping("/getClientListExport")
    public void getClientListExport(ClientOrderdetailVo clientOrderdetailVo,HttpServletResponse response) throws Exception {
        clientOrderdetailVo.setPageNum(1);
        clientOrderdetailVo.setPageSize(100000);
        BufferedOutputStream bos = null;
        String fileName = "企业委托单详情表"+DateUtil.formatDate(new Date());
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;fileName=" +  java.net.URLEncoder.encode(fileName+".xlsx", "UTF-8") );
        PageInfo pageInfo = entrustService.getClientListExport(clientOrderdetailVo);
        List<ClientOrderdetailVo> list = Lists.newArrayList();
        if(!CollectionUtils.isEmpty(pageInfo.getList())){
            System.out.println(pageInfo.getList().size());
            list =  pageInfo.getList();
        }
        InputStream inputStream = entrustService.exportPersonDetails(list,clientOrderdetailVo);
        ServletOutputStream outputStream = response.getOutputStream();
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        bos = new BufferedOutputStream(outputStream);
        byte[] buff = new byte[2048];
        int bytesRead;
        while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
            bos.write(buff, 0, bytesRead);
            bos.flush();
        }
        bos.close();
    }

    /**
     * 对客户下的委托单进行受理
     */
    @GetMapping("acceptEntrust")
    public Result acceptEntrust(Long id){
        if (id == null){
            return ResultUtil.error("缺少必要参数");
        }
        boolean b = entrustService.acceptEntrust(id);
        if (b){
            return ResultUtil.success("受理成功");
        }else {
            return ResultUtil.error("受理失败");
        }
    }

    @GetMapping("exportAllEntrust")
    public void exportAllEntrust() throws Exception{

        String message = entrustService.getMessage();
        String[] strings = message.split("/");
        String fileName = strings[1];
        //获取所有符合条件的委托单id
        List<EntrustAddVo> list = entrustEntityMapper.getAllEntrustIdBySearch();
        List<EntrustAddVo> list1 = Lists.newArrayList();
        for (EntrustAddVo bean:list) {
            int intValue = bean.getEntrustmentNo().intValue();
            if (intValue >= 202207001){
                if (intValue < 2022070060){
                    list1.add(bean);
                }
            }
        }

        List<EntrustAddVo> noList = Lists.newArrayList();
        List<EntrustAddVo> yesList = Lists.newArrayList();
        for (EntrustAddVo bean:list1) {
            if (bean.getState() == 0){
                noList.add(bean);
            }else {
                if (StringUtils.isEmpty(bean.getEntrustCategoryType())){
                    yesList.add(bean);
                }

            }
        }
        /*for (EntrustAddVo bean:noList) {
            String path = "D:\\AAno\\"+bean.getEntrustmentNo()+".docx";
            try {
                FileOutputStream outputStream = new FileOutputStream(path);
                MinioClient client = MinIoUtil.minioClient;
                InputStream object = client.getObject(strings[0], fileName);
                //填充数据
                EntrustAddVo detail = entrustService.getEntrustHistoryDetail(bean.getId());
                XWPFDocument document = entrustService.downloadEntrust(detail, object);
                document.write(outputStream);
                outputStream.close();
                Thread.sleep(10);
            } catch (Exception ex) {
                log.info("导出失败：{}", ex);
            }
        }*/
        for (EntrustAddVo bean:yesList) {
            String path = "D:\\AAnono\\"+bean.getEntrustmentNo()+".docx";
            try {
                FileOutputStream outputStream = new FileOutputStream(path);
                MinioClient client = MinIoUtil.minioClient;
                InputStream object = client.getObject(strings[0], fileName);
                //填充数据
                EntrustAddVo detail = entrustService.getEntrustHistoryDetail(bean.getId());
                XWPFDocument document = entrustService.downloadEntrust(detail, object);
                document.write(outputStream);
                outputStream.close();
                Thread.sleep(20);
            } catch (Exception ex) {
                log.info("导出失败：{}", ex);
            }
        }
    }

    @GetMapping("merge")
    public void mergeDoc(){
        List<File> fileList = getAllFiles("D:\\AAnono");
        try{
            Document doc3 = new Document();
            FileOutputStream fos = new FileOutputStream(new File("D:\\Merge\\merge.docx"));
            doc3.removeAllChildren();
            for (File file:fileList) {
                FileInputStream fileInputStream = new FileInputStream(file);
                Document document = new Document(fileInputStream);
                doc3.appendDocument(document,ImportFormatMode.USE_DESTINATION_STYLES);
            }
            doc3.save(fos, SaveFormat.DOCX);
            log.info("委托单合并完成");
            fos.close();
        }catch(Exception e){
            log.error("合并委托单失败：{}",e);
        }
    }

    /**
     * 列出目录下的所有文件.
     * @param path
     * @return
     */

    private static List<File> getAllFiles(String path) {
        List<File> files = new ArrayList<File>();
        if (!StringUtils.isNotEmpty(path)) {
            return files;
        }
        File root = new File(path);
        if (root.exists()) {
            if (root.isDirectory()) {
                File[] childFiles = root.listFiles();
                for (File childFile : childFiles) {
                    files.addAll(getAllFiles(childFile.getAbsolutePath()));
                }
            } else {
                files.add(root);
            }
        }
        return files;
    }

    /**
     * 委托创建、修改、再来一单 （提供部门信息来源接口 ）
     *
     * @return
     */
    @GetMapping("get_issue_dept")
    public Result getIssueDept() {
        return ResultUtil.success(entrustEntityMapper.getIssueDept());
    }

    @GetMapping("exportPublishEntrust")
    public void exportPublishEntrust() throws Exception{
        String message = entrustService.getMessage();
        String[] strings = message.split("/");
        String fileName = strings[1];
        //七所12月份已发布
        List<EntrustAddVo> list7 = entrustEntityMapper.get7Infos1();
        //八所12月份已发布
        List<EntrustAddVo> list8 = entrustEntityMapper.get8Infos1();
        //交通所12月份已发布
        List<EntrustAddVo> listJt = entrustEntityMapper.getJtInfos1();
        for (EntrustAddVo bean:list7) {
            String path = "D:\\doc\\saveOriginalRecord\\7所-12月\\"+bean.getEntrustmentNo()+".docx";
            try {
                FileOutputStream outputStream = new FileOutputStream(path);
                MinioClient client = MinIoUtil.minioClient;
                InputStream object = client.getObject(strings[0], fileName);
                //填充数据
                EntrustAddVo detail = entrustService.getEntrustHistoryDetail(bean.getId());
                XWPFDocument document = entrustService.downloadEntrust(detail, object);
                document.write(outputStream);
                outputStream.close();
                Thread.sleep(20);
            } catch (Exception ex) {
                log.info("导出失败：{}", ex);
            }
        }
        for (EntrustAddVo bean:list8) {
            String path = "D:\\doc\\saveOriginalRecord\\8所-12月\\"+bean.getEntrustmentNo()+".docx";
            try {
                FileOutputStream outputStream = new FileOutputStream(path);
                MinioClient client = MinIoUtil.minioClient;
                InputStream object = client.getObject(strings[0], fileName);
                //填充数据
                EntrustAddVo detail = entrustService.getEntrustHistoryDetail(bean.getId());
                XWPFDocument document = entrustService.downloadEntrust(detail, object);
                document.write(outputStream);
                outputStream.close();
                Thread.sleep(20);
            } catch (Exception ex) {
                log.info("导出失败：{}", ex);
            }
        }

        for (EntrustAddVo bean:listJt) {
            String path = "D:\\doc\\saveOriginalRecord\\交通所-12月\\"+bean.getEntrustmentNo()+".docx";
            try {
                FileOutputStream outputStream = new FileOutputStream(path);
                MinioClient client = MinIoUtil.minioClient;
                InputStream object = client.getObject(strings[0], fileName);
                //填充数据
                EntrustAddVo detail = entrustService.getEntrustHistoryDetail(bean.getId());
                XWPFDocument document = entrustService.downloadEntrust(detail, object);
                document.write(outputStream);
                outputStream.close();
                Thread.sleep(20);
            } catch (Exception ex) {
                log.info("导出失败：{}", ex);
            }
        }
    }

    /**
     * 委托单撤回
     * @param entrustId
     * @return
     */
    @GetMapping("entrustRevocation")
    public Result entrustRevocation(Long entrustId) {
        // 委托单id = null 返回失败
        if(org.springframework.util.StringUtils.isEmpty(entrustId)){
            return ResultUtil.error("委托单id不能为空");
        }
        // 获取委托状态
        EntrustAddVo entrustData = entrustEntityMapper.selectByKeyId(entrustId);
        if (entrustData == null) {
            return ResultUtil.error("撤回失败：委托单不存在");
        }
        if (entrustData.getState() != null && entrustData.getState() == 201) {
            return ResultUtil.error("撤回失败：预委托单不能撤回");
        }
        // 根据委托单查询任务单状态
        List<TaskTestEntity> taskList = entrustEntityMapper.selectTaskTestEntityList(entrustId);
        // 委托单下 任务单不为空的话
        if(CollectionUtil.isNotEmpty(taskList)){
            // 效验任务单 是否开始试验。
            Boolean flag = entrustService.verifyTaskState(taskList);
            if(flag){
                return ResultUtil.error("撤回失败：任务单已开始试验");
            }
        }
        // 进行 撤回操作
        entrustService.entrustRevocation(taskList,entrustId);
        return ResultUtil.success("撤回成功");
    }

    @GetMapping("test")
    public void test(HttpServletResponse response) throws Exception{
        FileInputStream fileInputStream = new FileInputStream("C:\\Users\\Administrator\\Music\\test.docx");
        XWPFDocument doc = new XWPFDocument(fileInputStream);
        List<XWPFTable> tables = doc.getTables();
        XWPFTable table = tables.get(0);
        //设置字体
        List<XWPFTableRow> tableRows = table.getRows();
        for (int r =0;r<tableRows.size();r++) {
            List<XWPFTableCell> tableCells = tableRows.get(r).getTableCells();
            for (int k =0;k<tableCells.size();k++) {
                List<XWPFParagraph> paragraphs = tableCells.get(k).getParagraphs();
                for (int d=0;d<paragraphs.size();d++){
                    XWPFParagraph paragraph = paragraphs.get(d);
                    XWPFRun run = paragraph.createRun();
                    run.setFontFamily("宋体");
                    run.setFontSize(10);
                }
            }
        }
        List<XWPFTableRow> rows = table.getRows();
        rows.get(0).getTableCells().get(1).setText("No：" + 20230806);
        rows.get(0).getTableCells().get(1).setText("GB123456-11");
        rows.get(0).getTableCells().get(1).setText("样品");
        rows.get(1).getTableCells().get(1).setText("张三");
        rows.get(1).getTableCells().get(1).setText("李四");
        response.reset();
        response.setHeader("Access-Control-Expose-Headers","Content-Disposition");
        response.setContentType("application/x-msdownload");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;fileName=" +"test001.docx");
        OutputStream outputStream = response.getOutputStream();
        doc.write(outputStream);
        outputStream.close();
    }

    /**
     * 获取委托经营人员信息
     * @param entrustId
     * @return
     */
    @GetMapping("operatingPersonnel")
    public Result operatingPersonnel(Long entrustId){
        if (entrustId == null){
            return ResultUtil.error("缺少参数");
        }
        JSONObject jsonObject = entrustService.operatingPersonnel(entrustId);
        return ResultUtil.success(jsonObject);
    }

    /**
     * 审核发布-驳回委托单
     * @param entrustId
     * @return
     */
    @GetMapping("entrustReviewRejection")
    public Result entrustReviewRejection(Long entrustId , String content) {
        // 委托单id = null 返回失败
        if(org.springframework.util.StringUtils.isEmpty(entrustId)){
            return ResultUtil.error("委托单id不能为空");
        }
        // 审核发布-驳回委托单
        return entrustService.entrustReviewRejection(entrustId,content);
    }
    /**
     * 审核发布-审核通过
     * @param entrustId
     * @return
     */
    @GetMapping("entrustApproved")
    public Result entrustApproved(Long entrustId) {
        // 委托单id = null 返回失败
        if(org.springframework.util.StringUtils.isEmpty(entrustId)){
            return ResultUtil.error("委托单id不能为空");
        }
        // 审核发布-审核通过
        return entrustService.entrustApproved(entrustId ,1);
    }

    /**
     * 审核并发布
     * @param entity
     * @return
     */
    @PostMapping("distributionTask")
    public Result distributionTask(@RequestBody TaskVo entity) {
        if (entity.getEntrustmentId() == null) {
            return ResultUtil.error( "缺少必要参数");
        }
        //核查委托单位、委托人、委托人联系方式、样品信息、检测项信息是否完整
        EntrustAddVo vo = entrustService.getEntrustHistoryDetail(entity.getEntrustmentId());
        if(vo.getState() == 1){
            return ResultUtil.error("任务已被发布，请重新确认信息！");
        }
        if (StringUtils.isEmpty(vo.getEntrustCompany()) || StringUtils.isEmpty(vo.getEntrustPeople())) {
            return ResultUtil.error( "请检查委托人信息是否完整！");
        }
        // 丁连春：任务单完成时间 以委托单下单时间为准
        entity.setRequiredCompletionTime(vo.getRequestDate());
        // 任务单下单日期等于委托单受理日期
        entity.setOrderTime(vo.getAcceptanceDate());
        // 任务单提供资料等于委托单提供资料
        if(!org.springframework.util.StringUtils.isEmpty(vo.getPresentInformation())){
            entity.setPresentInformation(vo.getPresentInformation());
        }else {
            entity.setPresentInformation("--");
        }

        // 审核发布-审核通过
        return entrustService.entrustApproved1(entity.getEntrustmentId() ,entity);
    }


    /**
     * 预览材质单
     * @param entrustmentId
     * @return
     */
    @GetMapping("previewMaterial")
    public Result previewMaterial(Long entrustmentId){
        if (entrustmentId == null){
            return ResultUtil.error("缺少参数");
        }
        List<String> list = entrustService.getUrlListById(entrustmentId);
        return ResultUtil.success(list);
    }

}
