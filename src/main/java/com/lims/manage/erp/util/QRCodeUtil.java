package com.lims.manage.erp.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.lims.manage.erp.entity.QiYueSuoEntity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.erp.util
 * @desc 二维码工具
 * @date 2022-11-29 19:57
 * @Copyright © 河南交科院
 */
public class QRCodeUtil {
    private static final Logger log= LoggerFactory.getLogger(QRCodeUtil.class);
    @Autowired
    private QiYueSuoEntity qiYueSuoEntity;

    //CODE_WIDTH：二维码宽度，单位像素
    private static final int CODE_WIDTH = 400;
    //CODE_HEIGHT：二维码高度，单位像素
    private static final int CODE_HEIGHT = 400;
    //FRONT_COLOR：二维码前景色，0x000000 表示黑色
    private static final int FRONT_COLOR = 0x000000;
    //BACKGROUND_COLOR：二维码背景色，0xFFFFFF 表示白色
    //演示用 16 进制表示，和前端页面 CSS 的取色是一样的，注意前后景颜色应该对比明显，如常见的黑白
    private static final int BACKGROUND_COLOR = 0xFFFFFF;

    public static void createCodeToFile(String content, File codeImgFileSaveDir, String fileName) {
        try {
            if (StringUtils.isBlank(content) || StringUtils.isBlank(fileName)) {
                return;
            }
            content = content.trim();
            if (codeImgFileSaveDir==null || codeImgFileSaveDir.isFile()) {
                //二维码图片存在目录为空，默认放在桌面...
                codeImgFileSaveDir = FileSystemView.getFileSystemView().getHomeDirectory();
            }
            if (!codeImgFileSaveDir.exists()) {
                //二维码图片存在目录不存在，开始创建...
                codeImgFileSaveDir.mkdirs();
            }

            //核心代码-生成二维码
            BufferedImage bufferedImage = getBufferedImage(content);

            File codeImgFile = new File(codeImgFileSaveDir, fileName);
            ImageIO.write(bufferedImage, "png", codeImgFile);

            log.info("二维码图片生成成功：" + codeImgFile.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成二维码并输出到输出流, 通常用于输出到网页上进行显示，输出到网页与输出到磁盘上的文件中，区别在于最后一句 ImageIO.write
     * write(RenderedImage im,String formatName,File output)：写到文件中
     * write(RenderedImage im,String formatName,OutputStream output)：输出到输出流中
     * @param content  ：二维码内容
     * @param outputStream ：输出流，比如 HttpServletResponse 的 getOutputStream
     */
    public static void createCodeToOutputStream(String content, OutputStream outputStream) {
        try {
            if (StringUtils.isBlank(content)) {
                return;
            }
            content = content.trim();
            //核心代码-生成二维码
            BufferedImage bufferedImage = getBufferedImage(content);

            //区别就是这一句，输出到输出流中，如果第三个参数是 File，则输出到文件中
            ImageIO.write(bufferedImage, "png", outputStream);

            log.info("二维码图片生成到输出流成功...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //核心代码-生成二维码
    public static BufferedImage getBufferedImage(String content) throws WriterException {

        //com.google.zxing.EncodeHintType：编码提示类型,枚举类型
        Map<EncodeHintType, Object> hints = new HashMap();

        //EncodeHintType.CHARACTER_SET：设置字符编码类型
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        //EncodeHintType.ERROR_CORRECTION：设置误差校正
        //ErrorCorrectionLevel：误差校正等级，L = ~7% correction、M = ~15% correction、Q = ~25% correction、H = ~30% correction
        //不设置时，默认为 L 等级，等级不一样，生成的图案不同，但扫描的结果是一样的
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);

        //EncodeHintType.MARGIN：设置二维码边距，单位像素，值越小，二维码距离四周越近
        hints.put(EncodeHintType.MARGIN, 1);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, CODE_WIDTH, CODE_HEIGHT, hints);
        BufferedImage bufferedImage = new BufferedImage(CODE_WIDTH, CODE_HEIGHT, BufferedImage.TYPE_INT_BGR);
        for (int x = 0; x < CODE_WIDTH; x++) {
            for (int y = 0; y < CODE_HEIGHT; y++) {
                bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? FRONT_COLOR : BACKGROUND_COLOR);
            }
        }
        return bufferedImage;
    }

    /**
     * 根据内容和图标生成二维码
     * @param qrCodeData
     * @param logoPath
     * @param filePath
     * @return
     * @throws WriterException
     * @throws IOException
     */
    public static File generateQRCode(String qrCodeData, String logoPath, String filePath) throws WriterException, IOException {
        int qrCodeSize = 400; // 二维码尺寸
        int logoSize = 80; // Logo 尺寸
        File qRFile = new File(filePath);
        try {
            // 加载公司 Logo 图像
            URL url1 = new URL(logoPath);
            URLConnection connection = url1.openConnection();
            InputStream inputStream = connection.getInputStream();
            BufferedImage logoImage = ImageIO.read(inputStream);
            // 生成二维码图像
            BitMatrix bitMatrix = new MultiFormatWriter().encode(qrCodeData, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize);
            BufferedImage qrCodeImage = bitMatrixToImage(bitMatrix);
            // 在二维码中心添加公司 Logo
            int logoX = qrCodeSize / 2 - logoSize / 2;
            int logoY = qrCodeSize / 2 - logoSize / 2;
            // 在 qrCodeImage 上绘制 logoImage，以覆盖原有的二维码图像
            Graphics2D graphics = qrCodeImage.createGraphics();
            // 设置透明度，使得 Logo 可以覆盖在二维码上，同时保留原始的背景颜色
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            graphics.drawImage(logoImage, logoX, logoY, logoSize, logoSize, null);
            graphics.dispose();
            // 保存带有公司 Logo 的二维码图像
            ImageIO.write(qrCodeImage, "PNG", qRFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return qRFile;
    }

    private static BufferedImage bitMatrixToImage(BitMatrix bitMatrix) {
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (bitMatrix.get(x, y)) {
                    image.setRGB(x, y, 0x00ffffff); // 设置二维码的像素为白色，表示二维码的“0”
                } else {
                    image.setRGB(x, y, 0x00000000); // 设置二维码的像素为透明，表示二维码的“1”
                }
            }
        }
        return image;
    }

    public static BufferedImage getBufferedImage(String content, String logoURL, int logoWidth, int logoHeight) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);
        try {
            // 加载公司Logo图像
            BufferedImage logoImage = ImageIO.read(new URL(logoURL));
            logoImage = resizeImage(logoImage, logoWidth, logoHeight); // 如果需要调整Logo大小
            BufferedImage bufferedImage = new BufferedImage(CODE_WIDTH, CODE_HEIGHT, BufferedImage.TYPE_INT_BGR);
            bufferedImage.createGraphics().drawImage(logoImage, 0, 0, null); // 在背景上绘制Logo
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, CODE_WIDTH, CODE_HEIGHT, hints);
            for (int x = 0; x < CODE_WIDTH; x++) {
                for (int y = 0; y < CODE_HEIGHT; y++) {
                    bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? FRONT_COLOR : BACKGROUND_COLOR);
                }
            }
            return bufferedImage;
        } catch (IOException e) {
            throw new WriterException("Error loading logo image:"+ e.getMessage());
        }
    }

// 调整图像大小的方法（可选）
    private static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;

    }
}
