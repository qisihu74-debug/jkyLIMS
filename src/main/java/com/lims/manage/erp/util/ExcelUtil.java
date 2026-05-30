package com.lims.manage.erp.util;

import com.aspose.words.HorizontalAlignment;
import com.lims.manage.erp.vo.PlanInfoImportVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Excel操作工具类
 *
 * @author : zhq
 * @version : V1.0
 * @date :   2023-03-06
 */
@Slf4j
public class ExcelUtil {

    /**
     * Excel导入支持的文件类型
     */
    private static List<String> SUFFIX_NAME_ARRAY = new ArrayList<>(1);

    static {
        SUFFIX_NAME_ARRAY.add(".xls");
    }

    private static List<String> EXCEL_CELL_TYPE_DOUBLE = new ArrayList<>(3);

    static {
        EXCEL_CELL_TYPE_DOUBLE.add("0_);[Red]\\(0\\)");
        EXCEL_CELL_TYPE_DOUBLE.add("General");
        EXCEL_CELL_TYPE_DOUBLE.add("@");
    }

    /**
     * Excel生成模板
     *
     * @param sheetName   模板名称
     * @param columnNames 列名称
     * @param isNullAbles 是否为空
     */
    public static HSSFWorkbook createTemplate(String sheetName, List<String> columnNames, List<Boolean> isNullAbles) {
        // 声明一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet(sheetName);
        // 设置默认列宽
        sheet.setDefaultColumnWidth(18);

        // 生成必填字段字体样式
        HSSFFont fontRequired = workbook.createFont();
        fontRequired.setColor(HSSFColor.RED.index);

        // 生成默认样式
        HSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);

        // 创建标题行
        HSSFRow row = sheet.createRow(0);
        for (int i = 0; i < columnNames.size(); i++) {
            // 创建列
            HSSFCell cell = row.createCell(i);
            cell.setCellStyle(style);
            if (isNullAbles.get(i)) {
                HSSFRichTextString hts = new HSSFRichTextString("*" + columnNames.get(i));
                hts.applyFont(0, 1, fontRequired);
                cell.setCellValue(hts);
            } else {
                cell.setCellValue(columnNames.get(i));
            }
        }
        return workbook;
    }


    /**
     * Excel解析-计划结果导入
     */
    public static List<PlanInfoImportVo> importPlanExcel(HttpServletRequest request) {
        List<PlanInfoImportVo> planInfoImportVoList = new ArrayList<>();
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile file = entity.getValue();
            // 获取文件格式
            String fileName = file.getOriginalFilename();
            if (StringUtils.isNotBlank(fileName)) {
                String suffixName = fileName.substring(fileName.lastIndexOf("."));
                // 判断是否为设备通道Excel导入支持的文件类型
                if (SUFFIX_NAME_ARRAY.contains(suffixName)) {
                    try {
                        HSSFWorkbook workbook = new HSSFWorkbook(file.getInputStream());
                        // 循环获取所有工作簿
                        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                            HSSFSheet sheet = workbook.getSheetAt(sheetIndex);
                            // 处理当前工作簿,循环读取每一行
                            for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
                                HSSFRow hssfRow = sheet.getRow(rowNum);
                                // 排除空行
                                if (hssfRow != null) {
                                    // 排除表头,从第二行开始解析数据
                                    if (rowNum != 0) {
                                        verificationDeviceChannel(hssfRow, planInfoImportVoList);
                                    }
                                } else {
                                    log.error("文件流解析工作簿{}第{}行为空！", sheet.getSheetName(), rowNum);
                                    // 跳出当前工作簿解析
                                    break;
                                }
                            }
                        }
                    } catch (IOException e) {
                        log.error("文件流解析失败！{}", e.getMessage());
                    }
                } else {
                    log.error("暂不支持其他格式文件上传，请重新核对设备通道导入模板！");
                }
            }
        }
        return planInfoImportVoList;
    }

    /**
     * 验证解析Excel并入库
     *
     * @param row 行
     */
    private static void verificationDeviceChannel(HSSFRow row, List<PlanInfoImportVo> planInfoImportVoList) {
        int num = 0;

        PlanInfoImportVo planInfoImport = new PlanInfoImportVo();
        boolean success = true;
        // 获取用户名称
        if (row.getCell(num) != null) {
            String name = row.getCell(num).getStringCellValue().trim();
            if (StringUtils.isNotBlank(name)) {
                planInfoImport.setName(name);
            }
        }
        num++;

        // 获取用户账号
        if (row.getCell(num) != null) {
            String username = row.getCell(num).getStringCellValue().trim();
            if (StringUtils.isNotBlank(username)) {
                planInfoImport.setUsername(username);
            }
        }
        num++;

        // 获取用户表现
        if (row.getCell(num) != null) {
            String expression = row.getCell(num).getStringCellValue().trim();
            if (StringUtils.isNotBlank(expression)) {
                planInfoImport.setExpression(expression);
            }
        }
        num++;

        // 获取用户成绩
        if (row.getCell(num) != null) {
            double score = row.getCell(num).getNumericCellValue();
            planInfoImport.setScore(score);
        }
        num++;

        // 获取用户积分
        if (row.getCell(num) != null) {
            Integer integralNum = (int) row.getCell(num).getNumericCellValue();
            planInfoImport.setIntegralNum(integralNum);
        }
        planInfoImportVoList.add(planInfoImport);
    }

    /**
     * 获取Excel的String值
     *
     * @param cell 列对象
     * @return String
     */
    private static String getStringVal(HSSFCell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                if (EXCEL_CELL_TYPE_DOUBLE.contains(cell.getCellStyle().getDataFormatString())) {
                    if (cell.toString().contains(".") && !cell.toString().contains(".0")) {
                        if (cell.toString().length() > 10) {
                            return new DecimalFormat("0").format(cell.getNumericCellValue()).trim();
                        } else {
                            return String.valueOf(cell.getNumericCellValue()).trim();
                        }
                    } else {
                        return new DecimalFormat("0").format(cell.getNumericCellValue()).trim();
                    }
                } else {
                    return String.valueOf(cell.getNumericCellValue()).trim();
                }
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue().trim();
            default:
                return "";
        }
    }


}
