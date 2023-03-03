package com.lims.manage.erp.util;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * 视频剪辑封面
 *
 * @author: zhq
 * @date: 2023-01-06
 * @version: v1.0
 */
public class FileImageUtil {
    /**
     * 获取指定视频的帧并保存为图片至指定目录
     *
     * @param filePath       视频存放的地址 例如：D:/ruoyi/uploadPath/avatar/123.mp4
     * @param targetFileName 截图保存的文件名称 123
     * @return 图片的地址
     * @throws Exception
     */
    public static MultipartFile executeCodecs(String filePath, String targetFileName) {
        try {
            FFmpegFrameGrabber ff = FFmpegFrameGrabber.createDefault(filePath);
            ff.start();
            String rotate = ff.getVideoMetadata("rotate");
            Frame f;
            int i = 0;
            MultipartFile fileName = null;
            while (i < 1) {
                f = ff.grabImage();
                IplImage src = null;
                if (null != rotate && rotate.length() > 1) {
                    OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
                    src = converter.convert(f);
                    f = converter.convert(rotate(src, Integer.valueOf(rotate)));
                }
                fileName = doExecuteFrame(f, targetFileName);
                i++;
            }
            ff.stop();
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * 旋转角度的
     */
    public static IplImage rotate(IplImage src, int angle) {
        IplImage img = IplImage.create(src.height(), src.width(), src.depth(), src.nChannels());
        opencv_core.cvTranspose(src, img);
        opencv_core.cvFlip(img, img, angle);
        return img;
    }


    public static MultipartFile doExecuteFrame(Frame f, String targetFileName) {

        if (null == f || null == f.image) {
            return null;
        }
        Java2DFrameConverter converter = new Java2DFrameConverter();
        String imageMat = "jpg";
        String fileName = targetFileName + "." + imageMat;
        BufferedImage bi = converter.getBufferedImage(f);
        //将newImage写入字节数组输出流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bi, imageMat, baos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] imageByte = baos.toByteArray();
        //将 byte[] 转为 MultipartFile
        MultipartFile multipartFile = new ConvertToMultipartFile(imageByte, targetFileName, fileName, "jpg", imageByte.length);

        return multipartFile;
    }

    public static String doExecuteFrame1(Frame f, String targetFilePath, String targetFileName) {

        if (null == f || null == f.image) {
            return null;
        }
        Java2DFrameConverter converter = new Java2DFrameConverter();
        String imageMat = "jpg";
        String fileName = targetFilePath + "/" + targetFileName + "." + imageMat;
        BufferedImage bi = converter.getBufferedImage(f);

        System.out.println("width:" + bi.getWidth());//打印宽、高
        System.out.println("height:" + bi.getHeight());
        File file = new File(fileName);
        try {
            ImageIO.write(bi, imageMat, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }
}
