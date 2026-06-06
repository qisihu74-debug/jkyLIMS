package com.lims.manage.erp.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lims.manage.erp.entity.CmaCapabilityItem;
import com.lims.manage.erp.entity.CmaSyncLog;
import com.lims.manage.erp.entity.NewsBean;
import com.lims.manage.erp.job.StandardHander;
import com.lims.manage.erp.mapper.CmaCapabilityItemMapper;
import com.lims.manage.erp.mapper.CmaSyncLogMapper;
import com.lims.manage.erp.service.CmaService;
import com.lims.manage.erp.service.NewsService;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CmaServiceImpl extends ServiceImpl<CmaCapabilityItemMapper, CmaCapabilityItem>
        implements CmaService {

    private static final Logger logger = LoggerFactory.getLogger(CmaServiceImpl.class);
    private static final String CMA_CONFIG_URL =
            "https://cma.caqit.org.cn/cma-admin/system/config/configKey/standard.data.excel.name";
    private static final String CMA_PROFILE_BASE =
            "https://cma.caqit.org.cn/cma-admin/profile/";
    private static final String OPENSTD_LIST_URL =
            "https://openstd.samr.gov.cn/bzgk/gb/std_list?p.p1=0&p.p90=circulation_date&p.p91=desc&p.p2=";
    private static final Pattern HCNO_PATTERN =
            Pattern.compile("hcno=([A-F0-9]{32})", Pattern.CASE_INSENSITIVE);

    @Resource
    private CmaSyncLogMapper cmaSyncLogMapper;

    @Resource
    private NewsService newsService;

    @Override
    public PageInfo<CmaCapabilityItem> list(int pageNum, int pageSize, String domain,
                                             String standardName, String standardCode) {
        PageHelper.startPage(pageNum, pageSize);
        QueryWrapper<CmaCapabilityItem> qw = new QueryWrapper<>();
        if (StringUtils.isNotBlank(domain)) {
            qw.eq("domain", domain);
        }
        if (StringUtils.isNotBlank(standardName)) {
            qw.like("standard_name", standardName);
        }
        if (StringUtils.isNotBlank(standardCode)) {
            qw.like("standard_code", standardCode);
        }
        qw.orderByAsc("domain", "standard_code");
        List<CmaCapabilityItem> items = baseMapper.selectList(qw);
        return new PageInfo<>(items);
    }

    @Override
    public List<String> domains() {
        return baseMapper.selectDomains();
    }

    @Override
    public CmaSyncLog latestSync() {
        return cmaSyncLogMapper.selectLastSuccess();
    }

    @Override
    public void syncFromCma() {
        logger.info("开始同步CMA能力项目库...");
        try {
            // 1. 获取最新文件名
            String configResp = HttpUtil.get(CMA_CONFIG_URL, 15000);
            JSONObject configJson = JSONObject.parseObject(configResp);
            if (configJson == null || !Integer.valueOf(200).equals(configJson.getInteger("code"))) {
                saveSyncLog("", 0, "FAIL", "获取CMA配置失败: " + configResp);
                return;
            }
            String fileName = configJson.getString("msg");
            if (StringUtils.isBlank(fileName)) {
                saveSyncLog("", 0, "FAIL", "CMA配置返回文件名为空");
                return;
            }

            // 2. 与上次成功同步的文件名比较
            CmaSyncLog lastSuccess = cmaSyncLogMapper.selectLastSuccess();
            if (lastSuccess != null && fileName.equals(lastSuccess.getFileName())) {
                logger.info("CMA文件名未变更({}), 跳过本次同步", fileName);
                saveSyncLog(fileName, 0, "SKIP", "文件名未变更，跳过本次同步");
                return;
            }

            // 3. 下载Excel
            String downloadUrl = CMA_PROFILE_BASE
                    + URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
            logger.info("正在下载CMA Excel: {}", downloadUrl);
            byte[] excelBytes = HttpUtil.downloadBytes(downloadUrl);
            if (excelBytes == null || excelBytes.length == 0) {
                saveSyncLog(fileName, 0, "FAIL", "下载Excel失败，响应为空");
                return;
            }

            // 4. 解析Excel
            List<CmaCapabilityItem> items = parseExcel(excelBytes);
            int count = items.size();
            logger.info("解析CMA Excel完成，共{}条", count);

            // 5. 清空并批量插入
            baseMapper.delete(null);
            int batchSize = 500;
            for (int i = 0; i < items.size(); i += batchSize) {
                int end = Math.min(i + batchSize, items.size());
                this.saveBatch(items.subList(i, end));
            }

            // 6. 记录成功日志
            saveSyncLog(fileName, count, "SUCCESS", null);
            logger.info("CMA同步完成，共{}条记录，文件={}", count, fileName);

            // 7. 发布更新公告
            publishUpdateNews(fileName, count);

            // 8. 异步补充hcno
            enrichHcnoAsync();

        } catch (Exception e) {
            logger.error("CMA同步异常", e);
            saveSyncLog("", 0, "FAIL", e.getMessage());
        }
    }

    @Override
    public void enrichHcnoAsync() {
        Thread t = new Thread(() -> {
            logger.info("开始异步补充hcno...");
            try {
                List<CmaCapabilityItem> gbItems = baseMapper.selectGbItemsWithoutHcno(5000);
                int updated = 0;
                for (CmaCapabilityItem item : gbItems) {
                    try {
                        String hcno = fetchHcnoByCode(item.getStandardCode());
                        if (hcno != null) {
                            CmaCapabilityItem upd = new CmaCapabilityItem();
                            upd.setId(item.getId());
                            upd.setHcno(hcno);
                            baseMapper.updateById(upd);
                            updated++;
                        }
                        Thread.sleep(600);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        logger.warn("hcno查询失败: code={}, err={}", item.getStandardCode(), e.getMessage());
                    }
                }
                logger.info("hcno补充完成，更新{}条", updated);
            } catch (Exception e) {
                logger.error("hcno异步补充异常", e);
            }
        }, "cma-hcno-enrichment");
        t.setDaemon(true);
        t.start();
    }

    private String fetchHcnoByCode(String code) {
        try {
            String url = OPENSTD_LIST_URL + URLEncoder.encode(code, "UTF-8");
            String html = StandardHander.sendGet(url, "UTF-8");
            if (StringUtils.isBlank(html)) return null;
            Document doc = Jsoup.parse(html);
            Elements links = doc.select("a[href*=hcno=]");
            for (Element link : links) {
                String href = link.attr("href");
                Matcher m = HCNO_PATTERN.matcher(href);
                if (m.find()) return m.group(1).toUpperCase();
            }
            Matcher m = HCNO_PATTERN.matcher(html);
            if (m.find()) return m.group(1).toUpperCase();
        } catch (Exception e) {
            logger.warn("fetchHcno异常: {}", e.getMessage());
        }
        return null;
    }

    private List<CmaCapabilityItem> parseExcel(byte[] excelBytes) throws Exception {
        List<CmaCapabilityItem> items = new ArrayList<>();
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes));
        XSSFSheet sheet = workbook.getSheetAt(0);
        int lastRow = sheet.getLastRowNum();
        for (int rowIdx = 1; rowIdx <= lastRow; rowIdx++) {
            XSSFRow row = sheet.getRow(rowIdx);
            if (row == null) continue;
            String domain = getCellValue(row.getCell(0));
            String standardName = getCellValue(row.getCell(1));
            String standardCode = getCellValue(row.getCell(2));
            String remarks = getCellValue(row.getCell(3));
            if (StringUtils.isBlank(standardName)) continue;
            CmaCapabilityItem item = new CmaCapabilityItem();
            item.setDomain(StringUtils.defaultString(domain, ""));
            item.setStandardName(standardName);
            item.setStandardCode(StringUtils.defaultString(standardCode, ""));
            item.setRemarks(StringUtils.isBlank(remarks) ? null : remarks);
            items.add(item);
        }
        return items;
    }

    private String getCellValue(XSSFCell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING: return cell.getStringCellValue().trim();
            case Cell.CELL_TYPE_NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            default: return "";
        }
    }

    private void publishUpdateNews(String fileName, int count) {
        try {
            NewsBean news = new NewsBean();
            news.setNextNum(getNextNewsNum());
            news.setType(0);
            news.setTitle("【一单一库更新】CMA能力项目库已更新（" + LocalDate.now() + "）");
            news.setContent("CMA能力项目库于 " + LocalDate.now() + " 发布新版本（" + fileName + "），"
                    + "本次共收录标准 " + count + " 条，数据已自动同步完成。\n"
                    + "如需查阅，请前往【知识管理 → 一单一库】进行搜索。");
            news.setPublishDept("系统管理");
            news.setPublishUser("system");
            news.setPublishDate(new Date());
            newsService.save(news);
            logger.info("已发布CMA更新公告");
        } catch (Exception e) {
            logger.warn("发布CMA更新公告失败: {}", e.getMessage());
        }
    }

    private Integer getNextNewsNum() {
        try {
            QueryWrapper<NewsBean> qw = new QueryWrapper<>();
            qw.orderByDesc("next_num");
            qw.last("LIMIT 1");
            NewsBean last = newsService.getOne(qw);
            return last != null && last.getNextNum() != null ? last.getNextNum() + 1 : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    private void saveSyncLog(String fileName, int count, String status, String message) {
        CmaSyncLog log = new CmaSyncLog();
        log.setFileName(StringUtils.defaultString(fileName, ""));
        log.setItemCount(count);
        log.setStatus(status);
        log.setMessage(message != null && message.length() > 1000 ? message.substring(0, 1000) : message);
        log.setSyncTime(new Date());
        cmaSyncLogMapper.insert(log);
    }
}
