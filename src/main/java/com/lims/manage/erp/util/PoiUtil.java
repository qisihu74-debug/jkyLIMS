package com.lims.manage.erp.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFonts;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGridCol;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblLayoutType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblLayoutType;

import java.math.BigInteger;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.util
 * @desc
 * @date 2022/7/8 11:48
 * @Copyright © 河南交科院
 */
public class PoiUtil {
    private static String notpath = "/evn_ets_rescueprogramme/moduleflag-1/";

    public static void setExcelVertical(XWPFDocument document, String dirPath) {
        List<XWPFTable> tables = document.getTables();
        if (tables != null && tables.size() > 1) {
            for (int z = 1; z < tables.size(); z++) {
                XWPFTable table = tables.get(z);
                CTTblLayoutType type = table.getCTTbl().getTblPr().addNewTblLayout();
                type.setType(STTblLayoutType.FIXED);
                //设置表格居中
                CTJc jc = table.getCTTbl().getTblPr().getJc();
                if (jc == null) {
                    jc = table.getCTTbl().getTblPr().addNewJc();
                }
                jc.setVal(STJc.CENTER);
                table.getCTTbl().getTblPr().setJc(jc);

                //设置表格左对齐缩进
                /*CTTblWidth tblInd = table.getCTTbl().getTblPr().getTblInd();
                if(null == tblInd){
                    tblInd = table.getCTTbl().getTblPr().addNewTblInd();
                }
                //缩进距离
                tblInd.setW(BigInteger.valueOf(170));
                table.getCTTbl().getTblPr().setTblInd(tblInd);*/
                /**************设置表格样式*****************/
                if ("/evn_ets_rescueprogramme/moduleflag-3/".equals(dirPath)) {
                    List<CTTblGridCol> gridColList = table.getCTTbl().getTblGrid().getGridColList();
                    if (CollectionUtils.isNotEmpty(gridColList)) {
                        //根据不同列长度，设置不同列宽，具体宽度以提供的模板为准
                        if (gridColList.size() == 5) {
                            CTTblGrid ctTblGrid = table.getCTTbl().addNewTblGrid();
                            ctTblGrid.addNewGridCol().setW(BigInteger.valueOf(1222));
                            ctTblGrid.addNewGridCol().setW(BigInteger.valueOf(1609));
                            ctTblGrid.addNewGridCol().setW(BigInteger.valueOf(2210));
                            ctTblGrid.addNewGridCol().setW(BigInteger.valueOf(1927));
                            ctTblGrid.addNewGridCol().setW(BigInteger.valueOf(1255));
                            table.getCTTbl().setTblGrid(ctTblGrid);
                        } else {
                            CTTblGrid ctTblGrid = table.getCTTbl().addNewTblGrid();
                            ctTblGrid.addNewGridCol().setW(BigInteger.valueOf(1211));
                            ctTblGrid.addNewGridCol().setW(BigInteger.valueOf(875));
                            ctTblGrid.addNewGridCol().setW(BigInteger.valueOf(2493));
                            ctTblGrid.addNewGridCol().setW(BigInteger.valueOf(821));
                            ctTblGrid.addNewGridCol().setW(BigInteger.valueOf(1578));
                            ctTblGrid.addNewGridCol().setW(BigInteger.valueOf(1257));
                            table.getCTTbl().setTblGrid(ctTblGrid);
                        }
                    }

                    //参考博客：https://blog.csdn.net/qq_31189355/article/details/80438506
                    CTTblBorders borders = table.getCTTbl().getTblPr().addNewTblBorders();
                    //表格内部横向表格
                    CTBorder hBorder = borders.addNewInsideH();
                    // 线条类型（格式参考：创建word文档，参入最终需要的结果表格，另存xml文件，查看xml文件中的具体格式）
                    hBorder.setVal(STBorder.Enum.forString("single"));
                    // 线条大小
                    hBorder.setSz(new BigInteger("12"));
                    // 设置颜色
                    hBorder.setColor("auto");
                    // 表格内部纵向表格
                    CTBorder vBorder = borders.addNewInsideV();
                    vBorder.setVal(STBorder.Enum.forString("single"));
                    vBorder.setSz(new BigInteger("12"));
                    vBorder.setColor("auto");
                    //表格最左边一条线的样式
                    CTBorder lBorder = borders.addNewLeft();
                    lBorder.setVal(STBorder.Enum.forString("thinThickSmallGap"));
                    lBorder.setSz(new BigInteger("12"));
                    lBorder.setColor("auto");
                    //表格最上边一条线（顶部）的样式
                    CTBorder tBorder = borders.addNewTop();
                    tBorder.setVal(STBorder.Enum.forString("thinThickSmallGap"));
                    tBorder.setSz(new BigInteger("12"));
                    tBorder.setColor("auto");
                    //表格最右边一条线的样式
                    CTBorder rBorder = borders.addNewRight();
                    rBorder.setVal(STBorder.Enum.forString("thickThinSmallGap"));
                    rBorder.setSz(new BigInteger("12"));
                    rBorder.setColor("auto");
                    //表格最下边一条线（底部）的样式
                    CTBorder bBorder = borders.addNewBottom();
                    bBorder.setVal(STBorder.Enum.forString("thickThinSmallGap"));
                    bBorder.setSz(new BigInteger("12"));
                    bBorder.setColor("auto");
                }
                /**************设置表格样式**************/
                List<XWPFTableRow> rows = table.getRows();
                if (rows != null && rows.size() > 0) {
                    for (int i = 0; i < rows.size(); i++) {
                        XWPFTableRow row = rows.get(i);
                        //for (XWPFTableRow row : rows) {
                        //设置表格内容不允许跨页换行
                        row.setCantSplitRow(true);
                        if (i == 0) {
                            //设置表格标题在每一页显示
                            row.setRepeatHeader(true);
                        }
                        if (notpath.indexOf(dirPath) < 0) {
                            List<XWPFTableCell> cells = row.getTableCells();
                            if (cells != null && cells.size() > 0) {
                                for (XWPFTableCell cell : cells) {
                                    if (StringUtils.isNotBlank(cell.getText())) {
                                        //垂直居中
                                        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                                    }
                                }
                            }
                        }
                        //字体处理
                        if (("/evn_ets_rescueprogramme/moduleflag-3/".equals(dirPath)) && i > 0) {
                            List<XWPFTableCell> cells = row.getTableCells();
                            if (cells != null && cells.size() > 0) {
                                for (XWPFTableCell cell : cells) {
                                    List<XWPFParagraph> paragraphs = cell.getParagraphs();
                                    for (int j = 0; j < paragraphs.size(); j++) {
                                        XWPFParagraph xwpfParagraph = paragraphs.get(j);
                                        List<XWPFRun> runs = xwpfParagraph.getRuns();
                                        for (int k = 0; k < runs.size(); k++) {
                                            XWPFRun xwpfRun = runs.get(k);
                                            CTFonts font = xwpfRun.getCTR().addNewRPr().addNewRFonts();
                                            //中文
                                            font.setEastAsia("仿宋_GB2312"); //改变中文字体设置这个
                                            // ASCII
                                            font.setAscii("Times New Roman"); //改变数字或者英文字体需要设置这个
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
