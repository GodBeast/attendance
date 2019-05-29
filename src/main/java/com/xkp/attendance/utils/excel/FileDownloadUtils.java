package com.xkp.attendance.utils.excel;

import com.xkp.attendance.utils.DateUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Chaoyi.Guo 2019/3/14 16:41
 * @version:
 */
public class FileDownloadUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileDownloadUtils.class);

    /**
     * 编译下载的文件名
     * @param filename
     * @param agent
     * @return
     * @throws IOException
     */
    public static String encodeDownloadFilename(String filename, String agent)throws IOException {
        if (agent.contains("Firefox")) { // 火狐浏览器
            filename = "=?UTF-8?B?"
                    + new BASE64Encoder().encode(filename.getBytes("utf-8"))
                    + "?=";
            filename = filename.replaceAll("\r\n", "");
        } else { // IE及其他浏览器
            filename = URLEncoder.encode(filename, "utf-8");
            filename = filename.replace("+"," ");
        }
        return filename;
    }

    /**
     * 创建文件夹;
     * @param path
     */
    public static void createFile(String path) {
        File file = new File(path);
        //判断文件是否存在;
        if (!file.exists()) {
            //创建文件;
            file.mkdirs();
        }
    }

    /**
     * 生成.zip文件;
     * @param path
     * @throws IOException
     */
