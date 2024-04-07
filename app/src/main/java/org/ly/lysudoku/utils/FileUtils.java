package org.ly.lysudoku.utils;

import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class FileUtils {
    /**
     * 删除文件或目录
     *
     * @param path 文件或目录。
     * @return true 表示删除成功，否则为失败
     */
    synchronized public static boolean delete(File path) {
        if (null == path) {
            return true;
        }

        if (path.isDirectory()) {
            File[] files = path.listFiles();
            if (null != files) {
                for (File file : files) {
                    if (!delete(file)) {
                        return false;
                    }
                }
            }
        }
        return !path.exists() || path.delete();
    }

    /**
     * 创建文件， 如果不存在则创建，否则返回原文件的File对象
     *
     * @param path 文件路径
     * @return 创建好的文件对象, 返回为空表示失败
     */
    synchronized public static File createFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }

        File file = new File(path);
        if (file.isFile()) {
            return file;
        }

        File parentFile = file.getParentFile();
        if (parentFile != null && (parentFile.isDirectory() || parentFile.mkdirs())) {
            try {
                if (file.createNewFile()) {
                    return file;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void writeToFile(String content, String filePath) {
        //FileWriter fileWriter = null;
        BufferedWriter fileWriter=null;
        try {
            fileWriter = new BufferedWriter(new FileWriter(filePath,true));
            /*fileWriter = new BufferedWriter(
                                    new OutputStreamWriter(
                                    new FileOutputStream(filePath,true), "UTF-8"));*/
            fileWriter.write(content);

            /*fileWriter = new FileWriter(filePath, true);
            BufferedWriter bufWriter = new BufferedWriter(fileWriter);
            bufWriter.write(content);
            bufWriter.newLine();
            bufWriter.close();
             fileWriter.flush();
            */
            fileWriter.flush();

        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
