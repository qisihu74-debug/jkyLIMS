package com.lims.manage.erp.util;


import com.alibaba.simpleimage.ImageFormat;
import com.alibaba.simpleimage.ImageWrapper;
import com.alibaba.simpleimage.SimpleImageException;
import com.alibaba.simpleimage.analyze.search.cluster.impl.Point;
import com.alibaba.simpleimage.render.CropParameter;
import com.alibaba.simpleimage.render.ScaleParameter;
import com.alibaba.simpleimage.render.ScaleParameter.Algorithm;
import com.alibaba.simpleimage.render.WatermarkParameter;
import com.alibaba.simpleimage.render.WriteParameter;
import com.alibaba.simpleimage.util.ImageCropHelper;
import com.alibaba.simpleimage.util.ImageDrawHelper;
import com.alibaba.simpleimage.util.ImageReadHelper;
import com.alibaba.simpleimage.util.ImageScaleHelper;
import com.alibaba.simpleimage.util.ImageWriteHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.PlanarImage;
import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.util
 * @desc
 * @date 2021/11/4 10:13
 * @Copyright © 河南交科院
 */
public class ImgUtils {


    public static String WATER_IMAGE_URL = "D:\\img\\watermark.png";
    public static List<File> fileList = new ArrayList();
    protected static ImageFormat outputFormat = ImageFormat.JPEG;

    public static void main(String[] args) throws Exception {
        // 输入输出文件路径/文件
        String src = "D:\\1.jpg";
        String res = "D:\\2.jpg";
        File srcFile = new File(src);
        File destFile = new File(res);

        // 将输入文件转换为字节数组
        byte[] bytes = getByte(srcFile);
        // 构造输入输出字节流
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // 处理图片
        zoomAndCut2(is, os, 600, 600);
        // 将字节输出流写到输出文件路径下
        writeFile(os, destFile);
    }