//    public static ZipOutputStream craeteZipPath(String path) throws IOException{
//        ZipOutputStream zipOutputStream = null;
//        File file = new File(path+DateUtils.getDateWx()+".zip");
//        zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
//        File[] files = new File(path).listFiles();
//        FileInputStream fileInputStream = null;
//        byte[] buf = new byte[1024];
//        int len = 0;
//        if(files!=null && files.length > 0){
//            for(File excelFile:files){
//                String fileName = excelFile.getName();
//                fileInputStream = new FileInputStream(excelFile);
//                //放入压缩zip包中;
//                zipOutputStream.putNextEntry(new ZipEntry(path + "/"+fileName));
//                //读取文件;
//                while((len=fileInputStream.read(buf)) >0){
//                    zipOutputStream.write(buf, 0, len);
//                }
//                //关闭;
//                zipOutputStream.closeEntry();
//                if(fileInputStream != null){
//                    fileInputStream.close();
//                }
//            }
//        }
//
//        /*if(zipOutputStream !=null){
//            zipOutputStream.close();
//        }*/
//        return zipOutputStream;
//    }


    /**
     * 压缩文件
     *
     * @param response
     * @param srcfile     要压缩的文件数组
     * @param zipfile     生成的zip文件对象
     * @param zipFilePath 压缩包临时预约了
     */
    public static void ZipFiles(HttpServletResponse response, File[] srcfile, File zipfile, String zipFilePath) {
        byte[] buf = new byte[1024];
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
                    zipfile));
            for (int i = 0; i < srcfile.length; i++) {
                FileInputStream in = new FileInputStream(srcfile[i]);
                out.putNextEntry(new ZipEntry(srcfile[i].getName()));
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();

            response.setContentType("application/zip");
            response.setHeader("Location", zipfile.getName());
            response.setHeader("Content-Disposition", "attachment; filename=" + zipfile.getName());
            OutputStream outputStream = response.getOutputStream();
            InputStream inputStream = new FileInputStream(zipFilePath);
            byte[] buffer = new byte[1024];
            int i = -1;
            while ((i = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, i);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            // 删除文件夹及文件夹下所有文件
            deleteDir(zipfile);
            for (File file : srcfile) {
                deleteDir(file);
            }
        } catch (Exception e) {
            logger.error("压缩文件异常", e);
        }
    }

    /**
     * 删除文件夹及文件夹下所有文件
     * @param dir
     * @return
     */
    public static boolean deleteDir(File dir) {
        if (dir == null || !dir.exists()){
            return true;
        }
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    /**
     * 生成html
     * @param msg
     * @return
     * @author zgd
     * @time 2018年6月25日11:47:07
     */
    public static String getErrorHtml(String msg) {
        StringBuffer sb = new StringBuffer();
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<div id='errorInfo'> ");
        sb.append("</div>");
        sb.append("<script>alert('"+msg+"')</script>");
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }


    /**
     * 设置下载excel的响应头信息
     * @param response
     * @param request
     * @param agent
     * @param fileName
     * @throws IOException
     * @author zgd
     * @time 2018年6月25日11:47:07
     */
    public static void setExcelHeadInfo(HttpServletResponse response, HttpServletRequest request, String fileName)  {
        try {
            // 获取客户端浏览器的类型
            String agent = request.getHeader("User-Agent");
            // 对文件名重新编码
            String encodingFileName = FileDownloadUtils.encodeDownloadFilename(fileName, agent);
            // 告诉客户端允许断点续传多线程连接下载
            response.setHeader("Accept-Ranges", "bytes");
            //文件后缀
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + encodingFileName);
        } catch (IOException e) {
            logger.error(Thread.currentThread().getStackTrace()[1].getMethodName() +"发生的异常是: ",e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置下载zip的响应头信息
     * @param response
     * @param fileName 文件名
     * @param request
     * @throws IOException
     * @author zgd
     * @time 2018年6月25日11:47:07
     */
    public static void setZipDownLoadHeadInfo(HttpServletResponse response, HttpServletRequest request, String fileName) throws IOException {
        // 获取客户端浏览器的类型
        String agent = request.getHeader("User-Agent");
        response.setContentType("application/octet-stream ");
        // 表示不能用浏览器直接打开
        response.setHeader("Connection", "close");
        // 告诉客户端允许断点续传多线程连接下载
        response.setHeader("Accept-Ranges", "bytes");
        // 对文件名重新编码
        String encodingFileName = FileDownloadUtils.encodeDownloadFilename(fileName, agent);
        response.setHeader("Content-Disposition", "attachment; filename=" + encodingFileName);
    }



    public void export(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException {
        Long t0 = System.currentTimeMillis();

            //zip文件中的Excel文件数
            Long excelNumber = 0L;
            //Excel文件中的sheet页数量
            Long sheetNumber = 0L;
            logger.info("##########所有Excel中sheet数量总和 : " + sheetNumber);

            String excelDate = DateUtil.formatDate(new Date(),"YYYYMMddHHmmss");
            String excelBaseName = "Event-"+ excelDate + "-*.xlsx";

            //遍历zip压缩文件中的每个Excel文件
            for(Long j = 0L; j < excelNumber; j++) {
                // 在内存中缓存100行数据(100测试最佳)
                SXSSFWorkbook workBook = new SXSSFWorkbook(100);
                // 临时文件进行压缩，建议不要true，否则会影响导出时间
                workBook.setCompressTempFiles(false);
                String excelName = "Event-" + excelDate + "-"+ (j+1) + ".xlsx";
                File excelFile = new File(excelName);
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(excelFile);
                } catch (FileNotFoundException e) {
                    logger.info("创建内存Excel文件时出错");
                }

                try {
                    workBook.write(out);
                    out.close();
                    logger.info("@@@@@@@@@@" + excelName +"文件写入数据完毕");
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    logger.info(excelName+ "缓存至本地时异常");
                }
            }

//            logger.info("================数据查询结束，开始生成.zip压缩文件，敬请期待......");
//            List<File> excelFileList = FileUtil.getFiles("./", excelBaseName);
//            try {
//                File zip = new File("Event-"+ excelDate + ".zip");// 压缩文件
//                FileUtil.zipFiles(excelFileList, zip,response.getOutputStream());
//                FileUtil.deleteFile(zip);
//                FileUtil.deleteFile(excelFileList);
//            } catch (IOException e) {
//                logger.error(e.getMessage(), e);
//            }
        logger.info("###############导出告警Excel文件结束###############耗时" + (System.currentTimeMillis() - t0) + "ms");
    }

    public static File[] getFiles(String dir, String s) {
        // 开始的文件夹
        File file = new File(dir);
        s = s.replace('.', '#');
        s = s.replaceAll("#", "\\\\.");
        s = s.replace('*', '#');
        s = s.replaceAll("#", ".*");
        s = s.replace('?', '#');
        s = s.replaceAll("#", ".?");
        s = "^" + s + "$";
        Pattern p = Pattern.compile(s);
        ArrayList list = filePattern(file, p);
        File[] rtn = new File[list.size()];
        list.toArray(rtn);
        return rtn;
    }

    private static ArrayList filePattern(File file, Pattern p) {
        if (file == null) {
            return null;
        } else if (file.isFile()) {
            Matcher fMatcher = p.matcher(file.getName());
            if (fMatcher.matches()) {
                ArrayList list = new ArrayList();
                list.add(file);
                return list;
            }
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                ArrayList list = new ArrayList();
                for (int i = 0; i < files.length; i++) {
                    ArrayList rlist = filePattern(files[i], p);
                    if (rlist != null) {
                        list.addAll(rlist);
                    }
                }
                return list;
            }
        }
        return null;
    }
}

