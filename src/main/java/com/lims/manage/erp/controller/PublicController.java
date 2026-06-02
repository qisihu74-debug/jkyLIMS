package com.lims.manage.erp.controller;

import com.lims.manage.erp.mapper.PublicQueryDao;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 客户免登录查询（公开接口，Shiro 白名单 anon）。
 * 鉴权：委托编号 + 委托电话(entrust_phone) 匹配。仅返回只读进度/报告。
 * 限流：建议在 Nginx 层做（见需求文档）；本类不做计数器以免 TTL 处理不当误封。
 */
@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private PublicQueryDao publicQueryDao;
    @Autowired
    private RedisUtils redisUtils;

    private static final String DL_PREFIX = "public:dl:";
    private static final long DL_TTL = 600L; // 下载 token 有效期 10 分钟

    /** 委托编号+电话 匹配，返回委托行或 null */
    private Map<String, Object> auth(Map<String, String> body) {
        if (body == null) return null;
        String noStr = body.get("entrustmentNo");
        String phone = body.get("phone");
        if (noStr == null || phone == null) return null;
        noStr = noStr.trim();
        phone = phone.trim();
        if (noStr.isEmpty() || phone.isEmpty()) return null;
        Integer no;
        try {
            no = Integer.parseInt(noStr);
        } catch (NumberFormatException e) {
            return null;
        }
        return publicQueryDao.matchEntrust(no, phone);
    }

    @PostMapping("/queryEntrustProgress")
    public Result queryEntrustProgress(@RequestBody Map<String, String> body) {
        Map<String, Object> e = auth(body);
        if (e == null || e.get("id") == null) {
            return ResultUtil.error(403, "委托编号与电话不匹配");
        }
        Long eid = ((Number) e.get("id")).longValue();
        Map<String, Object> agg = publicQueryDao.progressAgg(eid);
        if (agg == null) agg = new HashMap<>();
        long sampleCount = num(agg.get("sampleCount"));
        long reportCount = num(agg.get("reportCount"));
        long issued = num(agg.get("issuedCount"));
        String acc = str(e.get("acceptanceDate"));
        if (acc == null) acc = str(e.get("createTime"));
        String verifyTime = str(agg.get("verifyTime"));
        String sealTime = str(agg.get("sealTime"));
        String issuerTime = str(agg.get("issuerTime"));

        List<Map<String, Object>> timeline = new ArrayList<>();
        timeline.add(stage("received", "已受理", acc, acc != null));
        timeline.add(stage("sampling", "样品登记", acc, sampleCount > 0));
        timeline.add(stage("testing", "试验检测", verifyTime, reportCount > 0));
        timeline.add(stage("verify", "校核审核", verifyTime, verifyTime != null));
        timeline.add(stage("report_sealing", "报告盖章", sealTime, sealTime != null));
        timeline.add(stage("mailing", "报告签发", issuerTime, issuerTime != null || issued > 0));

        int done = 0;
        String curName = "已受理";
        for (Map<String, Object> s : timeline) {
            if (Boolean.TRUE.equals(s.get("done"))) {
                done++;
                curName = (String) s.get("name");
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("entrustmentNo", String.valueOf(e.get("entrustmentNo")));
        data.put("entrustCompany", e.get("entrustCompany"));
        data.put("projectName", e.get("projectName"));
        data.put("createTime", str(e.get("createTime")));
        data.put("currentStageName", curName);
        data.put("progressPercent", (int) Math.round(done * 100.0 / timeline.size()));
        data.put("sampleCount", sampleCount);
        data.put("reportCount", reportCount);
        data.put("issuedReportCount", issued);
        data.put("timeline", timeline);
        return ResultUtil.success(data);
    }

    @PostMapping("/queryReports")
    public Result queryReports(@RequestBody Map<String, String> body) {
        Map<String, Object> e = auth(body);
        if (e == null || e.get("id") == null) {
            return ResultUtil.error(403, "委托编号与电话不匹配");
        }
        Long eid = ((Number) e.get("id")).longValue();
        List<Map<String, Object>> reports = publicQueryDao.reportList(eid);
        List<Map<String, Object>> out = new ArrayList<>();
        if (reports != null) {
            for (Map<String, Object> r : reports) {
                Object rid = r.get("reportId");
                String token = UUID.randomUUID().toString().replace("-", "");
                redisUtils.set(DL_PREFIX + token, String.valueOf(rid), DL_TTL);
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("reportCode", r.get("reportCode"));
                m.put("reportType", r.get("reportType"));
                m.put("reportTime", r.get("reportTime"));
                m.put("token", token);
                out.add(m);
            }
        }
        return ResultUtil.success(out);
    }

    @GetMapping("/downloadReport")
    public void downloadReport(@RequestParam(value = "token", required = false) String token,
                               HttpServletResponse response) throws Exception {
        if (token == null || token.trim().isEmpty()) {
            response.sendError(403, "缺少 token");
            return;
        }
        Object rid = redisUtils.get(DL_PREFIX + token.trim());
        if (rid == null) {
            response.sendError(403, "下载链接已失效，请重新查询");
            return;
        }
        Long id;
        try {
            id = Long.parseLong(String.valueOf(rid));
        } catch (Exception ex) {
            response.sendError(403, "无效 token");
            return;
        }
        String url = publicQueryDao.reportUrl(id);
        if (url == null || url.trim().isEmpty()) {
            response.sendError(404, "报告文件不存在");
            return;
        }
        response.sendRedirect(url);
    }

    private static Map<String, Object> stage(String stage, String name, String time, boolean done) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("stage", stage);
        m.put("name", name);
        m.put("time", time);
        m.put("done", done);
        return m;
    }

    private static long num(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).longValue();
        try {
            return Long.parseLong(String.valueOf(o));
        } catch (Exception e) {
            return 0;
        }
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
