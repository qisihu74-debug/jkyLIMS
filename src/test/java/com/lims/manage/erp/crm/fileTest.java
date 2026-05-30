package com.lims.manage.erp.crm;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * @author gjl
 * @version V1.0
 * @Package com.lims.manage.demo.crm
 * @desc
 * @date 2021/9/17 14:54
 * @Copyright © 河南交科院
 */
public class fileTest {
    public static void main(String[] args) throws Exception{
        //modifyPDFJar();
        //String jarPath = "D:\\doc\\Apose\\pdf\\lib\\aspose.pdf-21.8.jar";
        //crack(jarPath);
        //modifyExcelJar();
        modifyPptJar();
    }

    /**
     * 修改pdf jar包里面的校验
     */
    public static void modifyPDFJar() {
        try {
            //这一步是完整的jar包路径,选择自己解压的jar目录
            ClassPool.getDefault().insertClassPath("D:\\doc\\Apose\\pdf\\lib\\aspose.pdf-21.11.jar");
            //获取指定的class文件对象
            CtClass zzZJJClass = ClassPool.getDefault().getCtClass("com.aspose.pdf.l9f");
            //从class对象中解析获取所有方法
            CtMethod[] methodA = zzZJJClass.getDeclaredMethods();
            for (CtMethod ctMethod : methodA) {
                //获取方法获取参数类型
                CtClass[] ps = ctMethod.getParameterTypes();
                //筛选同名方法，入参是Document
                if (ps.length == 1 && ctMethod.getName().equals("lI") && ps[0].getName().equals("java.io.InputStream")) {
                    System.out.println("ps[0].getName==" + ps[0].getName());
                    //替换指定方法的方法体
                    ctMethod.setBody("{this.l0if = com.aspose.pdf.l10if.lf;com.aspose.pdf.internal.imaging.internal.p71.Helper.help1();lI(this);}");
                }
            }
            //这一步就是将破译完的代码放在桌面上
            zzZJJClass.writeFile("D:\\Users\\Administrator\\Desktop\\res");

        } catch (Exception e) {
            System.out.println("错误==" + e);
        }
    }

    /**
     * 修改words jar包里面的校验
     */
    public static void modifyWordsJar() {
        try {
            //这一步是完整的jar包路径,选择自己解压的jar目录
            ClassPool.getDefault().insertClassPath("D:\\doc\\Apose\\word\\lib\\aspose-words-21.11.0-jdk17.jar");
            //获取指定的class文件对象
            CtClass zzZJJClass = ClassPool.getDefault().getCtClass("com.aspose.words.zzXDb");
            //从class对象中解析获取指定的方法
            CtMethod[] methodA = zzZJJClass.getDeclaredMethods("zzY0J");
            //遍历重载的方法
            for (CtMethod ctMethod : methodA) {
                CtClass[] ps = ctMethod.getParameterTypes();
                if (ctMethod.getName().equals("zzY0J")) {
                    System.out.println("ps[0].getName==" + ps[0].getName());
                    //替换指定方法的方法体
                    ctMethod.setBody("{this.zzZ3l = new java.util.Date(Long.MAX_VALUE);this.zzWSL = com.aspose.words.zzYeQ.zzXgr;zzWiV = this;}");
                }
            }
            //这一步就是将破译完的代码放在桌面上
            zzZJJClass.writeFile("D:\\Users\\Administrator\\Desktop\\res");

            //获取指定的class文件对象
            CtClass zzZJJClassB = ClassPool.getDefault().getCtClass("com.aspose.words.zzYKk");
            //从class对象中解析获取指定的方法
            CtMethod methodB = zzZJJClassB.getDeclaredMethod("zzWy3");
            //替换指定方法的方法体
            methodB.setBody("{return 256;}");
            //这一步就是将破译完的代码放在桌面上
            zzZJJClassB.writeFile("D:\\Users\\Administrator\\Desktop\\res");
        } catch (Exception e) {
            System.out.println("错误==" + e);
        }
    }

    /**
     * 修改cells.jar包里面的校验
     */
    public static void modifyExcelJar() {
        try {
            //这一步是完整的jar包路径,选择自己解压的jar目录
            ClassPool.getDefault().insertClassPath("D:\\doc\\Apose\\excel\\JDK 1.6\\lib\\aspose-cells-21.11.jar");
            //获取指定的class文件对象
            CtClass zzZJJClass = ClassPool.getDefault().getCtClass("com.aspose.cells.License");
            //从class对象中解析获取所有方法
            CtMethod[] methodA = zzZJJClass.getDeclaredMethods();
            for (CtMethod ctMethod : methodA) {
                //获取方法获取参数类型
                CtClass[] ps = ctMethod.getParameterTypes();
                //筛选同名方法，入参是Document
                if (ps.length == 1 && ctMethod.getName().equals("a") && ps[0].getName().equals("org.w3c.dom.Document")) {
                    System.out.println("ps[0].getName==" + ps[0].getName());
                    //替换指定方法的方法体
                    ctMethod.setBody("{a = this;com.aspose.cells.zblc.a();}");
                }
            }
            //这一步就是将破译完的代码放在桌面上
            zzZJJClass.writeFile("D:\\doc\\");

        } catch (Exception e) {
            System.out.println("错误==" + e);
        }
    }

