package com.stu.manage.demo.controller;

import com.alibaba.fastjson.JSON;
import com.dingtalk.api.response.OapiDepartmentGetResponse;
import com.dingtalk.api.response.OapiUserGetuserinfoResponse;
import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.stu.manage.demo.entity.Book;
import com.stu.manage.demo.entity.Ding;
import com.stu.manage.demo.result.Result;
import com.stu.manage.demo.result.ResultEnum;
import com.stu.manage.demo.result.ResultUtil;
import com.stu.manage.demo.service.BookService;
import com.stu.manage.demo.util.AccessTokenSingleton;
import com.stu.manage.demo.util.BookExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.stu.manage.demo.controller
 * @desc
 * @date 2021/8/22 11:27
 * @Copyright © jky
 */
@RestController
@RequestMapping("/book/")
@Slf4j
public class BookController {
    @Autowired
    private BookService bookService;
    Logger logger = LoggerFactory.getLogger(BookController.class);
    // 钉钉api相关
    @Value("${dingtalk.token_url}")
    private String token;
    @Value("${dingtalk.user_url}")
    private String user;
    @Value("${dingtalk.info_url}")
    private String userInfo;
    @Value("${dingtalk.dept_url}")
    private String dept;
    @Value("${dingtalk.app_key}")
    private String appKey;
    @Value("${dingtalk.app_secret}")
    private String appsecret;
    @Value("${ding_tushu_url}")
    private String bookUrl;
    @Value("${ding_showapi_appid}")
    private String showAppId;
    @Value("${ding_showapi_sign}")
    private String showAppSign;

    /**
     * 插入图书数据
     *
     * @param book
     * @return
     */
    @PostMapping("insert")
    public Result insert(@RequestBody @Valid Book book) {
        int res = bookService.insertBook(book);
        if (res == 1) {
            return ResultUtil.success(res);
        } else {
            return ResultUtil.error(ResultEnum.STUDENT_NOT_EXIST.getCode(), ResultEnum.STUDENT_NOT_EXIST.getMsg());
        }
    }

    /**
     * 获取图书列表信息
     *
     * @param search
     * @return
     */
    @GetMapping("list")
    public Result list(String search) {
        List<Book> list = bookService.getList(search);
        return ResultUtil.success(list);
    }

    /**
     * 导出图书信息
     *
     * @param request
     * @param response
     */
    @RequestMapping(value = "export", method = RequestMethod.GET)
    public void mutiExport(HttpServletRequest request, HttpServletResponse response) {
        BufferedOutputStream bos = null;
        try {

            List<Book> bookList = bookService.getList(null);
            String fileName = "图书信息";
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename="
                    + new String(fileName.getBytes("gbk"), "iso_8859_1") + ".xls");
            InputStream inputStream = BookExportUtil.bookExport(bookList);
            ServletOutputStream outputStream = response.getOutputStream();
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            bos = new BufferedOutputStream(outputStream);
            byte[] buff = new byte[2048];
            int bytesRead;
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
                bos.flush();
            }
        } catch (Exception e) {
            logger.error("导出模版异常", e);
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                logger.error("数据流关闭异常:{}", e);
            }
        }
    }

    /**
     * 调用钉钉api免登陆
     *
     * @param code
     * @return
     */
    @GetMapping("noRegistration")
    public Result noRegistration(String code) {
        if (StringUtils.isEmpty(code)){
            return ResultUtil.error(8,"缺少必要参数");
        }
        OapiUserGetuserinfoResponse rsp1 = null;
        OapiV2UserGetResponse rsp2 = null;
        List<OapiDepartmentGetResponse> list = null;

        AccessTokenSingleton instance = AccessTokenSingleton.getInstance();
        String token1 = instance.getToken(token,appKey,appsecret);
        if (StringUtils.isNotEmpty(token1)){
            rsp1 = instance.getDingUser(user,code,token1);
        }
        if (rsp1 != null){
            rsp2 = instance.getDingInfo(userInfo, rsp1.getUserid(), token1);
        }
        if (rsp2 != null){
            list = instance.getDingDept(dept,rsp2.getResult().getDeptIdList(),token1);
        }
        //返回钉钉用户部门信息
        List<Ding> list1 = new ArrayList();
        List<String> deptList = new ArrayList<>();
        Ding ding = new Ding();
        for (OapiDepartmentGetResponse bean:list) {
            ding.setUserName(rsp2.getResult().getName());
            deptList.add(bean.getName());
        }
        ding.setList(deptList);
        return ResultUtil.success(ding);
    }

    /**
     * 根据图书条形码获取图书详情
     * @param  @RequestParam String isbn,
     * @return
     */
    @PostMapping("updateFormDatas")
    public Result getBookDetail(
                                @RequestParam String appType,
                                @RequestParam String systemToken,
                                @RequestParam String userId,
                                @RequestParam String formInstId,
                                @RequestParam String updateFormDataJson){
        /*if (StringUtils.isEmpty(isbn)){
            return ResultUtil.error(-1,"缺少参数");
        }*/
        Book book = AccessTokenSingleton.getInstance().getDetail(updateFormDataJson, bookUrl, showAppId, showAppSign);
        logger.debug("获取到书籍详情:{}",JSON.toJSONString(book));
        if (book != null){
            String language = "zh_CN";
            AccessTokenSingleton.getInstance().update(appType,systemToken,userId,language,
                    formInstId,false, JSON.toJSONString(book));
        }
        return ResultUtil.success(book);
    }

}

