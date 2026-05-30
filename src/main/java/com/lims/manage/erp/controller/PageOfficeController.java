package com.lims.manage.erp.controller;

import com.aspose.cells.SaveFormat;
import com.aspose.cells.Worksheet;
import com.lims.manage.erp.entity.QiYueSuoReqBean;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TaskIdEntity;
import com.lims.manage.erp.entity.TaskTestEntity;
import com.lims.manage.erp.http.QiYueSuoResponse;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TestProductItemDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.PageOfficeCopyService;
//import com.lims.manage.erp.service.PageOfficeService;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.ExcelInsertVo;
import com.lims.manage.erp.vo.LabelValueVo;
import com.lims.manage.erp.vo.TeamVo;
import com.zhuozhengsoft.pageoffice.FileSaver;
import com.zhuozhengsoft.pageoffice.OpenModeType;
import com.zhuozhengsoft.pageoffice.PageOfficeCtrl;
import com.zhuozhengsoft.pageoffice.excelwriter.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.controller
 * @desc
 * @date 2023-04-18 10:16
 * @Copyright © 河南交科院
 */
@RequestMapping("/pageOffice/")
@RestController
public class PageOfficeController {

    private final static Logger logger = LoggerFactory.getLogger(PageOfficeController.class);
    @Autowired
    DownloadUtils downloadUtils;
    //    @Autowired
//    PageOfficeService pageOfficeService;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TestProductItemDao testProductItemDao;
    @Autowired
    PageOfficeCopyService pageOfficeCopyService;
    @Value("${autograph.path}")
    private String dir;
    @Value("${posyspath}")
    private String poSysPath;

