/*
package com.lims.manage.erp.util;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

*/
/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.util
 * @desc
 * @date 2021/10/29 10:16
 * @Copyright © 河南交科院
 *//*

public class VideoUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoUtil.class);
    */
/**
     * 通过Javacv的方式获取视频截图
     *
     * @param deviceFile 视频文件路径
     * @return Map<String, Object>
     *//*


    public static Map<String, Object> getScreenshot(File deviceFile) {
        try {
            LOGGER.debug("截取视频截图开始：" + System.currentTimeMillis());
            Map<String, Object> result = new HashMap<String, Object>();
            FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(deviceFile);

            String fileName = deviceFile.getName();
            String absolutePath = "D:\\Users\\Administrator";

//            // 第一帧图片存储位置
//            String targerFilePath = filePath.substring(0, filePath.lastIndexOf("\\"));
//            // 视频文件名
//            String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);
            // 图片名称
            String targetFileName = fileName.substring(0, fileName.lastIndexOf("."));
//            LOGGER.debug("视频路径是：" + targerFilePath);
//            LOGGER.debug("视频文件名：" + fileName);
            LOGGER.debug("图片名称是：" + targetFileName);

            grabber.start();
            //设置视频截取帧（默认取第一帧）
            Frame frame = grabber.grabImage();
            int i = 0;
            int lenght = grabber.getLengthInFrames();
            Frame f = null;
            while (i < lenght) {
                // 过滤前300帧，避免出现全黑的图片，依自己情况而定
//             f = grabber.grabFrame();
                f=grabber.grabImage();
                if ((i > 150) && (f.image != null)) {
                    break;
                }
                i++;
            }

            //视频旋转度
            String rotate = grabber.getVideoMetadata("rotate");
            Java2DFrameConverter converter = new Java2DFrameConverter();
            //绘制图片
            BufferedImage bi = converter.getBufferedImage(f);
            if (rotate != null) {
                // 旋转图片
                bi = rotate(bi, Integer.parseInt(rotate));
            }
            //图片的类型
            String imageMat = "jpg";
            //图片的完整路径
            String imagePath = absolutePath + File.separator + targetFileName + "." + imageMat;
            //创建文件
            File output = new File(imagePath);
            ImageIO.write(bi, imageMat, output);

//            //拼接Map信息
//            result.put("videoWide", bi.getWidth());
//            result.put("videoHigh", bi.getHeight());
//            long duration = grabber.getLengthInTime() / (1000 * 1000);
//            result.put("rotate", StringUtils.isBlank(rotate) ? "0" : rotate);
//            result.put("format", grabber.getFormat());
            result.put("imgPath", output.getPath());
//            result.put("time", duration);
//            LOGGER.debug("视频的宽:" + bi.getWidth());
//            LOGGER.debug("视频的高:" + bi.getHeight());
//            LOGGER.debug("视频的旋转度：" + rotate);
//            LOGGER.debug("视频的格式：" + grabber.getFormat());
//            LOGGER.debug("此视频时长（s/秒）：" + duration);
            grabber.stop();
            LOGGER.debug("截取视频截图结束：" + System.currentTimeMillis());
            return result;
        } catch (Exception e) {
//            LOGGER.error("VideoUtil getScreenshot fail: {}", ExceptionStackTrace.printStackTrace(e));
            return null;
        }
    }

    */
/**
     * 根据视频旋转度来调整图片
     *
     * @param src   BufferedImage
     * @param angel angel	视频旋转度
     * @return BufferedImage
     *//*

    public static BufferedImage rotate(BufferedImage src, int angel) {
        int src_width = src.getWidth(null);
        int src_height = src.getHeight(null);
        int type = src.getColorModel().getTransparency();
        Rectangle rect_des = calcRotatedSize(new Rectangle(new Dimension(src_width, src_height)), angel);
        BufferedImage bi = new BufferedImage(rect_des.width, rect_des.height, type);
        Graphics2D g2 = bi.createGraphics();
        g2.translate((rect_des.width - src_width) / 2, (rect_des.height - src_height) / 2);
        g2.rotate(Math.toRadians(angel), src_width / 2, src_height / 2);
        g2.drawImage(src, 0, 0, null);
        g2.dispose();
        return bi;
    }


    */
/**
     * 计算图片旋转大小
     *
     * @param src   Rectangle
     * @param angel int
     * @return Rectangle
     *//*

    public static Rectangle calcRotatedSize(Rectangle src, int angel) {
        if (angel >= 90) {
            if (angel / 90 % 2 == 1) {
                int temp = src.height;
                src.height = src.width;
                src.width = temp;
            }
            angel = angel % 90;
        }
        double r = Math.sqrt(src.height * src.height + src.width * src.width) / 2;
        double len = 2 * Math.sin(Math.toRadians(angel) / 2) * r;
        double angel_alpha = (Math.PI - Math.toRadians(angel)) / 2;
        double angel_dalta_width = Math.atan((double) src.height / src.width);
        double angel_dalta_height = Math.atan((double) src.width / src.height);
        int len_dalta_width = (int) (len * Math.cos(Math.PI - angel_alpha - angel_dalta_width));
        int len_dalta_height = (int) (len * Math.cos(Math.PI - angel_alpha - angel_dalta_height));
        int des_width = src.width + len_dalta_width * 2;
        int des_height = src.height + len_dalta_height * 2;
        return new Rectangle(new Dimension(des_width, des_height));
    }

}
*/
