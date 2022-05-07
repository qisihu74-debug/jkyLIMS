package com.lims.manage.erp.util;

import cn.hutool.core.util.StrUtil;
import com.itextpdf.awt.geom.Rectangle2D;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.lims.manage.erp.entity.TargetWordItem;
import com.lims.manage.erp.entity.TextLocal;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChangePdfText {

    /**
     * 图片插入
     * @param pdfIn
     * @param map
     * @throws Exception
     */
   /* public static void insertPicToPdf(byte[] pdfIn, Map<String,byte[]> map) throws Exception{
        String outPath = "E:\\test.pdf";
        PdfReader reader = new PdfReader(pdfIn);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outPath));

        //关键字，以及替换后的内容及位置
        List<TargetWordItem> keyList = new ArrayList<>();
        keyList.add(new TargetWordItem("检测：", "", 2));
        keyList.add(new TargetWordItem("审核：", "", 2));
        keyList.add(new TargetWordItem("批准：", "", 2));

        //找到的位置，匹配到的关键字
        List<TargetWordItem> keyItemList = matchPage(reader, keyList);

        //修改PDF
        for(int i = 0; i < keyItemList.size(); i++) {
            TargetWordItem keyItem = keyItemList.get(i);
            PdfContentByte overContent = stamper.getOverContent(keyItem.getPageNum());
            *//*
                2.添加图片
             *//*
            Image image = Image.getInstance(imgPath);
            //图片位置
            image.setAbsolutePosition(keyItem.getX() + keyItem.getSize() + 30, keyItem.getY());
            //图片大小
            image.scaleToFit(30, 20);
            overContent.addImage(image);
            overContent.stroke();
        }
        stamper.close();
        reader.close();
        System.out.println("签字成功------");
    }*/

    /**
     * 从PDF中读取内容
     * 内容与关键字比对，如果满足条件，则在匹配内容的指定位置，增加需要显示的内容（替换关键字，修改PDF）
     */
    public static void main(String[] args) throws Exception {

        //String url = "http://121.89.242.0:9000/report-download/1651024332679104.pdf";
        String path = "E:\\222.pdf";
        String outPath = path.replace(".pdf", "_temp.pdf");

        String imgPath = "E:\\gjl.png";

        PdfReader reader = new PdfReader(path);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outPath));

        //关键字，以及替换后的内容及位置
        List<TargetWordItem> keyList = new ArrayList<>();
        keyList.add(new TargetWordItem("检测：", "", 2));
        keyList.add(new TargetWordItem("审核：", "", 2));
        keyList.add(new TargetWordItem("批准：", "", 2));

        //找到的位置，匹配到的关键字
        List<TargetWordItem> keyItemList = matchPage(reader, keyList);

        //修改PDF
        int index = 0;
        for(int i = 0; i < keyItemList.size(); i++) {
            TargetWordItem keyItem = keyItemList.get(i);
            PdfContentByte overContent = stamper.getOverContent(keyItem.getPageNum());
            Image image = Image.getInstance(imgPath);
            //图片位置
            if (keyItem.getKey().equals("检测：")){
                image.setAbsolutePosition(keyItem.getX()+index + keyItem.getSize() + 30, keyItem.getY()+index);
                index++;
            }
            //图片大小
            image.scaleToFit(30, 20);
            overContent.addImage(image);
            overContent.stroke();
        }

        stamper.close();
        reader.close();
        System.out.println("签字成功------");
    }

    /**
     * 满足关键字的位置
     */
    public static List<TargetWordItem> matchPage(PdfReader reader, List<TargetWordItem> keywordList) throws Exception {

        List<TargetWordItem> keyItemList = new ArrayList<>();
        //那些满足关键字
        for (TargetWordItem key : keywordList) {
            List<TextLocal> allItemList = getKeyWord(key.getKey(), reader);
            for (TextLocal pageItem : allItemList) {
                if(Objects.equals(key.getKey(), pageItem.getContent())) {

                    key.setPageNum(pageItem.getPageNum());
                    key.setX(pageItem.getX());
                    key.setY(pageItem.getY());

                    keyItemList.add(key);

                    //找到第一个就结束
                    break;
                }
            }
        }

        return keyItemList;
    }


    /**
     * 获取关键字坐标信息
     * @param keyWord 关键字
     * @return 坐标信息
     * @throws IOException IOException
     */
    private static List<TextLocal> getKeyWord(String keyWord, PdfReader pdfReader) throws IOException {

        List<TextLocal> textLocals = new ArrayList<>();
        //通过指定的RenderListener处理来自PDFReader页面的内容
        PdfReaderContentParser contentParser = new PdfReaderContentParser(pdfReader);
        int pageNum = pdfReader.getNumberOfPages();
        for (int i = 1; i <= pageNum; i++) {
            //用于存放文本信息
            StringBuffer sb = new StringBuffer();
            //使用指定的监听器处理来自指定页码的内容
            int finalI = i;
            contentParser.processContent(i, new RenderListener() {

                /**
                 * 当一个新的文本块打开时调用
                 */
                @Override
                public void beginTextBlock() {

                }

                /**
                 * 当文本应该呈现时调用
                 * @param textRenderInfo 指定要呈现什么信息
                 */
                @Override
                public void renderText(TextRenderInfo textRenderInfo) {
                    //获取整页内容
                    String text = sb.append(textRenderInfo.getText()).toString();
                    if (!StrUtil.isEmpty(text) && text.contains(keyWord)) {
                        //获取坐标
                        Rectangle2D.Float boundingRectangle = textRenderInfo.getBaseline().getBoundingRectange();
                        float x = boundingRectangle.x;
                        float y = boundingRectangle.y;
                        //将关键字添加到关键字列表中
                        TextLocal textLocal = new TextLocal(x, y, finalI);
                        textLocal.setLength(keyWord.length());
                        textLocal.setContent(keyWord);
                        textLocals.add(textLocal);
                        sb.setLength(0);
                    }
                }

                /**
                 * 当一个文本块结束时调用
                 */
                @Override
                public void endTextBlock() {

                }

                /**
                 * 当图像应该被渲染时调用
                 * @param imageRenderInfo 指定要呈现什么信息
                 */
                @Override
                public void renderImage(ImageRenderInfo imageRenderInfo) {

                }
            });
        }

        return textLocals;
    }
}