    /**
     * 编辑接口
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "Excel/editOriginalRecord")
    public ModelAndView showExcel(HttpServletRequest request, Map<String, Object> map) throws Exception {
        Map<String, String[]> parameterMap = request.getParameterMap();
        String list = parameterMap.get("list")[0];
        String[] items = list.split(",");
        Integer[] ids = new Integer[items.length];
        for (int j = 0; j < items.length; j++) {
            ids[j] = Integer.parseInt(items[j]);
        }
        // 编辑参数赋值。
        map.put("items", list);
        PDFHelper3.getLicense();
        // 查询检测项对应的 sheet下标
        List<ExcelInsertVo> sheetItems = testProductItemDao.selectItemSheetIndex(ids);
        if (CollectionUtils.isEmpty(sheetItems)) {
            // 检测项无Sheet页
            return new ModelAndView("error");
        }
        // 循环设置
        List<TaskIdEntity> dataEntitys = taskMapper.selectItems(ids);
        Long taskId = dataEntitys.get(0).getTaskId();
        // 验证 token 是否存在
        String[] mapToken = parameterMap.get("token");
        String strVerify = redisUtil.getRedisToken(mapToken[0]);
        if (strVerify == null) {
            System.out.println("strVerify: == null");
        }
        SysUserEntity user = new SysUserEntity();
        if (strVerify != null) {
            user = redisUtil.getRedisTokenUser(strVerify);
        }
        if (user != null) {
            if (user.getUserId() != null) {
                // 查询任务详情
                TaskTestEntity taskEntity = taskMapper.getTaskOrders(taskId);
                // 记录人
                List<LabelValueVo> recorderVos = new ArrayList<>();
                if (taskEntity.getRecorder().contains(user.getUserId().toString())) {
                    String[] array1 = taskEntity.getRecorder().split(",");
                    for (int i = 0; i < array1.length; i++) {
                        String[] recorders = array1[i].split("&");
                        LabelValueVo labelValueVo = new LabelValueVo();
                        labelValueVo.setLabel(recorders[0]);
                        labelValueVo.setValue(Long.parseLong(recorders[1]));
                        recorderVos.add(labelValueVo);
                    }
                }
                // 检测人
                List<LabelValueVo> inspectorVos = new ArrayList<>();
                if (taskEntity.getInspector().contains(user.getUserId().toString())) {
                    String[] array1 = taskEntity.getInspector().split(",");
                    for (int i = 0; i < array1.length; i++) {
                        String[] inspectors = array1[i].split("&");
                        LabelValueVo labelValueVo = new LabelValueVo();
                        labelValueVo.setLabel(inspectors[0]);
                        labelValueVo.setValue(Long.parseLong(inspectors[1]));
                        inspectorVos.add(labelValueVo);
                    }
                }
                if (CollectionUtils.isEmpty(recorderVos) && CollectionUtils.isEmpty(inspectorVos)) {
                    Map<String, Object> msgMap = new HashMap<>();
                    // {"code":201,"msg":"成功","data":"撤回成功，检测项回到初始状态"}
                    msgMap.put("code", 201);
                    msgMap.put("msg", "操作失败");
                    msgMap.put("data", "当前登录人 不是检测人或记录人：无操作权限");
                    ModelAndView mv = new ModelAndView();
                    mv.addObject(msgMap);
                    mv.setViewName("msg");
                    return mv;
                }
                map.put("teamVo", inspectorVos);
                map.put("recorderVo", recorderVos);
            }
        }
        //填充表头信息临时缓存到本地
        String url = pageOfficeCopyService.getProductExcelUrl(ids, sheetItems, dataEntitys);
        InputStream fileStream = null;
        try {
            // 获取公网 附件
            fileStream = FileAndFolderUtil.getInputStream(url);
        } catch (Exception e) {
            logger.info("读取产品excel异常 " + url + e);
        }
        //设置服务页面
        PageOfficeCtrl poCtrl = new PageOfficeCtrl(request);
        poCtrl.setServerPage(request.getContextPath() + "/poserver.zz");
        //禁止拷贝文档内容到外部
        poCtrl.setDisableCopyOnly(true);
        //设置委托样品下未勾选检测项对应的指定sheet不可编辑状态 TODO
//        poCtrl.setCustomToolbar(false);
        Workbook wb = new Workbook();
        //此处需要提供公共方法来批量设置sheet的不可编辑状态 TODO
        PDFHelper3.getLicense();
        com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook(fileStream);
        int count = workbook.getWorksheets().getCount();
        // 读取 url 附件 设置为全部可读
        // 设置所有状态 可见。
        for (int o = 0; o < count; o++) {
            // 设置全部可读
            workbook.getWorksheets().get(o).setVisible(true);
        }
        // 根据key 保证 sheet不重复使用。
        Map<String, String> keyMap = new HashMap<>();
        try {
            for (int i = 0; i < count; i++) {
                String name = workbook.getWorksheets().get(i).getName();
                for (int j = 0; j < dataEntitys.size(); j++) {
                    TaskIdEntity data = dataEntitys.get(j);
                    // 检测项 0：待检，1：检测中，2：待复核，3 ：通过，4：驳回 && 检测项对应的sheet 不为空
                    if (data != null && !data.getState().equals(3) && !CollectionUtils.isEmpty(sheetItems)) {
                        for (ExcelInsertVo excelInsertVo1 : sheetItems) {
                            // 获取sheetIndex工作表
                            Worksheet sheet = workbook.getWorksheets().get(excelInsertVo1.getSheetIndex());
                            // sheet != null && checkItemId 相等
                            if (sheet != null && excelInsertVo1.getCheckItemId().equals(data.getCheckItemId())) {
                                if (keyMap.get(sheet.getName()) == null) {
                                    keyMap.put(sheet.getName(), sheet.getName());

                                }
                            }
                        }
                    }
                }
                if (keyMap.get(name) != null){
                    // 设置为 可见
                    workbook.getWorksheets().get(i).setVisible(true);
                    //设置当工作表只读时，是否允许用户手动调整行列。
                    wb.openSheet(name).setAllowAdjustRC(true);
                    //如果值为true，处于可编辑的Sheet将变成只读。如果值为false，处于只读的Sheet将变成可编辑。
                    wb.openSheet(name).setReadOnly(false);
                } else{
                    // 获取工作表的隐藏状态，返回SheetVisibility类型
                    int visibility = workbook.getWorksheets().get(i).getVisibilityType();
                    if (visibility != 1) {
                        // sheetName 不相等 设置为隐藏
                        workbook.getWorksheets().get(i).setVisible(false);
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            // 设置所有状态 可见。
            for (int o = 0; o < count; o++) {
                // 设置全部可读
                workbook.getWorksheets().get(o).setVisible(true);
            }
            String excel = dir + GenID.getID() + "." + "xlsx";
            workbook.save(excel, SaveFormat.XLSX);
            InputStream inputStream = new FileInputStream(excel);
            // 更新 文件
            pageOfficeCopyService.updateExcelVisible(GenID.getID() + "." + "xlsx", ids, inputStream);
            // 删除附件
            FileAndFolderUtil.delete(excel);
            // 检测项无Sheet页
            return new ModelAndView("error");
        }
        String excel = dir + GenID.getID() + "." + "xlsx";
        workbook.save(excel, SaveFormat.XLSX);
        //此行必须
        poCtrl.setWriter(wb);
        //添加自定义按钮
        poCtrl.addCustomToolButton("保存", "Save()", 1);
        poCtrl.addCustomToolButton("打印", "PrintFile()", 6);
        poCtrl.addCustomToolButton("全屏/还原", "IsFullScreen()", 4);
        poCtrl.addCustomToolButton("关闭", "CloseFile()", 21);
        //设置操作栏按钮
        poCtrl.getRibbonBar().setTabVisible("TabHome", true);//开始
        poCtrl.getRibbonBar().setTabVisible("TabFormulas", false);//公式
        poCtrl.getRibbonBar().setTabVisible("TabInsert", false);//插入
        poCtrl.getRibbonBar().setTabVisible("TabData", false);//数据
        poCtrl.getRibbonBar().setTabVisible("TabReview", false);//审阅
        poCtrl.getRibbonBar().setTabVisible("TabView", false);//视图
        //设置处理文件保存的请求方法
        poCtrl.setSaveFilePage("saveOriginalRecord");
        logger.info("在线编辑原始记录本地缓存路径:{}",excel);
        if (excel.indexOf(":\\") < 0){
            excel = "file://"+excel;
        }
        logger.info("在线编辑原始记录本地缓存路径1:{}",excel);
        poCtrl.webOpen(excel, OpenModeType.xlsSubmitForm, "administrator");
        //TODO 删除临时文件
        // 删除附件
        FileAndFolderUtil.delete(excel);
        fileStream.close();
        map.put("pageoffice", poCtrl.getHtmlCode("PageOfficeCtrl1"));
        ModelAndView mv = new ModelAndView("POB");
        return mv;
    }

//    /**
//     * 保存接口
//     *
//     * @param request
//     * @param response
//     */
//    @RequestMapping("Excel/saveOriginalRecord")
//    public void save(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        FileSaver fs = new FileSaver(request, response);
//        // 实现逻辑操作 -- 完成编辑
//        PDFHelper3.getLicense();
//        String flag = pageOfficeCopyService.saveOriginalRecord(request, fs);
//        fs.close();
//        // 保存本地Excel 包含签名信息
//        if (!StringUtils.isEmpty(flag)) {
//            // 检测参数
//            String list = fs.getFormField("items");
//            // 获取检测项id 集合
//            String[] items = list.split(",");
//            Integer[] ids = new Integer[items.length];
//            for (int j = 0; j < items.length; j++) {
//                ids[j] = Integer.parseInt(items[j]);
//            }
//            pageOfficeCopyService.updateOriginalRecordUrl(flag, ids);
//            // 更新编辑原始记录完成标记。
//            pageOfficeCopyService.editItemdData(ids,null,null);
//        }
//    }

