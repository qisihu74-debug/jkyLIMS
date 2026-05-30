package com.lims.manage.erp.util;

import com.itextpdf.awt.geom.Rectangle2D;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.util
 * @desc
 * @date 2022/6/29 10:23
 * @Copyright © 河南交科院
 */
public class ItexUtil {
    private static List<String> resu = null;
    /**
     * 替换指定文字为白色的遮罩层
     * @param src 需要被转换的带全路径文件名
     * @param dest 转换之后pdf的带全路径文件名
     * @param replaceStr 指定转换的文字(水印的文字)
     */
    public static void replaceStr(String src, String dest, String replaceStr) {
        try {
            resu = new ArrayList<>();
            PdfReader reader = new PdfReader(src);
            //获取指定文字的坐标(就是上一步生成的pdf水印)
            getKeyWords(reader,replaceStr);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
            for (int i = 1; i <= resu.size(); i++) {
                String xy = resu.get(i - 1);
                PdfContentByte canvas = stamper.getOverContent(i);
                canvas.saveState();
                //设置颜色
                canvas.setColorFill(BaseColor.WHITE);
                //解析坐标
                float x = Float.valueOf(xy.split("--")[0]);
                float y = Float.valueOf(xy.split("--")[1]) - 10;
                //后面2个参数分别是宽高
                canvas.rectangle(x, y, 450, 100);
                canvas.fill();
                canvas.restoreState();
            }
            stamper.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取指定文字的坐标
     */
    private static void getKeyWords(PdfReader pdfReader,String replaceStr) {
        try {
            int pageNum = pdfReader.getNumberOfPages();
            PdfReaderContentParser pdfReaderContentParser = new PdfReaderContentParser(pdfReader);
            for (int i = 1; i <= pageNum; i++) {
                pdfReaderContentParser.processContent(i, new RenderListener() {
                    @Override
                    public void renderText(TextRenderInfo textRenderInfo) {
                        String text = textRenderInfo.getText(); // 整页内容
                        if (null != text && text.contains(replaceStr)) {
                            Rectangle2D.Float boundingRectange = textRenderInfo
                                    .getBaseline().getBoundingRectange();
                            String xy = boundingRectange.x + "--" + boundingRectange.y;
                            resu.add(xy);
                        }
                    }

                    @Override
                    public void renderImage(ImageRenderInfo arg0) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void endTextBlock() {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void beginTextBlock() {
                        // TODO Auto-generated method stub
                    }
                });
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
