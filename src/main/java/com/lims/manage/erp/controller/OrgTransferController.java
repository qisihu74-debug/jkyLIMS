package com.lims.manage.erp.controller;

import com.lims.manage.erp.mapper.OrgTransferDao;
import com.lims.manage.erp.mapper.SysUserDao;
import com.lims.manage.erp.entity.SysUserEntity;
import com.lims.manage.erp.result.Result;
import com.lims.manage.erp.result.ResultUtil;
import com.lims.manage.erp.util.GenID;
import com.lims.manage.erp.util.ShiroUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 组织架构（部门 / 用户 / 角色）一键导出 / 导入。
 * - 导出：单个 .xlsx，三个 Sheet。ID 列写文本以保留 bigint 精度。
 * - 导入：增量 upsert（只增和改、不删除）；用户仅按用户名更新已有账号并“只增不减”地分配角色。
 *   提供预览（不写库）与执行两个接口。
 */
@RestController
@RequestMapping("/org")
public class OrgTransferController {

    @Autowired
    private OrgTransferDao orgTransferDao;
    @Autowired
    private SysUserDao sysUserDao;

    private static final String SHEET_DEPT = "部门";
    private static final String SHEET_ROLE = "角色";
    private static final String SHEET_USER = "用户";

    private static final String[] DEPT_HEADER = {"部门ID", "上级部门ID", "部门名称", "部门编码", "负责人", "备注", "排序"};
    private static final String[] ROLE_HEADER = {"角色ID", "角色名称", "角色说明", "角色大类", "优先级"};
    private static final String[] USER_HEADER = {"用户名", "姓名", "手机", "岗位", "邮箱", "角色(逗号分隔)"};

    private boolean notManager() {
        SysUserEntity u = ShiroUtils.getUserInfo();
        return u == null || u.getUserId() == null || sysUserDao.isManagerOrAbove(u.getUserId()) == 0;
    }