    /**
     * 2023年9月5日更新：试验检测在线编辑：保留检测项对应签名信息，签名图片不做处理。
     *
     * @param request
     * @param response
     */
    @RequestMapping("Excel/saveOriginalRecord")
    public void save(HttpServletRequest request, HttpServletResponse response) throws Exception {
        FileSaver file = new FileSaver(request, response);
        // 读取 读取file中 试验签名，记录签名
        Map<String, String> map = pageOfficeCopyService.getName(file);
        // 检测人
        String testSet = map.get("testSet");
        // 记录人
        String recordSet = map.get("recordSet");
        // 实现逻辑操作 -- 完成编辑
        PDFHelper3.getLicense();
        // 生成的 完成后的file文件
        String saveExcel = dir + file.getFileName();
        // 文件路径
        file.saveToFile(saveExcel);
        file.close();
        // 保存本地Excel 包含签名信息
        if (!StringUtils.isEmpty(saveExcel)) {
            com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook(saveExcel);
            int count = workbook.getWorksheets().getCount();
            for (int o = 0; o < count; o++) {
                // 设置全部可读
                workbook.getWorksheets().get(o).setVisible(true);
            }
            workbook.save(saveExcel, SaveFormat.XLSX);
            // 检测参数
            String list = file.getFormField("items");
            // 获取检测项id 集合
            String[] items = list.split(",");
            Integer[] ids = new Integer[items.length];
            for (int j = 0; j < items.length; j++) {
                ids[j] = Integer.parseInt(items[j]);
            }
            pageOfficeCopyService.updateOriginalRecordUrl(saveExcel, ids);
            // 更新编辑原始记录完成标记。
            // 进行检测项与检测人和记录人的存储。
            pageOfficeCopyService.editItemdData(ids, testSet, recordSet);
        }
    }

