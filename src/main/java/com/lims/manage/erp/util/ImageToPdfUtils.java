package com.lims.manage.erp.util;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.ContentByteUtils;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfContentStreamProcessor;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.lims.manage.erp.entity.ImagePro;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletOutputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.util
 * @desc 将印章插入到pdf指定位置（根据pdf中的关键字）
 * @date 2022/1/19 14:57
 * @Copyright © 河南交科院
 */
@Slf4j
public class ImageToPdfUtils {

    /**
     * 图片插入pdf(支持多个印章插入和文字插入)
     * @param inputPDFFilePath  要写入的pdf文件路径
     * @param outPutPDFFilePath 输出的pdf文件路径
     * @param imagePros         要写入的图片的list,包含图片坐标等
     * @param pdfList           要写入的文字的list,包含坐标等
     * @throws Exception
     */
    public static void writeToPdf(String inputPDFFilePath, String outPutPDFFilePath, List<ImagePro> imagePros/*, List<PdfPro> pdfList*/)
            throws Exception {
        //append 追加
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(outPutPDFFilePath), false));
        PdfReader reader = new PdfReader(inputPDFFilePath);
        PdfStamper stamper = new PdfStamper(reader, bos);
        int total = reader.getNumberOfPages() + 1;
        PdfContentByte content;
        // BaseFont base = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.EMBEDDED);
        // "c:\\windows\\fonts\\SIMHEI.TTF" 使用windows系统的黑体
        BaseFont base = BaseFont.createFont("c:\\windows\\fonts\\SIMHEI.TTF", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        PdfGState gs = new PdfGState();
        for (int i = 1; i < total; i++) {
            //content = stamper.getOverContent(i);// 在内容上方加水印
            content = stamper.getUnderContent(i);//在内容下方加水印
            gs.setFillOpacity(0.2f);
            content.beginText();
            //字体大小
            content.setFontAndSize(base, 10.5F);
            //content.setTextMatrix(390, 810);
            //内容居中，横纵坐标，偏移量
            /*for (PdfPro pdfPro : pdfList) {
                content.showTextAligned(Element.ALIGN_CENTER, pdfPro.getText(), pdfPro.getX(), pdfPro.getY(), 0);
            }*/
            for (ImagePro imagePro : imagePros) {
                //添加图片
                Image image = Image.getInstance(imagePro.getImgPath());
            /*
              img.setAlignment(Image.LEFT | Image.TEXTWRAP);
              img.setBorder(Image.BOX); img.setBorderWidth(10);
              img.setBorderColor(BaseColor.WHITE); img.scaleToFit(100072);//大小
              img.setRotationDegrees(-30);//旋转
             */
                //图片的位置（坐标）
                image.setAbsolutePosition(imagePro.getX(), imagePro.getY());
                image.scaleToFit(200, 200);
                image.scalePercent(imagePro.getScalePercent());//依照比例缩放. 调整缩放,控制图片大小
                content.addImage(image);
            }
            content.setFontAndSize(base, 8);
            content.endText();
        }
        stamper.close();
        reader.close();
    }

    public static OutputStream writeToPdf2(InputStream is, ServletOutputStream fileOutputStream, List<ImagePro> imagePros)
            throws Exception {
        //append 追加
        //ServletOutputStream转OutputStream
        OutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = FileAndFolderUtil.convertIo(fileOutputStream);
        try {
            outputStream = FileAndFolderUtil.parseIn(inputStream);
        }catch (Exception e){
            log.info("流转换异常:{}",e);
        }
        //BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
        PdfReader reader = new PdfReader(is);
        PdfStamper stamper = new PdfStamper(reader, outputStream);
        int total = reader.getNumberOfPages() + 1;
        PdfContentByte content;
        BaseFont base = BaseFont.createFont("c:\\windows\\fonts\\SIMHEI.TTF", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        PdfGState gs = new PdfGState();
        for (int i = 1; i < total; i++) {
            content = stamper.getUnderContent(i);//在内容下方加水印
            gs.setFillOpacity(0.2f);
            content.beginText();
            //字体大小
            content.setFontAndSize(base, 10.5F);
            for (ImagePro imagePro : imagePros) {
                //添加图片
                Image image = Image.getInstance(imagePro.getImgPath());
                //图片的位置（坐标）
                image.setAbsolutePosition(imagePro.getX(), imagePro.getY());
                image.scaleToFit(200, 200);
                image.scalePercent(imagePro.getScalePercent());//依照比例缩放. 调整缩放,控制图片大小
                content.addImage(image);
            }
            content.setFontAndSize(base, 8);
            content.endText();
        }
        stamper.close();
        reader.close();
        return outputStream;
    }

    /**
     * 图片插入pdf
     * @param oriPdfPath 源pdf路径
     * @param desPdfPath 输出的pdf路径及文件名称
     * @param imagePath 要插入的印章图片路径
     * @param keyword 插入的位置（pdf中的关键字）
     * @throws Exception
     */
    public static void insertImageToPdf(String oriPdfPath,String desPdfPath,String imagePath,String keyword) throws Exception {
        File pdfFile = new File(oriPdfPath);
        byte[] pdfData = new byte[(int) pdfFile.length()];
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(pdfFile);
            inputStream.read(pdfData);
        } catch (IOException e) {
            throw e;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
        List<float[]> positions = findKeywordPostions(pdfData, keyword);
        log.info("total:{}" + positions.size());
        if (positions != null && positions.size() > 0) {
            for (float[] position : positions) {
                log.info("pageNum:{} " + (int) position[0]);
                log.info("\tx:{} " + position[1]);
                log.info("\ty:{} " + position[2]);
                seal(pdfFile,new File(desPdfPath),(int) position[0],position[1],position[2],imagePath);
            }
        }
    }

    /**
     * 盖章
     * @param src
     * @param dest
     * @param page
     * @param x
     * @param y
     * @param imagePath
     * @throws Exception
     */
    public static void seal(File src,File dest,int page,float x,float y,String imagePath)throws Exception{
        // 读取模板文件
        InputStream input = new FileInputStream(src);
        PdfReader reader = new PdfReader(input);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        Rectangle pageSize = reader.getPageSize(1);
        float height = pageSize.getHeight();
        float width = pageSize.getWidth();
        x = width*x;
        y = height-height*y-145;
        // 读图片
        Image image = Image.getInstance(imagePath);
        // 获取操作的页面
        PdfContentByte under = stamper.getOverContent(page);
        // 添加图片
        //调整图片尺寸
        image.setAbsolutePosition(x, y);
        under.addImage(image);
        stamper.close();
        reader.close();
    }
    /**
     *
     * 【功能描述：添加图片和文字水印】 【功能详细描述：功能详细描述】
     * @param srcFile 待加水印文件
     * @param destFile 加水印后存放地址
     * @param text 加水印的文本内容
     * @param textWidth 文字横坐标
     * @param textHeight 文字纵坐标
     * @throws Exception
     */
    public void addWaterMark(String srcFile, String destFile, String text,
                             int textWidth, int textHeight) throws Exception
    {
        // 待加水印的文件
        PdfReader reader = new PdfReader(srcFile);
        // 加完水印的文件
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(
                destFile));
        int total = reader.getNumberOfPages() + 1;
        PdfContentByte content;
        // 设置字体
        BaseFont font = BaseFont.createFont();
        // 循环对每页插入水印
        for (int i = 1; i < total; i++)
        {
            // 水印的起始
            content = stamper.getUnderContent(i);
            // 开始
            content.beginText();
            // 设置颜色 默认为蓝色
            content.setColorFill(BaseColor.BLUE);
            // content.setColorFill(Color.GRAY);
            // 设置字体及字号
            content.setFontAndSize(font, 38);
            // 设置起始位置
            // content.setTextMatrix(400, 880);
            content.setTextMatrix(textWidth, textHeight);
            // 开始写入水印
            content.showTextAligned(Element.ALIGN_LEFT, text, textWidth,
                    textHeight, 45);
            content.endText();
        }
        stamper.close();
    }

    /**
     * findKeywordPostions &nbsp;
     *
     * @param pdfData
     * @param keyword
     * @return List<float[]> : float[0]:pageNum float[1]:x float[2]:y
     * @throws IOException
     */
    public static List<float[]> findKeywordPostions(byte[] pdfData,
                                                    String keyword) throws IOException {
        List<float[]> result = new ArrayList<float[]>();
        List<PdfPageContentPositions> pdfPageContentPositions = getPdfContentPostionsList(pdfData);

        for (PdfPageContentPositions pdfPageContentPosition : pdfPageContentPositions) {
            List<float[]> charPositions = findPositions(keyword,
                    pdfPageContentPosition);
            if (charPositions == null || charPositions.size() < 1) {
                continue;
            }
            result.addAll(charPositions);
        }
        return result;
    }

    private static List<PdfPageContentPositions> getPdfContentPostionsList(
            byte[] pdfData) throws IOException {
        PdfReader reader = new PdfReader(pdfData);

        List<PdfPageContentPositions> result = new ArrayList<PdfPageContentPositions>();

        int pages = reader.getNumberOfPages();
        for (int pageNum = 1; pageNum <= pages; pageNum++) {
            float width = reader.getPageSize(pageNum).getWidth();
            float height = reader.getPageSize(pageNum).getHeight();

            PdfRenderListener pdfRenderListener = new PdfRenderListener(
                    pageNum, width, height);

            // 解析pdf，定位位置
            PdfContentStreamProcessor processor = new PdfContentStreamProcessor(
                    pdfRenderListener);
            PdfDictionary pageDic = reader.getPageN(pageNum);
            PdfDictionary resourcesDic = pageDic.getAsDict(PdfName.RESOURCES);
            try {
                processor.processContent(ContentByteUtils
                        .getContentBytesForPage(reader, pageNum), resourcesDic);
            } catch (IOException e) {
                reader.close();
                throw e;
            }

            String content = pdfRenderListener.getContent();
            List<CharPosition> charPositions = pdfRenderListener
                    .getcharPositions();

            List<float[]> positionsList = new ArrayList<float[]>();
            for (CharPosition charPosition : charPositions) {
                float[] positions = new float[] { charPosition.getPageNum(),
                        charPosition.getX(), charPosition.getY() };
                positionsList.add(positions);
            }

            PdfPageContentPositions pdfPageContentPositions = new PdfPageContentPositions();
            pdfPageContentPositions.setContent(content);
            pdfPageContentPositions.setPostions(positionsList);

            result.add(pdfPageContentPositions);
        }
        reader.close();
        return result;
    }

    private static List<float[]> findPositions(String keyword,
                                               PdfPageContentPositions pdfPageContentPositions) {
        List<float[]> result = new ArrayList<float[]>();
        String content = pdfPageContentPositions.getContent();
        List<float[]> charPositions = pdfPageContentPositions.getPositions();
        for (int pos = 0; pos < content.length();) {
            int positionIndex = content.indexOf(keyword, pos);
            if (positionIndex == -1) {
                break;
            }
            float[] postions = charPositions.get(positionIndex);
            result.add(postions);
            pos = positionIndex + 1;
        }
        return result;
    }

    private static class PdfPageContentPositions {
        private String content;
        private List<float[]> positions;
        public String getContent() {
            return content;
        }
        public void setContent(String content) {
            this.content = content;
        }
        public List<float[]> getPositions() {
            return positions;
        }
        public void setPostions(List<float[]> positions) {
            this.positions = positions;
        }
    }

    private static class PdfRenderListener implements RenderListener {
        private int pageNum;
        private float pageWidth;
        private float pageHeight;
        private StringBuilder contentBuilder = new StringBuilder();
        private List<CharPosition> charPositions = new ArrayList<CharPosition>();
        public PdfRenderListener(int pageNum, float pageWidth, float pageHeight) {
            this.pageNum = pageNum;
            this.pageWidth = pageWidth;
            this.pageHeight = pageHeight;
        }

        @Override
        public void beginTextBlock() {

        }

        @Override
        public void renderText(TextRenderInfo renderInfo) {
            List<TextRenderInfo> characterRenderInfos = renderInfo
                    .getCharacterRenderInfos();
            for (TextRenderInfo textRenderInfo : characterRenderInfos) {
                String word = textRenderInfo.getText();
                if (word.length() > 1) {
                    word = word.substring(word.length() - 1, word.length());
                }
                com.itextpdf.awt.geom.Rectangle2D.Float rectangle = textRenderInfo.getAscentLine()
                        .getBoundingRectange();
                double x = rectangle.getMinX();
                double y = rectangle.getMaxY();

                float xPercent = Math.round(x / pageWidth * 10000) / 10000f;
                float yPercent = Math.round((1 - y / pageHeight) * 10000) / 10000f;//

                CharPosition charPosition = new CharPosition(pageNum, xPercent,
                        yPercent);
                charPositions.add(charPosition);
                contentBuilder.append(word);
            }
        }

        @Override
        public void endTextBlock() {

        }

        @Override
        public void renderImage(ImageRenderInfo renderInfo) {

        }

        public String getContent() {
            return contentBuilder.toString();
        }

        public List<CharPosition> getcharPositions() {
            return charPositions;
        }
    }

    private static class CharPosition {
        private int pageNum = 0;
        private float x = 0;
        private float y = 0;

        public CharPosition(int pageNum, float x, float y) {
            this.pageNum = pageNum;
            this.x = x;
            this.y = y;
        }

        public int getPageNum() {
            return pageNum;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        @Override
        public String toString() {
            return "[pageNum=" + this.pageNum + ",x=" + this.x + ",y=" + this.y
                    + "]";
        }
    }
}
