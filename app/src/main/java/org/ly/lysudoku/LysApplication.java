package org.ly.lysudoku;

import android.app.Application;
import android.content.res.AssetManager;

import org.ly.lysudoku.db.OSDData;
import org.ly.lysudoku.utils.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;

public class LysApplication extends Application {
    private static LysApplication mInstance = null;

    public static LysApplication getInstance()
    {
        return mInstance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        LogUtil.setLogDir(LogUtil.getBasePath(this));
        Thread.setDefaultUncaughtExceptionHandler( uncaughtExceptionHandler  );
    }
    public AssetManager getAssetManager()
    {
        return  getAssets();
    }
    public InputStream openAssFile(String fileName) throws IOException {
        String[] list= this.getAssetManager().list("Web");
        InputStream is = this.getAssets().open(fileName);
        return is;
    }
    /**
     * 捕获错误信息的handler
     */
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            // LogUtil.showLog("我崩溃了");
            String info = null;
            ByteArrayOutputStream baos = null;
            PrintStream printStream = null;
            try {
                baos = new ByteArrayOutputStream();
                printStream = new PrintStream(baos);
                ex.printStackTrace(printStream);
                byte[] data = baos.toByteArray();
                info = new String(data);
                data = null;
                //LogUtil.e("App崩溃了",ex.getMessage());
                writeErrorLog(info);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (printStream != null) {
                        printStream.close();
                    }
                    if (baos != null) {
                        baos.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


//            Intent intent = new Intent(getApplicationContext(),
//                    CollapseActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
        }
    };

    /**
     * 向文件中写入错误信息
     *
     * @param info
     */
    protected void writeErrorLog(String info) {
        LogUtil.e("App崩溃了",info);
    }
}