    /**
     * 添加PageOffice的服务器端授权程序Servlet（必须）
     *
     * @return
     */
    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        com.zhuozhengsoft.pageoffice.poserver.Server poserver = new com.zhuozhengsoft.pageoffice.poserver.Server();
        poserver.setSysPath(poSysPath);//设置PageOffice注册成功后,license.lic文件存放的目录
        ServletRegistrationBean srb = new ServletRegistrationBean(poserver);
        srb.addUrlMappings("/poserver.zz");
        srb.addUrlMappings("/posetup.exe");
        srb.addUrlMappings("/pageoffice.js");
        srb.addUrlMappings("/jquery.min.js");
        srb.addUrlMappings("/pobstyle.css");
        srb.addUrlMappings("/sealsetup.exe");
        return srb;//
    }

    /**
     * 创建合同
     * @param reqBean
     * @return
     */
    @PostMapping("startInitiateContractLock")
    public Result startInitiateContractLock(@RequestBody QiYueSuoReqBean reqBean) {
        if (reqBean == null) {
            return ResultUtil.error("缺少必要的参数");
        }
        if (org.apache.commons.collections.CollectionUtils.isEmpty(reqBean.getList())) {
            return ResultUtil.error("请选择需要签署的检测项");
        }
        Long[] array = new Long[reqBean.getList().size()];
        for (int i = 0; i < reqBean.getList().size(); i++) {
            array[i] = reqBean.getList().get(i);
        }
        List<ExcelInsertVo> list = testProductItemDao.selectCheckList(array);
        if (CollectionUtils.isEmpty(list)) {
            return ResultUtil.error("创建合同失败：当前检测项 不存在");
        }
        List<String> stringList = new ArrayList<>();
        // 效验每个检测项的信息
        for (ExcelInsertVo excelInsertVo : list) {
            if (excelInsertVo.getState() != 3) {
                return ResultUtil.error("创建合同失败：当前检测项 " + excelInsertVo.getCheckItemName() + " 未通过复核 ");
            }
            if (StringUtils.isEmpty(excelInsertVo.getEditData())) {
                return ResultUtil.error("创建合同失败：当前检测项 " + excelInsertVo.getCheckItemName() + " 未进行excel在线编辑 ");
            }
            if(org.apache.commons.lang3.StringUtils.isNotEmpty(excelInsertVo.getSignUrl())){
                return ResultUtil.error("创建合同失败：当前检测项 " + excelInsertVo.getCheckItemName() + " 电子印章signUrl已存在 ");
            }
            stringList.add(excelInsertVo.getCheckItemCode());
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < stringList.size(); i++) {
            stringBuilder.append(stringList.get(i));
            if (i == stringList.size() - 1) {
                continue;
            } else {
                stringBuilder.append(",");
            }
        }
        reqBean.setSubject(stringBuilder.toString());
        QiYueSuoResponse response = pageOfficeCopyService.createbycategoryBatch(reqBean, stringList);
        if (response != null && response.getCode() == 0) {
            return ResultUtil.success("向契约锁发起报告制作申请成功!");
        } else {
            return ResultUtil.error("向契约锁发起报告制作申请失败：" + response.getMessage());
        }
    }
}
