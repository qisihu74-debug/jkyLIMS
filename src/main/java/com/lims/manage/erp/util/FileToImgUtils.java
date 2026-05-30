package com.lims.manage.erp.util;

import com.aspose.cells.*;
import com.aspose.slides.ISlide;
import com.aspose.slides.ISlideCollection;
import com.aspose.slides.Presentation;
import com.aspose.words.Document;
import com.aspose.words.ImageSaveOptions;
import com.aspose.words.PageSet;
import com.aspose.words.SaveFormat;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.Color;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件传图片工具类
 *
 * @author: zhq
 * @date: 2023-01-16
 * @version: v1.0
 */
public class FileToImgUtils {

    /**
     * @Description: 验证aspose.word组件是否授权：无授权的文件有水印标记
     */
    public static boolean isWordLicense() {
        String s = "<License><Data><Products><Product>Aspose.Total for Java</Product><Product>Aspose.Words for Java</Product></Products><EditionType>Enterprise</EditionType><SubscriptionExpiry>20991231</SubscriptionExpiry><LicenseExpiry>20991231</LicenseExpiry><SerialNumber>8bfe198c-7f0c-4ef8-8ff0-acc3237bf0d7</SerialNumber></Data><Signature>sNLLKGMUdF0r8O1kKilWAGdgfs2BvJb/2Xp8p5iuDVfZXmhppo+d0Ran1P9TKdjV4ABwAgKXxJ3jcQTqE/2IRfqwnPf8itN8aFZlV3TJPYeD3yWE7IT55Gz6EijUpC7aKeoohTb4w2fpox58wWoF3SNp6sK6jDfiAUGEHYJ9pjU=</Signature></License>";
        boolean result = false;
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(s.getBytes());
            com.aspose.words.License license = new com.aspose.words.License();
            license.setLicense(inputStream);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isPPTLicense() {
        boolean result = false;
        try {
            String s = "<License><Data><Products><Product>Aspose.Total for Java</Product></Products><EditionType>Enterprise</EditionType><SubscriptionExpiry>20991231</SubscriptionExpiry><LicenseExpiry>20991231</LicenseExpiry><SerialNumber>8bfe198c-7f0c-4ef8-8ff0-acc3237bf0d7</SerialNumber></Data><Signature>sNLLKGMUdF0r8O1kKilWAGdgfs2BvJb/2Xp8p5iuDVfZXmhppo+d0Ran1P9TKdjV4ABwAgKXxJ3jcQTqE/2IRfqwnPf8itN8aFZlV3TJPYeD3yWE7IT55Gz6EijUpC7aKeoohTb4w2fpox58wWoF3SNp6sK6jDfiAUGEHYJ9pjU=</Signature></License>";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(s.getBytes());
            com.aspose.slides.License license = new com.aspose.slides.License();
            license.setLicense(inputStream);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isExcelLicense() {
        boolean result = false;
        try {
            String s = "<License><Data><Products><Product>Aspose.Total for Java</Product></Products><EditionType>Enterprise</EditionType><SubscriptionExpiry>20991231</SubscriptionExpiry><LicenseExpiry>20991231</LicenseExpiry><SerialNumber>8bfe198c-7f0c-4ef8-8ff0-acc3237bf0d7</SerialNumber></Data><Signature>sNLLKGMUdF0r8O1kKilWAGdgfs2BvJb/2Xp8p5iuDVfZXmhppo+d0Ran1P9TKdjV4ABwAgKXxJ3jcQTqE/2IRfqwnPf8itN8aFZlV3TJPYeD3yWE7IT55Gz6EijUpC7aKeoohTb4w2fpox58wWoF3SNp6sK6jDfiAUGEHYJ9pjU=</Signature></License>";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(s.getBytes());
            com.aspose.cells.License license = new com.aspose.cells.License();
            license.setLicense(inputStream);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @Description: word和txt文件转换图片
     */
    public static List<BufferedImage> wordToImg(InputStream inputStream) {
        /*if (!isWordLicense()) {
            return null;
        }*/
        try {
            Document doc = new Document(inputStream);
            ImageSaveOptions options = new ImageSaveOptions(SaveFormat.JPEG);
            options.setPrettyFormat(true);
            options.setUseAntiAliasing(true);
            options.setUseHighQualityRendering(true);
            int pageCount = doc.getPageCount();
            List<BufferedImage> imageList = new ArrayList<>();
            for (int i = 0; i < pageCount; i++) {
                OutputStream output = new ByteArrayOutputStream();
                options.setPageSet(new PageSet(i));
                doc.save(output, options);
                ImageInputStream imageInputStream = ImageIO.createImageInputStream(parse(output));
                imageList.add(ImageIO.read(imageInputStream));
            }
            return imageList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * @Description: ppt, pptx文件转换图片
     */
    public static List<BufferedImage> pptToImg(InputStream inputStream) {
        // 验证License
        if (!isPPTLicense()) {
            return null;
        }
        FileOutputStream out = null;
        try {
            List<BufferedImage> imageList = new ArrayList<BufferedImage>();
            Presentation pres = new Presentation(inputStream);
            ISlideCollection slides = pres.getSlides();
            for (int i = 0; i < slides.size(); i++) {
                ISlide slide = slides.get_Item(i);
                int height = (int) (pres.getSlideSize().getSize().getHeight() - 150);
                int width = (int) (pres.getSlideSize().getSize().getWidth() - 150);
                BufferedImage img = slide.getThumbnail(new java.awt.Dimension(width, height));
                imageList.add(img);
            }
            return imageList;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * @Description: pdf文件转换图片
     */
    public static List<BufferedImage> pdfToImg(InputStream inputStream) {
        try {
            PDDocument pdDocument;
            List<BufferedImage> imageList = new ArrayList<BufferedImage>();
            pdDocument = PDDocument.load(inputStream);
            /*PDFRenderer renderer = new PDFRenderer(pdDocument);
             *//* dpi越大转换后越清晰，相对转换速度越慢 *//*
            int pages = pdDocument.getNumberOfPages();
            for (int i = 0; i < pages; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 100);
                imageList.add(image);
            }*/

            List<PDPage> allPages = pdDocument.getDocumentCatalog().getAllPages();
            for (int i = 0; i < allPages.size(); i++) {
                PDPage page = allPages.get(i);
                page.convertToImage();
                BufferedImage image = page.convertToImage();
                imageList.add(image);
            }
            return imageList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * @Description: excel文件转换图片
     */
    public static List<BufferedImage> excelToImg(InputStream inputStream) {
        if (!isExcelLicense()) {
            return null;
        }
        Workbook book = null;
        List<BufferedImage> imageList = new ArrayList<BufferedImage>();
        try {
            book = new Workbook(inputStream);
            WorksheetCollection worksheets = book.getWorksheets();
            ImageOrPrintOptions imgOptions = new ImageOrPrintOptions();
            imgOptions.setImageFormat(ImageFormat.getJpeg());
            imgOptions.setCellAutoFit(true);
            imgOptions.setOnePagePerSheet(true);
            for (int i = 0; i < worksheets.getCount(); i++) {
                Worksheet sheet = worksheets.get(i);
                OutputStream output = new ByteArrayOutputStream();
                sheet.getPageSetup().setLeftMargin(-20);
                sheet.getPageSetup().setRightMargin(0);
                sheet.getPageSetup().setBottomMargin(0);
                sheet.getPageSetup().setTopMargin(0);
                SheetRender render = new SheetRender(sheet, imgOptions);
                render.toImage(0, output);
                ByteArrayInputStream parse = parse(output);
                if (parse != null) {
                    ImageInputStream imageInputStream = ImageIO.createImageInputStream(parse);
                    BufferedImage bufferedImage = ImageIO.read(imageInputStream);
                    imageList.add(bufferedImage);
                    parse.close();
                }
                output.close();
            }
            return imageList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;

    }

    /**
     * @Description: outputStream转inputStream
     */
    public static ByteArrayInputStream parse(OutputStream out) {
        try {
            ByteArrayOutputStream baos = null;
            baos = (ByteArrayOutputStream) out;
            ByteArrayInputStream swapStream = new ByteArrayInputStream(baos.toByteArray());
            return swapStream;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return null;
    }


    /**
     * 合并任数量的图片成一张图片
     *
     * @param isHorizontal true代表水平合并，fasle代表垂直合并
     * @param imgs         待合并的图片数组
     * @return
     * @throws IOException
     */
    public static BufferedImage mergeImage(boolean isHorizontal, List<BufferedImage> imgs) throws IOException {
        // 生成新图片
        BufferedImage destImage = null;
        // 计算新图片的长和高
        int allw = 0, allh = 0, allwMax = 0, allhMax = 0;
        // 获取总长、总宽、最长、最宽
        for (int i = 0; i < imgs.size(); i++) {
            BufferedImage img = imgs.get(i);
            allw += img.getWidth();
            allh += img.getHeight();
            allhMax = Math.max(img.getHeight(), allhMax);
            allwMax = Math.max(img.getWidth(), allwMax);
        }
        // 创建新图片
        if (isHorizontal) {
            destImage = new BufferedImage(allw, allhMax, BufferedImage.TYPE_INT_RGB);
        } else {
            destImage = new BufferedImage(allwMax, allh + imgs.size() * 5, BufferedImage.TYPE_INT_RGB);
        }
        Graphics2D g2 = (Graphics2D) destImage.getGraphics();
        g2.setBackground(Color.LIGHT_GRAY);

        g2.clearRect(0, 0, allw, allh);
        g2.setPaint(Color.RED);
        // 合并所有子图片到新图片
        int wx = 0, wy = 0;
        for (int i = 0; i < imgs.size(); i++) {
            BufferedImage img = imgs.get(i);
            int w1 = img.getWidth();
            int h1 = img.getHeight();
            // 从图片中读取RGB
            int[] ImageArrayOne = new int[w1 * h1];
            ImageArrayOne = img.getRGB(0, 0, w1, h1, ImageArrayOne, 0, w1); // 逐行扫描图像中各个像素的RGB到数组中
            if (isHorizontal) { // 水平方向合并
                destImage.setRGB(wx, 0, w1, h1, ImageArrayOne, 0, w1); // 设置上半部分或左半部分的RGB
            } else { // 垂直方向合并
                destImage.setRGB(0, wy, w1, h1, ImageArrayOne, 0, w1); // 设置上半部分或左半部分的RGB
            }
            wx += w1;
            wy += h1 + 5;
        }
        return destImage;
    }
}
