package com.lims.manage.erp.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageUtil {

    public static void main(String[] args) {
        createDateImage(",","");
    }

    public static void createDateImage(String url,String reportCompleteDate) {
        int width = 120;
        int height = 30;
        File file = new File(url);
        Font font = new Font("Serif", Font.BOLD, 10);
        // 创建一个画布
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 获取画布的画笔
        Graphics2D g2 = (Graphics2D) bi.getGraphics();
        // 开始绘图
        // ----------  增加下面的代码使得背景透明  -----------------
        bi = g2.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        g2.dispose();
        g2 = bi.createGraphics();
        g2.setColor(new Color(0, 0, 0));
        g2.setStroke(new BasicStroke(1));
        FontRenderContext context = g2.getFontRenderContext();
        Rectangle2D bounds = font.getStringBounds(reportCompleteDate, context);
        double x = (width - bounds.getWidth()) / 2;
        double y = (height - bounds.getHeight()) / 2;
        double ascent = -bounds.getY();
        double baseY = y + ascent;
        g2.drawString(reportCompleteDate, (int) x, (int) baseY);
        //释放对象
        g2.dispose();
        try {
            ImageIO.write(bi, "png", file);
        } catch (IOException e) {
            System.out.println("出错........");
            e.printStackTrace();
        }
    }

    public static BufferedImage drawTranslucentStringPic(int width, int height, Integer fontHeight, String drawStr) {
        try {
            BufferedImage buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D gd = buffImg.createGraphics();
            //设置透明 start
            buffImg = gd.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
            gd = buffImg.createGraphics();
            //设置透明 end
            gd.setFont(new Font("微软雅黑", Font.PLAIN, fontHeight)); //设置字体
            gd.setColor(Color.ORANGE); //设置颜色
            gd.drawRect(0, 0, width - 1, height - 1); //画边框
            gd.drawString(drawStr, width / 2 - fontHeight * drawStr.length() / 2, fontHeight); //输出文字(中文横向居中)
            return buffImg;
        } catch (Exception e) {
            return null;
        }
    }

    public static void main2(String[] args) {
        BufferedImage imgMap = drawTranslucentStringPic(400, 80, 36, "欢迎访问我的博客");
        File imgFile = new File("D:\\doc\\saveOriginalRecord\\test.png");
        try {
            ImageIO.write(imgMap, "PNG", imgFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("生成完成");
    }
}
