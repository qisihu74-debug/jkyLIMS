package com.lims.manage.erp.controller;

import com.aspose.slides.Collections.ArrayList;
import com.lims.manage.erp.entity.TaskIdEntity;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.service.PageOfficeService;
import com.lims.manage.erp.service.TaskService;
import com.lims.manage.erp.util.*;
import com.lims.manage.erp.vo.UserInfoVo;
import com.zhuozhengsoft.pageoffice.FileSaver;
import com.zhuozhengsoft.pageoffice.OpenModeType;
import com.zhuozhengsoft.pageoffice.PageOfficeCtrl;
import com.zhuozhengsoft.pageoffice.excelwriter.Sheet;
import com.zhuozhengsoft.pageoffice.excelwriter.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    @Value("${autograph.path}")
    private String dir;
    @Value("${posyspath}")
    private String poSysPath;

    /**
     * 编辑接口
     * @param request
     * @return
     */
    @RequestMapping(value ="Excel/editOriginalRecord")
//    @ResponseBody
//    public String showExcel(@RequestParam("json") String json, HttpServletRequest request) throws IOException {
    public ModelAndView showExcel(HttpServletRequest request,Map<String, Object> map) throws IOException {
        Map<String, String[]> parameterMap = request.getParameterMap();
        String list = parameterMap.get("list")[0];
        String[] items = list.split(",");
        Integer[] ids = new Integer[items.length];
        System.out.println("items == " + items);
        for(int j =0; j< items.length; j++){
            ids[j] =Integer.parseInt(items[j]);
        }
//        JSONObject jsonObject = JSONObject.parseObject(list);
        // 编辑参数赋值。
        map.put("items",list);



//        String username = ShiroUtils.getUserInfo().getUsername();
        //根据参数获取样品相关信息和检测项相关信息


        //填充表头信息临时缓存到本地
        String url = pageOfficeService.getProductExcelUrl(ids);
        // 验证 token 是否存在
        String[] mapToken = parameterMap.get("token");
        String strVerify = redisUtil.getRedisToken(mapToken[0]);
        System.out.println("token == " + strVerify);



        //设置服务页面
        PageOfficeCtrl poCtrl = new PageOfficeCtrl(request);
        poCtrl.setServerPage(request.getContextPath() + "/poserver.zz");
        //禁止拷贝文档内容到外部
        poCtrl.setDisableCopyOnly(true);
        //设置委托样品下未勾选检测项对应的指定sheet不可编辑状态 TODO
        poCtrl.setCustomToolbar(false);
        poCtrl.setFileTitle("另外存");



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

        Workbook wb = new Workbook();
        //此处需要提供公共方法来批量设置sheet的不可编辑状态 TODO
        // 循环设置
        List<TaskIdEntity> dataEntitys = taskMapper.selectconditionId(ids);
        for (int i = 0; i < dataEntitys.size(); i++) {
            TaskIdEntity data = dataEntitys.get(i);
            Sheet sheet1 = wb.openSheet(data.getOriginalName());
            //设置当工作表只读时，是否允许用户手动调整行列。
            sheet1.setAllowAdjustRC(true);
            // 设置工作表是否只读。
            //如果值为true，处于可编辑的Sheet将变成只读。如果值为false，处于只读的Sheet将变成可编辑。
            sheet1.setReadOnly(false);
        }
        //此行必须
        poCtrl.setWriter(wb);

        //加载文档
        url = URLDecoder.decode(url, "utf-8");
        String[] strArray = url.split("\\.");
        int suffixIndex = strArray.length - 1;
        String type = strArray[suffixIndex];
        ReturnResponse<String> response = downloadUtils.downLoad(url, type, null);
        poCtrl.webOpen("D:\\doc\\excel模板\\shuini.xlsx",OpenModeType.xlsNormalEdit,"张三");
//        poCtrl.webOpen(response.getContent().replace("/", "\\"), OpenModeType.xlsNormalEdit, "administrator");
        //TODO 删除临时文件

        map.put("pageoffice", poCtrl.getHtmlCode("PageOfficeCtrl1"));
        List<UserInfoVo> userInfoVos = new ArrayList();
        UserInfoVo userInfoVo = new UserInfoVo();
        userInfoVo.setDepartmentId("111L");
        userInfoVo.setName("丁1");
        userInfoVos.add(userInfoVo);

        UserInfoVo userInfoVo2 = new UserInfoVo();
        userInfoVo2.setDepartmentId("222L");
        userInfoVo2.setName("孙2");
        userInfoVos.add(userInfoVo2);

        UserInfoVo userInfoVo3 = new UserInfoVo();
        userInfoVo3.setDepartmentId("3333L");
        userInfoVo3.setName("王3");
        userInfoVos.add(userInfoVo3);

        map.put("userInfoVos", userInfoVos);
        ModelAndView mv = new ModelAndView("POB");
        return mv;
    }

    /**
     * 保存接口
     * @param bean
     * @param request
     * @param response
     */
    @RequestMapping("Excel/saveOriginalRecord")
//    public void save(@RequestBody SaveParamBean bean, HttpServletRequest request, HttpServletResponse response) {
    public void save(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        FileSaver fs = new FileSaver(request, response);
        // 检测人
        String testSet = fs.getFormField("testSet");
        // 记录人
        String recordSet = fs.getFormField("recordSet");
        // 检测参数
        String items = fs.getFormField("items");
//        JSONObject jsonObject = JSONObject.parseObject(items);
        System.out.println("items == " + items);
//        System.out.println("jsonObject == " + jsonObject);
        fs.saveToFile(dir + fs.getFileName());
        System.out.println("文件返回值 == " + fs.getFileName());
        // 根据人员内容 塞入Excel中
        List<UserInfoVo> userInfoVos = new ArrayList();
        UserInfoVo userInfoVo = new UserInfoVo();
        userInfoVo.setDepartmentId("111L");
        userInfoVo.setName("丁1");
        userInfoVos.add(userInfoVo);

        UserInfoVo userInfoVo2 = new UserInfoVo();
        userInfoVo2.setDepartmentId("222L");
        userInfoVo2.setName("孙2");
        userInfoVos.add(userInfoVo2);

        UserInfoVo userInfoVo3 = new UserInfoVo();
        userInfoVo3.setDepartmentId("3333L");
        userInfoVo3.setName("王3");
        userInfoVos.add(userInfoVo3);

        fs.close();
        //上传文件到文件服务器、删除本地临时缓存的文件


        //相关表做更新或者插入操作
//        return fs.getFileName();
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
