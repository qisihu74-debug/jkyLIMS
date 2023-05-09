package com.lims.manage.erp.controller;

import com.aspose.cells.SaveFormat;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.entity.TaskIdEntity;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.mapper.TestProductItemDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.service.PageOfficeService;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.TeamVo;
import com.zhuozhengsoft.pageoffice.FileSaver;
import com.zhuozhengsoft.pageoffice.OpenModeType;
import com.zhuozhengsoft.pageoffice.PageOfficeCtrl;
import com.zhuozhengsoft.pageoffice.excelwriter.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
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
    @Autowired
    PageOfficeService pageOfficeService;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TestProductItemDao testProductItemDao;

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
//        System.out.println("items == " + items);
        for (int j = 0; j < items.length; j++) {
            ids[j] = Integer.parseInt(items[j]);
        }
        // 编辑参数赋值。
        map.put("items", list);
        // 验证 token 是否存在
        String[] mapToken = parameterMap.get("token");
        String strVerify = redisUtil.getRedisToken(mapToken[0]);
//        System.out.println("token == " + strVerify);
        SysUserEntity user = new SysUserEntity();
        if (strVerify != null) {
            user = redisUtil.getRedisTokenUser(strVerify);
        }
        if (user != null) {
            if (user.getUserId() != null) {
                // 领取人
                TeamVo returnList = taskService.getTeamUserNameTwo(user.getUserId());
                map.put("teamVo", returnList.getTeamVo());
            }
        }
        //根据参数获取样品相关信息和检测项相关信息
        //填充表头信息临时缓存到本地
        String url = pageOfficeService.getProductExcelUrl(ids);
        //设置服务页面
        PageOfficeCtrl poCtrl = new PageOfficeCtrl(request);
        poCtrl.setServerPage(request.getContextPath() + "/poserver.zz");
        //禁止拷贝文档内容到外部
        poCtrl.setDisableCopyOnly(true);
        //设置委托样品下未勾选检测项对应的指定sheet不可编辑状态 TODO
//        poCtrl.setCustomToolbar(false);
        Workbook wb = new Workbook();
        //此处需要提供公共方法来批量设置sheet的不可编辑状态 TODO
        // 循环设置
        List<TaskIdEntity> dataEntitys = taskMapper.selectItems(ids);
        InputStream fileStream = null;
        try {
            // 获取公网 附件
            fileStream = FileAndFolderUtil.getInputStream(url);
        } catch (Exception e) {
            logger.info("读取产品excel异常 " + url + e);
        }
        com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook(fileStream);
        int count = workbook.getWorksheets().getCount();
        for (int i = 0; i < count; i++) {
            Boolean flag = false;
            String sheetName = "";
            for (int j = 0; j < dataEntitys.size(); j++) {
                TaskIdEntity data = dataEntitys.get(j);
                if (data.getCheckItemName().equals(workbook.getWorksheets().get(i).getName())) {
                    flag = true;
                    sheetName = data.getCheckItemName();
                }
            }
            if (flag) {
                // 设置为 可见
                workbook.getWorksheets().get(i).setVisible(true);
                //设置当工作表只读时，是否允许用户手动调整行列。
                wb.openSheet(sheetName).setAllowAdjustRC(true);
                //如果值为true，处于可编辑的Sheet将变成只读。如果值为false，处于只读的Sheet将变成可编辑。
                wb.openSheet(sheetName).setReadOnly(false);
            } else {
                // sheetName 不相等 设置为隐藏
                workbook.getWorksheets().get(i).setVisible(false);
            }
        }
        String excel = dir + GenID.getID() + "." + "xlsx";
        workbook.save(excel, SaveFormat.XLSX);
        // 去除excel 中标记
        InputStream fileStream2 = new FileInputStream(excel);
        XSSFWorkbook wb2 = new XSSFWorkbook(fileStream2);
        // 调用方法 清除sheet名 = Evaluation Warning
        ExcelReplaceUtil.removeOtherSheets("Evaluation Warning", wb2);
        fileStream2.close();
        OutputStream f = new FileOutputStream(excel);
        wb2.write(f);
        f.close();
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
        //加载文档
        url = URLDecoder.decode(url, "utf-8");
        String[] strArray = url.split("\\.");
        int suffixIndex = strArray.length - 1;
        String type = strArray[suffixIndex];
//        ReturnResponse<String> response = downloadUtils.downLoad(url, type, null);
        poCtrl.webOpen(excel, OpenModeType.xlsSubmitForm, "administrator");
//        poCtrl.webOpen(response.getContent().replace("/", "\\"), OpenModeType.xlsSubmitForm, "administrator");
        //TODO 删除临时文件
        // 删除附件
        FileAndFolderUtil.delete(excel);
        fileStream.close();
        map.put("pageoffice", poCtrl.getHtmlCode("PageOfficeCtrl1"));
        ModelAndView mv = new ModelAndView("POB");
        return mv;
    }

    /**
     * 保存接口
     *
     * @param request
     * @param response
     */
    @RequestMapping("Excel/saveOriginalRecord")
    public ModelAndView save(HttpServletRequest request, HttpServletResponse response) throws Exception {
        FileSaver fs = new FileSaver(request, response);
        // 实现逻辑操作 -- 完成编辑
        String flag = pageOfficeService.saveOriginalRecord(request, fs);
        // 保存本地Excel 包含签名信息
        if (!StringUtils.isEmpty(flag)) {
//            // 检测参数
            String list = fs.getFormField("items");
            // 获取检测项id 集合
            String[] items = list.split(",");
            Integer[] ids = new Integer[items.length];
            for (int j = 0; j < items.length; j++) {
                ids[j] = Integer.parseInt(items[j]);
            }
            pageOfficeService.updateOriginalRecordUrl(flag, ids);
            ModelAndView mv = new ModelAndView("success");
            return new ModelAndView("success");
        } else {
            ModelAndView mv = new ModelAndView("error");
            return mv;
        }
    }

    /**
     * 添加PageOffice的服务器端授权程序Servlet（必须）
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
}