    /** 一键下载：导出当前组织架构为 .xlsx */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        if (notManager()) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"msg\":\"权限不足\",\"data\":null}");
            return;
        }
        XSSFWorkbook wb = new XSSFWorkbook();

        Sheet ds = wb.createSheet(SHEET_DEPT);
        writeHeader(ds, DEPT_HEADER);
        int r = 1;
        for (Map<String, Object> d : orgTransferDao.listDepts()) {
            Row row = ds.createRow(r++);
            text(row, 0, str(d.get("id")));
            text(row, 1, str(d.get("parentId")));
            text(row, 2, str(d.get("name")));
            text(row, 3, str(d.get("code")));
            text(row, 4, str(d.get("userName")));
            text(row, 5, str(d.get("remark")));
            text(row, 6, str(d.get("serialNumber")));
        }

        Sheet rs = wb.createSheet(SHEET_ROLE);
        writeHeader(rs, ROLE_HEADER);
        r = 1;
        for (Map<String, Object> ro : orgTransferDao.listRoles()) {
            Row row = rs.createRow(r++);
            text(row, 0, str(ro.get("roleId")));
            text(row, 1, str(ro.get("roleName")));
            text(row, 2, str(ro.get("roleRemark")));
            text(row, 3, str(ro.get("roleType")));
            text(row, 4, str(ro.get("priority")));
        }

        Sheet us = wb.createSheet(SHEET_USER);
        writeHeader(us, USER_HEADER);
        r = 1;
        for (Map<String, Object> u : orgTransferDao.listUsersWithRoles()) {
            Row row = us.createRow(r++);
            text(row, 0, str(u.get("username")));
            text(row, 1, str(u.get("name")));
            text(row, 2, str(u.get("mobile")));
            text(row, 3, str(u.get("position")));
            text(row, 4, str(u.get("email")));
            text(row, 5, str(u.get("roles")));
        }

        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String fileName = URLEncoder.encode("组织架构_" + date + ".xlsx", "UTF-8").replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ";filename*=UTF-8''" + fileName);
        ServletOutputStream os = response.getOutputStream();
        wb.write(os);
        os.flush();
    }

    /** 导入预览：解析上传文件并与库内现状比对，不写库 */
    @PostMapping("/importPreview")
    public Result importPreview(@RequestParam("file") MultipartFile file) {
        if (notManager()) {
            return ResultUtil.error(403, "权限不足");
        }
        try {
            return ResultUtil.success("预览成功", process(file, false));
        } catch (Exception e) {
            return ResultUtil.error(500, "解析失败：" + e.getMessage());
        }
    }

    /** 导入执行：增量 upsert 写库 */
    @PostMapping("/importApply")
    @Transactional(rollbackFor = Exception.class)
    public Result importApply(@RequestParam("file") MultipartFile file) {
        if (notManager()) {
            return ResultUtil.error(403, "权限不足");
        }
        try {
            return ResultUtil.success("导入成功", process(file, true));
        } catch (Exception e) {
            throw new RuntimeException("导入失败：" + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> process(MultipartFile file, boolean apply) throws Exception {
        Map<String, Object> out = new LinkedHashMap<>();
        InputStream is = file.getInputStream();
        XSSFWorkbook wb = new XSSFWorkbook(is);

        out.put("dept", processDept(wb.getSheet(SHEET_DEPT), apply));
        out.put("role", processRole(wb.getSheet(SHEET_ROLE), apply));
        out.put("user", processUser(wb.getSheet(SHEET_USER), apply));
        out.put("apply", apply);

        is.close();
        return out;
    }

    private Map<String, Object> processDept(Sheet sheet, boolean apply) {
        List<String> insert = new ArrayList<>();
        List<String> update = new ArrayList<>();
        List<String> error = new ArrayList<>();
        int unchanged = 0;
        if (sheet != null) {
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String idStr = cellStr(row.getCell(0));
                String name = cellStr(row.getCell(2));
                if (idStr.isEmpty() && name.isEmpty()) continue;
                if (name.isEmpty()) {
                    error.add("第" + (i + 1) + "行：部门名称为空，已跳过");
                    continue;
                }
                Long parentId = parseLong(cellStr(row.getCell(1)));
                String code = cellStr(row.getCell(3));
                String userName = cellStr(row.getCell(4));
                String remark = cellStr(row.getCell(5));
                Integer serial = parseInt(cellStr(row.getCell(6)));
                Long id = parseLong(idStr);

                Map<String, Object> m = new LinkedHashMap<>();
                m.put("parentId", parentId);
                m.put("name", name);
                m.put("code", emptyToNull(code));
                m.put("userName", emptyToNull(userName));
                m.put("remark", emptyToNull(remark));
                m.put("serialNumber", serial);

                if (id == null) {
                    insert.add(name);
                    if (apply) {
                        m.put("id", GenID.getID());
                        orgTransferDao.insertDept(m);
                    }
                } else {
                    Map<String, Object> cur = orgTransferDao.getDeptById(id);
                    if (cur == null) {
                        error.add("第" + (i + 1) + "行：部门ID " + id + " 不存在；如需新增请清空ID列");
                        continue;
                    }
                    m.put("id", id);
                    if (deptSame(cur, m)) {
                        unchanged++;
                    } else {
                        update.add(name);
                        if (apply) orgTransferDao.updateDept(m);
                    }
                }
            }
        }
        return section(insert, update, unchanged, error);
    }

    private Map<String, Object> processRole(Sheet sheet, boolean apply) {
        List<String> insert = new ArrayList<>();
        List<String> update = new ArrayList<>();
        List<String> error = new ArrayList<>();
        int unchanged = 0;
        if (sheet != null) {
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String idStr = cellStr(row.getCell(0));
                String name = cellStr(row.getCell(1));
                if (idStr.isEmpty() && name.isEmpty()) continue;
                if (name.isEmpty()) {
                    error.add("第" + (i + 1) + "行：角色名称为空，已跳过");
                    continue;
                }
                String remark = cellStr(row.getCell(2));
                String type = cellStr(row.getCell(3));
                Integer priority = parseInt(cellStr(row.getCell(4)));
                Long id = parseLong(idStr);

                Map<String, Object> m = new LinkedHashMap<>();
                m.put("roleName", name);
                m.put("roleRemark", emptyToNull(remark));
                m.put("roleType", emptyToNull(type));
                m.put("priority", priority);

                if (id == null) {
                    insert.add(name);
                    if (apply) {
                        if (m.get("priority") == null) m.put("priority", 100);
                        orgTransferDao.insertRole(m);
                    }
                } else {
                    Map<String, Object> cur = orgTransferDao.getRoleById(id);
                    if (cur == null) {
                        error.add("第" + (i + 1) + "行：角色ID " + id + " 不存在；如需新增请清空ID列");
                        continue;
                    }
                    m.put("roleId", id);
                    if (roleSame(cur, m)) {
                        unchanged++;
                    } else {
                        update.add(name);
                        if (apply) orgTransferDao.updateRole(m);
                    }
                }
            }
        }
        return section(insert, update, unchanged, error);
    }

    private Map<String, Object> processUser(Sheet sheet, boolean apply) {
        List<String> update = new ArrayList<>();
        List<String> roleAdd = new ArrayList<>();
        List<String> skip = new ArrayList<>();
        List<String> error = new ArrayList<>();
        int unchanged = 0;
        if (sheet != null) {
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String username = cellStr(row.getCell(0));
                if (username.isEmpty()) continue;
                Map<String, Object> cur = orgTransferDao.getUserByUsername(username);
                if (cur == null) {
                    skip.add(username + "（用户名不存在，不新建）");
                    continue;
                }
                Long userId = ((Number) cur.get("userId")).longValue();
                String name = cellStr(row.getCell(1));
                String mobile = cellStr(row.getCell(2));
                String position = cellStr(row.getCell(3));
                String email = cellStr(row.getCell(4));

                Map<String, Object> m = new LinkedHashMap<>();
                m.put("userId", userId);
                m.put("name", emptyToNull(name));
                m.put("mobile", emptyToNull(mobile));
                m.put("position", emptyToNull(position));
                m.put("email", emptyToNull(email));
                boolean basicChanged = !eq(str(cur.get("name")), name)
                        || !eq(str(cur.get("mobile")), mobile)
                        || !eq(str(cur.get("position")), position)
                        || !eq(str(cur.get("email")), email);
                if (basicChanged) {
                    update.add(username);
                    if (apply) orgTransferDao.updateUserBasic(m);
                } else {
                    unchanged++;
                }

                String rolesStr = cellStr(row.getCell(5));
                if (!rolesStr.isEmpty()) {
                    // 注意：角色名本身可能含“/”“、”“;”等（如“业务员/样品管理员”“安全、内务管理员”），
                    // 故仅以逗号分隔（导出用 GROUP_CONCAT 逗号；已确认无角色名含逗号）
                    String[] parts = rolesStr.split("[,，]");
                    for (String p : parts) {
                        String rn = p.trim();
                        if (rn.isEmpty()) continue;
                        Long rid = orgTransferDao.roleIdByName(rn);
                        if (rid == null) {
                            error.add(username + "：角色“" + rn + "”不存在");
                            continue;
                        }
                        if (orgTransferDao.countUserRole(userId, rid) == 0) {
                            roleAdd.add(username + " + " + rn);
                            if (apply) orgTransferDao.addUserRole(userId, rid);
                        }
                    }
                }
            }
        }
        Map<String, Object> sec = new LinkedHashMap<>();
        sec.put("update", update);
        sec.put("roleAdd", roleAdd);
        sec.put("skip", skip);
        sec.put("unchanged", unchanged);
        sec.put("error", error);
        return sec;
    }

    // ---------------- helpers ----------------
    private Map<String, Object> section(List<String> insert, List<String> update, int unchanged, List<String> error) {
        Map<String, Object> sec = new LinkedHashMap<>();
        sec.put("insert", insert);
        sec.put("update", update);
        sec.put("unchanged", unchanged);
        sec.put("error", error);
        return sec;
    }

    private boolean deptSame(Map<String, Object> cur, Map<String, Object> m) {
        return eq(str(cur.get("parentId")), str(m.get("parentId")))
                && eq(str(cur.get("name")), str(m.get("name")))
                && eq(str(cur.get("code")), str(m.get("code")))
                && eq(str(cur.get("userName")), str(m.get("userName")))
                && eq(str(cur.get("remark")), str(m.get("remark")))
                && eq(str(cur.get("serialNumber")), str(m.get("serialNumber")));
    }

    private boolean roleSame(Map<String, Object> cur, Map<String, Object> m) {
        return eq(str(cur.get("roleName")), str(m.get("roleName")))
                && eq(str(cur.get("roleRemark")), str(m.get("roleRemark")))
                && eq(str(cur.get("roleType")), str(m.get("roleType")))
                && eq(str(cur.get("priority")), str(m.get("priority")));
    }

    private void writeHeader(Sheet sheet, String[] headers) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            text(row, i, headers[i]);
        }
    }

    private void text(Row row, int col, String value) {
        Cell c = row.createCell(col);
        c.setCellValue(value == null ? "" : value);
    }

    private String cellStr(Cell c) {
        if (c == null) return "";
        int t = c.getCellType();
        if (t == Cell.CELL_TYPE_STRING) return c.getStringCellValue().trim();
        if (t == Cell.CELL_TYPE_NUMERIC) {
            double d = c.getNumericCellValue();
            if (d == Math.floor(d) && !Double.isInfinite(d)) return String.valueOf((long) d);
            return String.valueOf(d);
        }
        if (t == Cell.CELL_TYPE_BOOLEAN) return String.valueOf(c.getBooleanCellValue());
        if (t == Cell.CELL_TYPE_FORMULA) {
            try {
                return c.getStringCellValue().trim();
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }

    private String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private boolean eq(String a, String b) {
        return str(a).trim().equals(str(b).trim());
    }

    private String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    private Long parseLong(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInt(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
