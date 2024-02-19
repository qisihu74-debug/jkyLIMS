package com.lims.manage.erp.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.api.client.util.Lists;
import com.lims.manage.erp.entity.JsonRootBean;
import com.lims.manage.erp.entity.TestStandardFile;
import com.lims.manage.erp.service.TestStandardFileService;
import com.lims.manage.erp.util.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;

/**
 * 标准规范根据工标网更新标准
 */
@Component
public class StandardHander {

    Logger logger = LoggerFactory.getLogger(StandardHander.class);

    @Resource
    private TestStandardFileService testStandardFileService;

    /**
     * 中国标准服务网--https://www.cssn.net.cn/api/standards/?keyword=
     * 工标网--http://www.csres.com/s.jsp?keyword=
     */
    //国家标准全文公开系统
    private String url = "https://openstd.samr.gov.cn/bzgk/gb/std_list?p.p1=0&p.p90=circulation_date&p.p91=desc&p.p2=";

    /**
     * 根据编号进行标准规范查新
     * @param code
     * @return
     */
    public TestStandardFile checkStandard(String code){
        String response = "";
        TestStandardFile standard = new TestStandardFile();
        String cod = "";
        try {
            cod = URLEncoder.encode(code, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //查新api
        standard = serachApi(code);
        if (standard != null){
            return standard;
        }
        //发起查询请求
        response = sendGet(url+cod, "UTF-8");
        //响应为空跳出本次循环
        if (StringUtils.isBlank(response)) {
            //中国标准服务网
            standard = cssn(code);
            if (standard == null) {
                //工标网
                standard = csres(code);
            }
        }else {
            //匹配标书数据，并更新表数据。涉及3个数据源查询
            //国家标准全文公开系统
            standard = openstd(response, code);
            if (standard == null){
                standard = std("https://std.samr.gov.cn/search/stdPage?q="+cod+"&tid=");
                if (standard == null){
                    standard = cssn(code);
                    if (standard == null){
                        standard = csres(code);
                    }
                }
            }
        }
        try {
            Thread.sleep(3500);
        } catch (InterruptedException e) {
            logger.error("标准查新线程阻塞异常:{}",e);
        }
        return standard;
    }

    /**
     * 查新api
     * @return
     */
    private TestStandardFile serachApi(String code) {
        TestStandardFile standardFile = null;
        try {
            String cod = URLEncoder.encode(code, "UTF-8");
            String url = "https://www.nssi.org.cn/cssn/front/standard/selectStandardListByCond";
            String cookie = "__jsluid_s=b880ab25b9c9ab27cc50e8b8ec9a2b5f; Hm_lvt_bc93e3a5e15e3913035aef1581181e88=1706151236; token=; uname=; account_id=; user_id=; relogin_name=; user_type=; JSESSIONID=A7C2DC1690F380B244B16F1E5A6F56CB; Hm_lpvt_bc93e3a5e15e3913035aef1581181e88=1706157997";
            String postData = "cond.orderBy=&cond.keywords="+cod+"&cond.activeValue=%E7%8E%B0%E8%A1%8C&cond.yearStartValue=&cond.yearEndValue=&cond.publishCorpA104=&cond.chinaClassCN=&cond.aboradClassICSN=&cond.otherpublishCorpGroup=&cond.account_id=&cond.login_name=&cond.superKeyWord=&cond.superKeyWordChoose=&cond.advanced_search=&cond.A100=&cond.caiyong=&cond.super_A101=&cond.super_standarnumber=&cond.super_w_level_s=&cond.super_w_location_s=&cond.super_tips_s=&cond.w_level_s=&cond.w_source_s=&cond.w_location_s=&cond.tips_s=&cond.shouye_a100=&cond.resulttype=0&cond.secondword=&cond.loadPage=true&cond.interface_type=&cond.containFullTextValue=%E5%90%A6&cond.whetherSearchAll=0&page=1&limit=10&start=0";

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
            con.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
            con.setRequestProperty("Connection", "keep-alive");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            con.setRequestProperty("Cookie", cookie);
            con.setRequestProperty("Origin", "https://www.nssi.org.cn");
            con.setRequestProperty("Referer", "https://www.nssi.org.cn/nssi/front/listpage.jsp");
            con.setRequestProperty("Sec-Fetch-Dest", "empty");
            con.setRequestProperty("Sec-Fetch-Mode", "cors");
            con.setRequestProperty("Sec-Fetch-Site", "same-origin");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            con.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            con.setRequestProperty("sec-ch-ua", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\"");
            con.setRequestProperty("sec-ch-ua-mobile", "?0");
            con.setRequestProperty("sec-ch-ua-platform", "\"Windows\"");

            con.setDoOutput(true);
            con.getOutputStream().write(postData.getBytes("UTF-8"));

            int responseCode = con.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject jsonObject = JSONObject.parseObject(response.toString());
            JSONObject dataAllMap = (JSONObject) jsonObject.get("dataAllMap");
            List<JsonRootBean> list = (List<JsonRootBean>) dataAllMap.get("standars");
            if (list != null){
                String jsonString = JSON.toJSONString(list.get(0));
                JsonRootBean bean = JSONObject.parseObject(jsonString,JsonRootBean.class);
                if (bean != null){
                    standardFile = new TestStandardFile();
                    standardFile.setCode(code);
                    standardFile.setStandardStatus(bean.getActive());
                    //发布日期
                    standardFile.setReleaseDate(bean.getFabudate());
                    //实施日期
                    standardFile.setImplementationDate(bean.getShishidate());
                }
            }
        } catch (Exception e) {
            logger.error("批量查新规范api异常:{}",e);
        }
        return standardFile;
    }

    /**
     * 获取https://std.samr.gov.cn数据
     * @param url
     * @return
     */
    private TestStandardFile std(String url) {
        TestStandardFile standard = null;
        //发起查询请求
        String response = sendGet(url, "UTF-8");
        if (response != null){
            Document doc = Jsoup.parse(response);
            if (doc != null){
                standard = new TestStandardFile();
                // 获取现行值
                Element currentStatus = doc.select("span.label.label-success").first();
                String current = currentStatus.text();
                //标书状态码
                standard.setStandardStatus(current);
                // 获取发布于的值
                Element publishDate = doc.select("time.post-date").get(0);
                String published = publishDate.text();
                //发布日期
                standard.setReleaseDate(published);

                // 获取实施于的值
                Element implementDate = doc.select("time.post-date").get(1);
                String implemented = implementDate.text();
                //实施日期
                standard.setImplementationDate(implemented);
            }
        }
        return standard;
    }

    /**
     * 定时更新标准规范状态。 每天24点更新数据
     */
    @Async("standardExecutor")
    @Scheduled(cron = "0 0 0 * * ? ")
    public void standard() {
        logger.info("开始更新标准规范状态。。。");
        //先查询到列表，循环查询每条标书
        QueryWrapper<TestStandardFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);
        queryWrapper.orderByDesc("create_time");
        List<TestStandardFile> standardList = testStandardFileService.list(queryWrapper);

        for (TestStandardFile standard : standardList) {
            if (StringUtils.isBlank(standard.getCode())) {
                continue;
            }
            //标准编号转义
            String code = null;
            try {
                code = URLEncoder.encode(standard.getCode(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            //发起查询请求
            String response = sendGet(url + code, "UTF-8");
            //响应为空跳出本次循环
            if (StringUtils.isBlank(response)) {
                continue;
            }
            //匹配标书数据，并更新表数据。涉及3个数据源查询
            boolean isSuccess = openstd(response, standard);//国家标准全文公开系统
            if (!isSuccess) {
                boolean isCsres = cssn(standard);//中国标准服务网

                if (!isCsres) {
                    csres(standard);//工标网

                }
            }
            //等待5秒，在发起下一个请求
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.getMessage();
            }
        }
        logger.info("结束更新标准规范状态。。。");
    }


    /**
     * 查询国家标准全文公开系统
     *
     * @param response 响应信息
     * @param standard 实体类
     */
    public boolean openstd(String response, TestStandardFile standard) {
        logger.info("查询国家标准全文公开系统");
        Document document = Jsoup.parse(response);

        Elements elements = document.select("div.table-responsive");
        if (elements.isEmpty()) {
            return false;
        }
        Elements tr = elements.select("tr");
        if (tr.isEmpty()) {
            return false;
        }
        for (int i = 0; i < tr.size(); i++) {
            Elements td = elements.select("td");
            if (td.isEmpty()) {
                return false;
            }
            if (td.size() >= 9) {
                //获取标书编号
                String standardNo = td.get(1).select("a").text();
                String standardStatus = td.get(5).select("span").text();
                logger.info("表编号:{},标准规范编号:{}, 状态：{}", standard.getCode(), standardNo, standardStatus);
                //比较标书编号
                if (standard.getCode().equals(standardNo)) {
                    String releaseDate = Optional.of(td.get(6).text()).orElse("");
                    String implementationDate = Optional.of(td.get(7).text()).orElse("");
                    //标书状态码
                    standard.setStandardStatus(standardStatus);
                    //发布日期
                    standard.setReleaseDate(releaseDate.substring(0, 10));
                    //实施日期
                    standard.setImplementationDate(implementationDate.substring(0, 10));
                    //更新表字段
                    boolean isUpdate = testStandardFileService.updateById(standard);
                    logger.info("更新标准规范状态是否成功:{}", isUpdate);
                } else {
                    return false;
                }

            } else {
                return false;
            }
        }
        return true;
    }

    public TestStandardFile openstd(String response, String code) {
        TestStandardFile standard = null;
        logger.info("查询国家标准全文公开系统");
        Document document = Jsoup.parse(response);
        if (document != null){
            Elements elements = document.select("div.table-responsive");
            if (elements != null){
                Elements tr = elements.select("tr");
                if (tr != null){
                    for (int i = 0; i < tr.size(); i++) {
                        Elements td = elements.select("td");

                        if (td.size() >= 9) {
                            //获取标书编号
                            String standardNo = td.get(1).select("a").text();
                            String standardStatus = td.get(5).select("span").text();
                            logger.info("表编号:{},标准规范编号:{}, 状态：{}", code, standardNo, standardStatus);
                            //比较标书编号
                            if (code.equals(standardNo)) {
                                String releaseDate = Optional.of(td.get(6).text()).orElse("");
                                String implementationDate = Optional.of(td.get(7).text()).orElse("");
                                standard = new TestStandardFile();
                                //标书状态码
                                standard.setStandardStatus(standardStatus);
                                //发布日期
                                standard.setReleaseDate(releaseDate.substring(0, 10));
                                //实施日期
                                standard.setImplementationDate(implementationDate.substring(0, 10));
                            }
                        }
                    }
                }
            }
        }
        return standard;
    }

    /**
     * 查询中国标准服务网
     *
     * @param standard 实体类
     */
    public boolean cssn(TestStandardFile standard) {
        logger.info("查询中国标准服务网");
        //标准编号转义
        String code = null;
        try {
            code = URLEncoder.encode(standard.getCode(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //发起查询请求
        String response = sendGet("https://www.cssn.net.cn/api/standards/?keyword=" + code, "UTF-8");
        if (StringUtils.isBlank(response)) {
            return false;
        }

        JSONObject jsonObject = JSONObject.parseObject(response);
        if (jsonObject.isEmpty()) {
            return false;
        }
        JSONArray array = Optional.of(jsonObject.getJSONArray("results")).orElse(new JSONArray());
        if (!array.isEmpty()) {
            JSONObject obj = Optional.of(array.getJSONObject(0)).orElse(new JSONObject());
            //A101  发布日期    a205 实施日期   a100  标砖编号  a000  状态
            if (!obj.isEmpty()) {
                //获取标书编号
                String standardNo = obj.getString("a100");
                String standardStatus = obj.getString("a000");
                //为空表示数据不完善
                if (StringUtils.isBlank(standardStatus)) {
                    return false;
                }
                logger.info("表编号:{},标准规范编号:{}, 状态：{}", standard.getCode(), standardNo, standardStatus);
                //比较标书编号
                if (standard.getCode().equals(standardNo)) {
                    String releaseDate = Optional.of(obj.getString("a101")).orElse("");
                    String implementationDate = Optional.of(obj.getString("a205")).orElse("");
                    if (StringUtils.isBlank(releaseDate)) {
                        return false;
                    }
                    if (StringUtils.isBlank(implementationDate)) {
                        return false;
                    }
                    //标书状态码
                    standard.setStandardStatus(standardStatus);
                    //发布日期
                    standard.setReleaseDate(releaseDate);
                    //实施日期
                    standard.setImplementationDate(implementationDate);
                    //更新表字段
                    boolean isUpdate = testStandardFileService.updateById(standard);
                    logger.info("更新标准规范状态是否成功:{}", isUpdate);
                } else {
                    return false;
                }

            } else {
                return false;
            }

        } else {
            return false;
        }

        return true;
    }

    public TestStandardFile cssn(String code) {
        TestStandardFile standard = null;
        String cod = "";
        logger.info("查询中国标准服务网");
        try {
            cod = URLEncoder.encode(code, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //发起查询请求
        String response = sendGet("https://www.cssn.net.cn/api/standards/?keyword=" + cod, "UTF-8");
        if (StringUtils.isNotEmpty(response)){
            JSONObject jsonObject = JSONObject.parseObject(response);
            if (jsonObject != null){
                JSONArray array = Optional.of(jsonObject.getJSONArray("results")).orElse(new JSONArray());
                if (!array.isEmpty()) {
                    JSONObject obj = Optional.of(array.getJSONObject(0)).orElse(new JSONObject());
                    //A101  发布日期    a205 实施日期   a100  标砖编号  a000  状态
                    if (!obj.isEmpty()) {
                        //获取标书编号
                        String standardNo = obj.getString("a100");
                        String standardStatus = obj.getString("a000");
                        logger.info("表编号:{},标准规范编号:{}, 状态：{}", code, standardNo, standardStatus);
                        //比较标书编号
                        if (code.equals(standardNo)) {
                            String releaseDate = Optional.of(obj.getString("a101")).orElse("");
                            String implementationDate = Optional.of(obj.getString("a205")).orElse("");
                            standard = new TestStandardFile();
                            standard.setCode(code);
                            //标书状态码
                            standard.setStandardStatus(standardStatus);
                            //发布日期
                            standard.setReleaseDate(releaseDate);
                            //实施日期
                            standard.setImplementationDate(implementationDate);
                        }
                    }
                }
            }
        }
        return standard;
    }

    /**q
     * 查询工标网
     *
     * @param standard 实体类
     * @return true/false
     */
    public boolean csres(TestStandardFile standard) {
        logger.info("查询工标网");
        String url = "http://www.csres.com/s.jsp?keyword=";
        //标准编号转义
        String code = null;
        try {
            code = URLEncoder.encode(standard.getCode(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //发起查询请求
        String response = sendGet(url + code, "gbk");
        if (StringUtils.isBlank(response)) {
            return false;
        }
        Document document = Jsoup.parse(response);
        if (document.isBlock()) {
            return false;
        }
        Elements table = document.select("table.heng");
        if (table.isEmpty()) {
            return false;
        }
        Elements td = table.select("td");
        if (td.size() >= 5) {
            //获取标书编号
            String standardNo = td.select("a").text();
            String standardStatus = td.get(4).text();
            logger.info("表编号:{},标准规范编号:{}, 状态：{}", standard.getCode(), standardNo, standardStatus);
            //比较标书编号
            if (standard.getCode().equals(standardNo)) {
                String implementationDate = Optional.of(td.get(3).text()).orElse("");
                //标书状态码
                standard.setStandardStatus(standardStatus);
                //实施日期
                standard.setImplementationDate(implementationDate);
                //更新表字段
                boolean isUpdate = testStandardFileService.updateById(standard);
                logger.info("更新标准规范状态是否成功:{}", isUpdate);
            }
        }
        return true;
    }

    public TestStandardFile csres(String code) {
        TestStandardFile standard = null;
        String cod = "";
        logger.info("查询工标网");
        try {
            cod = URLEncoder.encode(code, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = "http://www.csres.com/s.jsp?keyword=";
        //标准编号转义
        //发起查询请求
        String response = sendGet(url + cod, "gbk");
        if (StringUtils.isNotEmpty(response)){
            Document document = Jsoup.parse(response);
            if (document != null){
                Elements table = document.select("table.heng");
                if (table != null){
                    Elements td = table.select("td");
                    if (td.size() >= 5) {
                        //获取标书编号
                        String standardNo = td.select("a").text();
                        String standardStatus = td.get(4).text();
                        logger.info("表编号:{},标准规范编号:{}, 状态：{}", code, standardNo, standardStatus);
                        //比较标书编号
                        if (code.equals(standardNo)) {
                            String implementationDate = Optional.of(td.get(3).text()).orElse("");
                            standard = new TestStandardFile();
                            standard.setCode(code);
                            //标书状态码
                            standard.setStandardStatus(standardStatus);
                            //实施日期
                            standard.setImplementationDate(implementationDate);
                        }
                    }
                }
            }
        }
        return standard;
    }

    /**
     * 发起get请求
     *
     * @param url 请求地址
     * @return 响应信息
     */
    public static String sendGet(String url, String charset) {
        System.out.println("请求GET地址：" + url);
        String result = "";
        BufferedReader in = null;
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "text/html; charset=UTF-8");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();

            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), charset));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        } finally {
            //关闭输入流
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }


}