    /**
     *
     * @param pInput
     * @param pImgeFlag
     * @return
     * @throws Exception
     */
    public static boolean isPicture(String pInput, String pImgeFlag) throws Exception {
        if (StringUtils.isBlank(pInput)) {
            return false;
        }
        String tmpName = pInput.substring(pInput.lastIndexOf(".") + 1, pInput.length());
        String imgeArray[][] = {{"bmp", "0"}, {"dib", "1"}, {"gif", "2"}, {"jfif", "3"}, {"jpe", "4"}, {"jpeg", "5"}, {"jpg", "6"}, {"png", "7"}, {"tif", "8"},
                {"tiff", "9"}, {"ico", "10"}};
        for (int i = 0; i < imgeArray.length; i++) {
            if (!StringUtils.isBlank(pImgeFlag) && imgeArray[i][0].equals(tmpName.toLowerCase()) && imgeArray[i][1].equals(pImgeFlag)) {
                return true;
            }
            if (StringUtils.isBlank(pImgeFlag) && imgeArray[i][0].equals(tmpName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 按固定长宽进行缩放
     * @param is      输入流
     * @param os      输出流
     * @param width   指定长度
     * @param height  指定宽度
     * @throws Exception
     */
    public static void zoomImage(InputStream is, OutputStream os, int width, int height) throws Exception {
        //读取图片
        BufferedImage bufImg = ImageIO.read(is);
        is.close();
        //获取缩放比例
        double wRatio = width * 1.0/ bufImg.getWidth();
        double hRatio = height * 1.0 / bufImg.getHeight();

        AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(wRatio, hRatio), null);
        BufferedImage bufferedImage = ato.filter(bufImg, null);
        //写入缩减后的图片
        ImageIO.write(bufferedImage, "jpg", os);
    }

    /**
     * 按固定文件大小进行缩放
     * @param is     输入流
     * @param os     输出流
     * @param size   文件大小指定
     * @throws Exception
     */
    public static void zoomImage(InputStream is, OutputStream os, Integer size) throws Exception {
        /*FileInputStream的available()方法返回的是int类型，当数据大于1.99G(2147483647字节)后将无法计量，
            故求取流文件大小最好的方式是使用FileChannel的size()方法，其求取结果与File的length()方法的结果一致
            参考：http://blog.csdn.net/chaijunkun/article/details/22387305*/
        int fileSize = is.available();
        //文件大于size时，才进行缩放。注意：size以K为单位
        if(fileSize < size * 1024){
            return;
        }
        // 获取长*宽(面积)缩放比例
        double sizeRate = (size * 1024 * 0.5) / fileSize;
        // 获取长和宽分别的缩放比例，即面积缩放比例的2次方根
        double sideRate = Math.sqrt(sizeRate);
        BufferedImage bufImg = ImageIO.read(is);

        AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(sideRate, sideRate), null);
        BufferedImage bufferedImage = ato.filter(bufImg, null);
        ImageIO.write(bufferedImage, "jpg", os);
    }

    /**
     * 等比例缩放，以宽或高较大者达到指定长度为准
     * @param src      输入文件路径
     * @param dest     输出文件路径
     * @param width    指定宽
     * @param height   指定高
     */
    public static void zoomTo400(String src, String dest, Integer width, Integer height){
        try {
            File srcFile = new File(src);
            File destFile = new File(dest);
            BufferedImage bufImg = ImageIO.read(srcFile);
            int w0 = bufImg.getWidth();
            int h0 = bufImg.getHeight();
            // 获取较大的一个缩放比率作为整体缩放比率
            double wRatio = 1.0 * width / w0;
            double hRatio = 1.0 * height / h0;
            double ratio = Math.min(wRatio, hRatio);
            // 缩放
            AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(ratio, ratio), null);
            BufferedImage bufferedImage = ato.filter(bufImg, null);
            // 输出
            ImageIO.write(bufferedImage, dest.substring(dest.lastIndexOf(".")+1), destFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 等比例图片压缩，以宽或高较大者达到指定长度为准
     * @param is     输入流
     * @param os     输出流
     * @param width  宽
     * @param height 高
     * @throws IOException
     */
    public static void changeSize(InputStream is, OutputStream os, int width, int height) throws IOException {
        BufferedImage bis = ImageIO.read(is); // 构造Image对象
        is.close();

        int srcWidth = bis.getWidth(null);   // 得到源图宽
        int srcHeight = bis.getHeight(null); // 得到源图高

        if (width <= 0 || width > srcWidth) {
            width = bis.getWidth();
        }
        if (height <= 0 || height > srcHeight) {
            height = bis.getHeight();
        }
        // 若宽高小于指定最大值，不需重新绘制
        if (srcWidth <= width && srcHeight <= height) {
            ImageIO.write(bis, "jpg", os);
            os.close();
        } else {
            double scale =
                    ((double) width / srcWidth) > ((double) height / srcHeight) ?
                            ((double) height / srcHeight)
                            : ((double) width / srcWidth);
            width = (int) (srcWidth * scale);
            height = (int) (srcHeight * scale);

            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            bufferedImage.getGraphics().drawImage(bis, 0, 0, width, height, Color.WHITE, null); // 绘制缩小后的图
            ImageIO.write(bufferedImage, "jpg", os);
            os.close();
        }
    }

    /**
     * 先等比例缩放，小边缩放至指定长度后， 大边直接裁剪指指定长度
     * @param is
     * @param os
     * @param width
     * @param height
     */
    public final static void zoomAndCut1(InputStream is, OutputStream os, int width, int height) throws SimpleImageException {
        // 读文件
        ImageWrapper imageWrapper = ImageReadHelper.read(is);
        int w = imageWrapper.getWidth();
        int h = imageWrapper.getHeight();
        double wRatio = 1.0 * width / w;
        double hRatio = 1.0 * height / h;
        double ratio = Math.max(wRatio, hRatio);
        /*1.缩放*/
        // 缩放参数  如果图片宽和高都小于目标图片则不做缩放处理
        ScaleParameter scaleParam = null;
        if (w < width && h < height) {
            scaleParam = new ScaleParameter(w, h, Algorithm.LANCZOS);
        }
        // 为防止强转int时小数部分丢失，故加1，防止出现异常错误
        scaleParam = new ScaleParameter((int)(w * ratio) + 1, (int)(h * ratio) + 1, Algorithm.LANCZOS);
        // 缩放
        PlanarImage planarImage = ImageScaleHelper.scale(imageWrapper.getAsPlanarImage(), scaleParam);
        /*2.裁切*/
        // 获取裁剪偏移量
        imageWrapper = new ImageWrapper(planarImage);
        int w2 = imageWrapper.getWidth();
        int h2 = imageWrapper.getHeight();
        int x = (w2 - width) / 2;
        int y = (h2 - height) / 2;
        // 裁切参数   如果图片宽和高都小于目标图片则处理
        CropParameter cropParam = new CropParameter(x, y, width, height);
        if (x < 0 || y < 0) {
            cropParam = new CropParameter(0, 0, w, h);
        }
        // 裁剪
        planarImage = ImageCropHelper.crop(planarImage, cropParam);
        /*输出*/
        imageWrapper = new ImageWrapper(planarImage);
        String prefix = "jpg";
        ImageWriteHelper.write(imageWrapper, os, ImageFormat.getImageFormat(prefix), new WriteParameter());
    }

    /**
     * 先等比例缩放，小边缩放至指定长度后， 大边直接裁剪指指定长度
     * @param is
     * @param os
     * @param width
     * @param height
     */
    public static void zoomAndCut2(InputStream is, OutputStream os, Integer width, Integer height) throws IOException, SimpleImageException{
        // 读文件
        BufferedImage bufferedImage = ImageIO.read(is);
        int w = bufferedImage.getWidth();
        int h = bufferedImage.getHeight();
        // 获取缩放比例
        double wRatio = 1.0 * width / w;
        double hRatio = 1.0 * height / h;
        double ratio = Math.max(wRatio, hRatio);
        // 缩放
        AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(ratio, ratio), null);
        bufferedImage = ato.filter(bufferedImage, null);
        // 对象转换
        ImageWrapper imageWrapper = new ImageWrapper(bufferedImage);
        // 获得裁剪偏移量
        int w2 = imageWrapper.getWidth();
        int h2 = imageWrapper.getHeight();
        float x = (w2 - width) / 2.0f;
        float y = (h2 - height) / 2.0f;
        // 裁剪参数   如果图片宽和高都小于目标图片则处理
        CropParameter cropParameter = new CropParameter(x, y, width, height);
        if (x < 0 && y < 0) {
            cropParameter = new CropParameter(0, 0, width, height);
        }
        PlanarImage crop = ImageCropHelper.crop(imageWrapper.getAsPlanarImage(), cropParameter);
        imageWrapper = new ImageWrapper(crop);
        // 后缀
        String prefix = "jpg";
        // 写文件
        ImageWriteHelper.write(imageWrapper, os, ImageFormat.getImageFormat(prefix), new WriteParameter());
    }

    /**
     * 从中间裁切需要的大小
     * @param is
     * @param os
     * @param width
     * @param height
     */
    public static void CutCenter(InputStream is, OutputStream os, Integer width, Integer height) {
        try {
            ImageWrapper imageWrapper = ImageReadHelper.read(is);

            int w = imageWrapper.getWidth();
            int h = imageWrapper.getHeight();

            int x = (w - width) / 2;
            int y = (h - height) / 2;

            CropParameter cropParam = new CropParameter(x, y, width, height);// 裁切参数
            if (x < 0 || y < 0) {
                cropParam = new CropParameter(0, 0, w, h);// 裁切参数
            }

            PlanarImage planrImage = ImageCropHelper.crop(imageWrapper.getAsPlanarImage(), cropParam);
            imageWrapper = new ImageWrapper(planrImage);
            String prefix = "JPG";
            ImageWriteHelper.write(imageWrapper, os, ImageFormat.getImageFormat(prefix), new WriteParameter());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * 将file文件转为字节数组
     * @param file
     * @return
     */
    public static byte[] getByte(File file){
        byte[] bytes = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            bytes = new byte[fis.available()];
            fis.read(bytes);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * 将字节流写到指定文件
     * @param os
     * @param file
     */
    public static void writeFile(ByteArrayOutputStream os, File file){
        FileOutputStream fos = null;
        try {
            byte[] bytes = os.toByteArray();
            if (file.exists()) {
                file.delete();
            }
            fos = new FileOutputStream(file);
            fos.write(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 压缩图片到 指定尺寸,图片比目标图片小则不会变形(有水印）
     *
     * @param src
     * @param target
     * @param width
     * @param height
     */
    public final static void scaleWithWaterMark(String src, String target, int width, int height) {
        File out = new File(target); // 目的图片
        FileOutputStream outStream = null;
        File in = new File(src); // 原图片
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(in);
            ImageWrapper imageWrapper = ImageReadHelper.read(inStream);
            int w = imageWrapper.getWidth();
            int h = imageWrapper.getHeight();
            int cw=w, ch=h,x=0,y=0;
            boolean isDeal=true;
            if(height>h||width>w){
                isDeal=false;
            }else if((w - width)>(h-height)){
                ch=h;
                cw=(h*width)/height;
                x=(w-cw)/2;
                if(cw>h){
                    cw=w;
                    ch=(w*height)/width;
                    y=(h-ch)/2;
                    x=0;
                }

            }else if((w - width)<=(h-height)){
                cw=w;
                ch=(w*height)/width;
                y=(h-ch)/2;
                if(ch>h){
                    ch=h;
                    cw=(h*width)/height;
                    x=(w-cw)/2;
                    y=0;
                }
            }
            System.out.println("x: "+x+" y" +y+"cw: "+cw+" ch"+ch+"");
            if(isDeal){
                CropParameter cropParam = new CropParameter(x, y, cw, ch);// 裁切参数
                PlanarImage planrImage = ImageCropHelper.crop(imageWrapper.getAsPlanarImage(), cropParam);
                ScaleParameter scaleParam = new ScaleParameter(width, height, Algorithm.LANCZOS); // 缩放参数
                planrImage = ImageScaleHelper.scale(planrImage, scaleParam);
                imageWrapper = new ImageWrapper(planrImage);
            }
            // 3.打水印
            BufferedImage waterImage = ImageIO.read(new File(WATER_IMAGE_URL));
            ImageWrapper waterWrapper = new ImageWrapper(waterImage);
            Point p =calculate(imageWrapper.getWidth(),imageWrapper.getHeight(),
                    waterWrapper.getWidth(), waterWrapper.getHeight());
            WatermarkParameter param = new WatermarkParameter(waterWrapper, 1f,(int) p.getX(),(int) p.getY());
            BufferedImage bufferedImage = ImageDrawHelper.drawWatermark(imageWrapper.getAsBufferedImage(), param);
            imageWrapper = new ImageWrapper(bufferedImage);
            // 4.输出
            outStream = new FileOutputStream(out);
            String prefix = out.getName().substring(out.getName().lastIndexOf(".") + 1);
            ImageWriteHelper.write(imageWrapper, outStream, outputFormat.getImageFormat(prefix), new WriteParameter());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SimpleImageException e) {
        } finally {
            IOUtils.closeQuietly(inStream); // 图片文件输入输出流必须记得关闭
            IOUtils.closeQuietly(outStream);

        }

    }

    public static Point calculate(int enclosingWidth, int enclosingHeight,
                                  int width, int height){
        int x = (enclosingWidth / 2) - (width / 2);
        int y = (enclosingHeight / 2) - (height / 2);
        return new Point(x, y);
    }

    /**
     * 说明：根据指定URL将文件下载到指定目标位置
     *
     * @param urlPath
     *            下载路径
     * @param downloadDir
     *            文件存放目录
     * @return 返回下载文件
     */
    @SuppressWarnings("finally")
    public File downloadFile(String urlPath, String downloadDir) {
        File file = null;
        try {
            // 统一资源
            URL url = new URL(urlPath);
            // 连接类的父类，抽象类
            URLConnection urlConnection = url.openConnection();
            // http的连接类
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            //设置超时
            httpURLConnection.setConnectTimeout(1000*5);
            //设置请求方式，默认是GET
            httpURLConnection.setRequestMethod("POST");
            // 设置字符编码
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            // 打开到此 URL引用的资源的通信链接（如果尚未建立这样的连接）。
            httpURLConnection.connect();
            // 文件大小
            int fileLength = httpURLConnection.getContentLength();
            BufferedInputStream bin = new BufferedInputStream(httpURLConnection.getInputStream());
            // 指定存放位置(有需求可以自定义)
            String path = downloadDir + File.separatorChar;
            file = new File(path);
            // 校验文件夹目录是否存在，不存在就创建一个目录
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            OutputStream out = new FileOutputStream(file);
            int size = 0;
            int len = 0;
            byte[] buf = new byte[2048];
            while ((size = bin.read(buf)) != -1) {
                len += size;
                out.write(buf, 0, size);
                // 控制台打印文件下载的百分比情况
                System.out.println("下载了-------> " + len * 100 / fileLength + "%\n");
            }
            // 关闭资源
            bin.close();
            out.close();
            System.out.println("文件下载成功！");
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("文件下载失败！");
        } finally {
            return file;
        }

    }

    /**
     * 去除文件列表里图片的水印并替换
     *
     * @Param fileList 文件列表
     */
    public static void convertAllImages(List<File> fileList) {
        try {
            for (File file : fileList) {
                if (!file.getName().endsWith("png") && !file.getName().endsWith("jpg")) {
                    continue;
                }
                BufferedImage bi = ImageIO.read(file); //用ImageIO流读取像素块
                if (bi != null) {
                    removeWatermark(bi);
                    String formatName = file.getName().substring(file.getName().lastIndexOf(".") + 1);//生成的图片格式
                    ImageIO.write(bi, formatName, file);//用ImageIO流生成的处理图替换原图片
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从源目录获取图片处理后导出到目标目录
     *
     * @param dir     源目录
     * @param saveDir 目标目录
     */
    public static void convertAllImages(String dir, String saveDir) {
        File dirFile = new File(dir);
        File saveDirFile = new File(saveDir);
        dir = dirFile.getAbsolutePath();
        saveDir = saveDirFile.getAbsolutePath();
        loadImages(new File(dir));
        for (File file : fileList) {
            String filePath = file.getAbsolutePath();
            String dstPath = saveDir + filePath.substring(filePath.indexOf(dir) + dir.length());
            replace(file.getAbsolutePath(), dstPath);
        }
    }

    /**
     * 加载图片
     */
    private static void loadImages(File f) {
        if (f != null) {
            if (f.isDirectory()) {
                File[] fileArray = f.listFiles();
                if (fileArray != null) {
                    for (File file : fileArray) {
                        loadImages(file); //递归调用
                    }
                }
            } else {
                String name = f.getName();
                if (name.endsWith("png") || name.endsWith("jpg")) {
                    fileList.add(f);
                }
            }
        }
    }

    /**
     * 生成源图片的处理图
     *
     * @param srcFile 源图片路径
     * @param dstFile 目标图片路径
     */
    private static void replace(String srcFile, String dstFile) {
        try {
            URL http;
            if (srcFile.trim().startsWith("https")) {
                http = new URL(srcFile);
                HttpsURLConnection conn = (HttpsURLConnection) http.openConnection();
                conn.setRequestMethod("GET");
            } else if (srcFile.trim().startsWith("http")) {
                http = new URL(srcFile);
                HttpURLConnection conn = (HttpURLConnection) http.openConnection();
                conn.setRequestMethod("GET");
            } else {
                http = new File(srcFile).toURI().toURL();
            }
            BufferedImage bi = ImageIO.read(http.openStream());
            if (bi != null) {
                removeWatermark(bi);
                exportImage(bi, srcFile, dstFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 由ImageIO流生成源图片的处理图
     *
     * @param bi       ImageIO
     * @param fileName 源图片带后缀的文件名
     * @param dstFile  目标图片路径
     */
    private static void exportImage(BufferedImage bi, String fileName, String dstFile) {
        try {
            String type = fileName.substring(fileName.lastIndexOf(".") + 1);
            Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName(type);
            ImageWriter writer = it.next();
            File f = new File(dstFile);
            ImageOutputStream ios = ImageIO.createImageOutputStream(f);
            writer.setOutput(ios);
            writer.write(bi);
            bi.flush();
            ios.flush();
            ios.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 去除水印
     */
    private static void removeWatermark(BufferedImage bi) {
        Color wColor = new Color(254, 254, 254);
        Color hColor = new Color(197, 196, 191);
        //白底水印
        for (int i = 0; i < bi.getWidth(); i++) {
            for (int j = 0; j < bi.getHeight(); j++) {
                int color = bi.getRGB(i, j);
                Color oriColor = new Color(color);
                int red = oriColor.getRed();
                int greed = oriColor.getGreen();
                int blue = oriColor.getBlue();
                if (red == 254 && greed == 254 && blue == 254) {
                    continue;
                }
                if (red > 220 && greed > 180 && blue > 80) {
                    bi.setRGB(i, j, wColor.getRGB());
                }
                if (red <= 240 && greed >= 200 && blue >= 150) {
                    bi.setRGB(i, j, wColor.getRGB());
                }
            }
        }
    }

}