    /**
     * 修改slides.jar包里面的校验
     */
    public static void modifyPptJar() {
        try {
            //这一步是完整的jar包路径,选择自己解压的jar目录
            ClassPool.getDefault().insertClassPath("D:\\doc\\Apose\\ppt\\lib\\aspose-slides-21.10-jdk16.jar");
            CtClass zzZJJClass = ClassPool.getDefault().getCtClass("com.aspose.slides.internal.of.public");
            CtMethod[] methodA = zzZJJClass.getDeclaredMethods();
            for (CtMethod ctMethod : methodA) {
                CtClass[] ps = ctMethod.getParameterTypes();
                if (ps.length == 3 && ctMethod.getName().equals("do")) {
                    System.out.println("ps[0].getName==" + ps[0].getName());
                    ctMethod.setBody("{}");
                }
            }
            //这一步就是将破译完的代码放在桌面上
            zzZJJClass.writeFile("D:\\doc\\Apose\\ppt\\lib\\");
        } catch (Exception e) {
            System.out.println("错误==" + e);
        }
    }

    /**
     * 第二种破解方式22.8
     * @param jarName
     */
    private static void crack(String jarName) {
        try {
            ClassPool.getDefault().insertClassPath(jarName);
            CtClass ctClass = ClassPool.getDefault().getCtClass("com.aspose.pdf.ADocument");
            CtMethod[] declaredMethods = ctClass.getDeclaredMethods();
            int num = 0;
            for (int i = 0; i < declaredMethods.length; i++) {
                if (num == 2) {
                    break;
                }
                CtMethod method = declaredMethods[i];
                CtClass[] ps = method.getParameterTypes();
                if (ps.length == 2
                        && method.getName().equals("lI")
                        && ps[0].getName().equals("com.aspose.pdf.ADocument")
                        && ps[1].getName().equals("int")) {
                    // 最多只能转换4页 处理
                    System.out.println(method.getReturnType());
                    System.out.println(ps[1].getName());
                    method.setBody("{return false;}");
                    num = 1;
                }
                if (ps.length == 0 && method.getName().equals("lt")) {
                    // 水印处理
                    method.setBody("{return true;}");
                    num = 2;
                }
            }
            File file=new File(jarName);
            ctClass.writeFile(file.getParent());
            disposeJar(jarName, file.getParent()+"/com/aspose/pdf/ADocument.class");
        } catch(NotFoundException e){
            e.printStackTrace();
        } catch(CannotCompileException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void disposeJar(String jarName, String replaceFile) {
        List<String> deletes = new ArrayList<>();
        deletes.add("META-INF/37E3C32D.SF");
        deletes.add("META-INF/37E3C32D.RSA");
        File oriFile = new File(jarName);
        if (!oriFile.exists()) {
            System.out.println("######Not Find File:" + jarName);
            return;
        }
        //将文件名命名成备份文件
        String bakJarName = jarName.substring(0, jarName.length() - 3) + "cracked.jar";
        //   File bakFile=new File(bakJarName);
        try {
            //创建文件（根据备份文件并删除部分）
            JarFile jarFile = new JarFile(jarName);
            JarOutputStream jos = new JarOutputStream(new FileOutputStream(bakJarName));
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                if (!deletes.contains(entry.getName())) {
                    if(entry.getName().equals("com/aspose/pdf/ADocument.class")){
                        System.out.println("Replace:-------" +entry.getName());
                        JarEntry jarEntry = new JarEntry(entry.getName());
                        jos.putNextEntry(jarEntry);
                        FileInputStream fin = new FileInputStream(replaceFile);
                        byte[] bytes = readStream(fin);
                        jos.write(bytes, 0, bytes.length);
                    }else {
                        jos.putNextEntry(entry);
                        byte[] bytes = readStream(jarFile.getInputStream(entry));
                        jos.write(bytes, 0, bytes.length);
                    }
                } else {
                    System.out.println("Delete:-------" + entry.getName());
                }
            }
            jos.flush();
            jos.close();
            jarFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        outSteam.close();
        inStream.close();
        return outSteam.toByteArray();
    }

}
