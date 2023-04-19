package com.lims.manage.erp.controller;

import com.lims.manage.erp.entity.ReqParamBean;
import com.lims.manage.erp.entity.SaveParamBean;
import com.lims.manage.erp.entity.TaskIdEntity;
import com.lims.manage.erp.mapper.TaskMapper;
import com.lims.manage.erp.service.PageOfficeService;
import com.lims.manage.erp.util.ShiroUtils;
import com.zhuozhengsoft.pageoffice.FileSaver;
import com.zhuozhengsoft.pageoffice.OpenModeType;
import com.zhuozhengsoft.pageoffice.PageOfficeCtrl;
import com.zhuozhengsoft.pageoffice.excelwriter.Sheet;
import com.zhuozhengsoft.pageoffice.excelwriter.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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


    @Autowired
    PageOfficeService pageOfficeService;

    @Value("${autograph.path}")
    private String dir;

    /**
     * 编辑接口
     * @param bean
     * @param request
     * @return
     */
    @RequestMapping(value ="Excel/editOriginalRecord")
    @ResponseBody
    public String showExcel(@RequestBody ReqParamBean bean, HttpServletRequest request) throws IOException {
        String username = ShiroUtils.getUserInfo().getUsername();
        //根据参数获取样品相关信息和检测项相关信息


        //填充表头信息临时缓存到本地
        System.out.println("触发");
        String url = pageOfficeService.getProductExcelUrl(bean);
        System.out.println();





        //设置服务页面
        PageOfficeCtrl poCtrl = new PageOfficeCtrl(request);
        poCtrl.setServerPage("/api/poserver.zz");
        //禁止拷贝文档内容到外部
        poCtrl.setDisableCopyOnly(true);
        //设置委托样品下未勾选检测项对应的指定sheet不可编辑状态 TODO
        poCtrl.setCustomToolbar(false);
        Workbook wb = new Workbook();

        //此处需要提供公共方法来批量设置sheet的不可编辑状态 TODO
        Sheet sheet1 = wb.openSheet("Sheet1");
        //设置当工作表只读时，是否允许用户手动调整行列。
        sheet1.setAllowAdjustRC(true);
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
        poCtrl.webOpen("临时本地文件", OpenModeType.xlsNormalEdit, username);
        poCtrl.webOpen("D:\\Users\\Administrator\\Desktop\\23年4月14日开发\\本地技术模板\\更改为标识符\\水泥.xlsx", OpenModeType.xlsNormalEdit, username);
        //TODO 删除临时文件

        return poCtrl.getHtmlCode("PageOfficeCtrl1");
    }

    /**
     * 保存接口
     * @param bean
     * @param request
     * @param response
     */
    @RequestMapping("saveOriginalRecord")
    public void save(@RequestBody SaveParamBean bean, HttpServletRequest request, HttpServletResponse response) {
        FileSaver fs = new FileSaver(request, response);
        fs.saveToFile(dir + fs.getFileName());
        fs.close();
        //上传文件到文件服务器、删除本地临时缓存的文件


        //相关表做更新或者插入操作
    }
}
