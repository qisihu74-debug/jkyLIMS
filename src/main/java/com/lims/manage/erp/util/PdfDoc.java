package com.lims.manage.erp.util;

import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.lims.manage.erp.entity.Doc;
import com.lims.manage.erp.entity.KeyPosition;
import com.lims.manage.erp.entity.PdfKeywordFinder;
import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gjl
 */
public class PdfDoc implements Doc {
    private final Logger logger = LoggerFactory.getLogger(PdfDoc.class);
    /**
     * pdf文档的路径
     */
    private String docPath;
    /**
     * 修改后的输出路径
     */
    private String destDocPath;
    /**
     * 对坐标的偏移量，x坐标
     */
    private float offsetX = 0;
    /**
     * 对坐标的偏移量，y坐标
     */
    private float offsetY = 0;

    public PdfDoc(String docPath, String destDocPath) {
        this.docPath = docPath;
        this.destDocPath = destDocPath;
    }

    /**
     *
     * @param imagePath 图片的路径
     * @param positionList      pdf上需要放图片的坐标
     * @param width         图片的宽
     * @param height        图片的长
     * @throws Exception
     */
    public void addImage(String imagePath, List<KeyPosition> positionList, int width, int height) throws Exception {
        PdfReader reader = null;
        PdfStamper stamper = null;

        try {
            reader = new PdfReader(docPath);

            File file = new File(destDocPath);
            if (file.exists()) {
                boolean fileDel = file.delete();
                System.out.println(fileDel);
            }

            OutputStream destDocOutputStream = new FileOutputStream(file);
            stamper = new PdfStamper(reader, destDocOutputStream);

            for (KeyPosition position : positionList) {
                int pageNum = position.getPageNum();
                float absoluteX = position.getX() + offsetX;
                float absoluteY = position.getY() + offsetY;
                System.out.println("imagePath[" + imagePath + "],pageNum[" + pageNum + "],absoluteX[" + absoluteX + "],absoluteY[" + absoluteY + "]");

                Image image = Image.getInstance(imagePath);
                image.scaleAbsolute(width, height);

                image.setAbsolutePosition(absoluteX, absoluteY);
                PdfContentByte over = stamper.getOverContent(pageNum);
                over.addImage(image);
                System.out.println("添加图片");
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new Exception("修改PDF异常", e);
        } finally {
            if (stamper != null) {
                try {
                    stamper.close();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (reader != null) {
                reader.close();
            }
            System.out.println("完成");
        }
    }

    @Override
    public void addImage(String imagePath, int pageNum, float x, float y, int width, int height) throws Exception {
        List<KeyPosition> positionList = new ArrayList<>();
        KeyPosition keyPosition = new KeyPosition(pageNum, x, y);
        positionList.add(keyPosition);
        addImage(imagePath, positionList, width, height);
    }

    @Override
    public void addImage(String imagePath, String key,float offsetX,float offsetY, int width, int height) throws Exception {
        List<KeyPosition> positionList = new ArrayList<>();
        this.offsetX=offsetX;
        this.offsetY=offsetY;
        try {
            List<float[]> tempPositionList = PdfKeywordFinder.getAddImagePositionList(docPath, key);

            for (float[] position : tempPositionList) {
                System.out.println("页数[" + (int) position[0] + "],x[" + position[1] + "],y[" + position[2] + "]");
                KeyPosition keyPosition = new KeyPosition((int) position[0], position[1], position[2]);
                positionList.add(keyPosition);
            }

            addImage(imagePath, positionList, width, height);

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new Exception("修改PDF异常", e);
        }

    }

    /**
     *
     * @param sourPath 原pdf
     * @param savePath 新pdf
     * @Param waterContent 水印内容
     */
    @SuppressWarnings("unchecked")
    public static void removePdfWatermark(String sourPath ,String savePath,String waterContent) {
        if (StringUtils.isEmpty(waterContent)){
            waterContent = " Evaluation Warning : The document was created with Spire.PDF for Java.";
        }
        try {
            //读取源文件
            PDDocument helloDocument = PDDocument.load(new File(sourPath));
            List<PDPage> allPages = helloDocument.getDocumentCatalog().getAllPages();
            for(PDPage pdPage : allPages) {
                PDStream contents = pdPage.getContents();
                PDFStreamParser parser = new PDFStreamParser(contents.getStream());
                parser.parse();
                List<Object> tokens = parser.getTokens();
                for (int j = 0; j < tokens.size(); j++) {
                    Object o = tokens.get(j);
                    if (o instanceof COSString){
                        COSString cosString = (COSString)o;
                        String operation = cosString.getString();
                        if (operation.equals(waterContent)) {
                            cosString.reset();
                        }
                    }
                }
                PDStream updatedStream = new PDStream(helloDocument);
                OutputStream out = updatedStream.createOutputStream();
                ContentStreamWriter tokenWriter = new ContentStreamWriter(out);
                tokenWriter.writeTokens(tokens);
                pdPage.setContents(updatedStream);
            }
            //Output file name
            helloDocument.save(savePath);
            helloDocument.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (COSVisitorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getDocPath() {
        return docPath;
    }

    public void setDocPath(String docPath) {
        this.docPath = docPath;
    }

    public String getDestDocPath() {
        return destDocPath;
    }

    public void setDestDocPath(String destDocPath) {
        this.destDocPath = destDocPath;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

}
