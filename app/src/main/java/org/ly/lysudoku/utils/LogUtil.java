package org.ly.lysudoku.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogUtil {
    /**
     * Log中的常量是int值，不适合给外面使用，这里统一用这个枚举值进行设置
     */
    public enum LogLevel {
        VERBOSE(Log.VERBOSE),
        DEBUG(Log.DEBUG),
        INFO(Log.INFO),
        WARN(Log.WARN),
        ERROR(Log.ERROR),
        ASSERT(Log.ASSERT);

        private int mValue;

        LogLevel(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }
    }
    static String SDCARD_DIR = "/LyCache/";
    private static final int CACHE_QUEUE_SIZE = 5; //缓存最多5条log信息后输出到文件
    private static final SimpleDateFormat LOG_DATE_TIME_FORMAT = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");

    private static ExecutorService sLogExecutor = Executors.newSingleThreadExecutor();

    private static final String PREFIX = "lys:";
    private static boolean sLogEnable = true;
    private static LogLevel sLogLevel = LogLevel.DEBUG;
    private static Queue<String> sMsgQueue = new ArrayBlockingQueue<>(CACHE_QUEUE_SIZE);
    private static LogFileManager sLogFileManager;

    /**
     * 设置Log开关
     *
     * @param enable 开关项(默认为开).
     */
    public static void setEnable(boolean enable) {
        sLogEnable = enable;
    }

    public static void setLogLevel(LogLevel level) {
        sLogLevel = level;
    }
    public static String getBasePath(Context context) {
        try {
            File cacheDir = null;
            if (Build.VERSION.SDK_INT < 23) {
                if (android.os.Environment.getExternalStorageState().equals(
                        android.os.Environment.MEDIA_MOUNTED))
                    cacheDir = new File(
                            android.os.Environment.getExternalStorageDirectory(),
                            SDCARD_DIR);
                else
                    cacheDir = android.os.Environment.getDownloadCacheDirectory();
            } else {
                if (null != context) {
                    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                            || !Environment.isExternalStorageRemovable()) {
                        cacheDir = context.getExternalCacheDir();
                    } else {
                        cacheDir = context.getCacheDir();
                    }
                }
            }
            if (!cacheDir.exists())
                cacheDir.mkdirs();
            cacheDir = new File(cacheDir.getPath() + "/Log/");
            if (!cacheDir.exists())
                cacheDir.mkdirs();
            return cacheDir.getPath() + "/";

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return        android.os.Environment.getExternalStorageDirectory().getPath();
    }
    /**
     * 设置写入log的文件夹
     *
     * @param dirPath 文件夹地址
     */
    public static void setLogDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory()) {
            throw new InvalidParameterException();
        }
        sLogFileManager = new LogFileManager(dirPath);
    }

    /**
     * 程序退出时调用该方法
     *
     * @param
     */
    public static void close() {
        if (sLogFileManager != null) {
            sLogExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    flushLogToFile();
                }
            });
        }
    }

    /**
     * log for debug
     *
     * @param message log message
     * @param tag     tag
     * @see android.util.Log#d(String, String)
     */
    public static void d(String tag, String message) {
        if (sLogEnable) {
            String msg = PREFIX + message;
            Log.d(tag, msg);
            writeToFileIfNeeded(tag, msg, LogLevel.DEBUG);
        }
    }

    /**
     * log for debug
     *
     * @param message   log message
     * @param throwable throwable
     * @param tag       tag
     * @see android.util.Log#d(String, String, Throwable)
     */
    public static void d(String tag, String message, Throwable throwable) {
        if (sLogEnable) {
            String msg = PREFIX + message;
            Log.d(tag, msg, throwable);
            writeToFileIfNeeded(tag, msg + "\n" + Log.getStackTraceString(throwable), LogLevel.DEBUG);
        }
    }

    /**
     * log for debug
     *
     * @param tag    tag
     * @param format message format, such as "%d ..."
     * @param params message content params
     * @see android.util.Log#d(String, String)
     */
    public static void d(String tag, String format, Object... params) {
        if (sLogEnable) {
            String msg = String.format(PREFIX + format, params);
            Log.d(tag, msg);
            writeToFileIfNeeded(tag, msg, LogLevel.DEBUG);
        }
    }

    /**
     * log for warning
     *
     * @param message log message
     * @param tag     tag
     * @see android.util.Log#w(String, String)
     */
    public static void w(String tag, String message) {
        if (sLogEnable) {
            String msg = PREFIX + message;
            Log.w(tag, msg);
            writeToFileIfNeeded(tag, msg, LogLevel.WARN);
        }
    }

    /**
     * log for warning
     *
     * @param tag       tag
     * @param throwable throwable
     * @see android.util.Log#w(String, Throwable)
     */
    public static void w(String tag, Throwable throwable) {
        if (sLogEnable) {
            Log.w(tag, throwable);
            writeToFileIfNeeded(tag, Log.getStackTraceString(throwable), LogLevel.WARN);
        }
    }

    /**
     * log for warning
     *
     * @param message   log message
     * @param throwable throwable
     * @param tag       tag
     * @see android.util.Log#w(String, String, Throwable)
     */
    public static void w(String tag, String message, Throwable throwable) {
        if (sLogEnable) {
            String msg = PREFIX + message;
            Log.w(tag, msg, throwable);
            writeToFileIfNeeded(tag, msg + "\n" + Log.getStackTraceString(throwable), LogLevel.WARN);
        }
    }

    /**
     * log for warning
     *
     * @param tag    tag
     * @param format message format, such as "%d ..."
     * @param params message content params
     * @see android.util.Log#w(String, String)
     */
    public static void w(String tag, String format, Object... params) {
        if (sLogEnable) {
            String msg = String.format(PREFIX + format, params);
            Log.w(tag, msg);
            writeToFileIfNeeded(tag, msg, LogLevel.WARN);
        }
    }
    static String DTAG="lys";
    public static void e( String message) {
        e(DTAG,message);
    }
    /**
     * log for error
     *
     * @param message message
     * @param tag     tag
     * @see android.util.Log#i(String, String)
     */
    public static void e(String tag, String message) {
        if (sLogEnable) {
            String msg = PREFIX + message;
            Log.e(tag, msg);
            writeToFileIfNeeded(tag, msg, LogLevel.ERROR);
        }
    }

    /**
     * log for error
     *
     * @param message   log message
     * @param throwable throwable
     * @param tag       tag
     * @see android.util.Log#i(String, String, Throwable)
     */
    public static void e(String tag, String message, Throwable throwable) {
        if (sLogEnable) {
            String msg = PREFIX + message;
            Log.e(tag, msg, throwable);
            writeToFileIfNeeded(tag, msg + "\n" + Log.getStackTraceString(throwable), LogLevel.ERROR);
        }
    }

    /**
     * log for error
     *
     * @param tag    tag
     * @param format message format, such as "%d ..."
     * @param params message content params
     * @see android.util.Log#e(String, String)
     */
    public static void e(String tag, String format, Object... params) {
        if (sLogEnable) {
            String msg = String.format(PREFIX + format, params);
            Log.e(tag, msg);
            writeToFileIfNeeded(tag, msg, LogLevel.ERROR);
        }
    }
    public static void i( String message) {
        i(DTAG,message);
    }
    /**
     * log for information
     *
     * @param message message
     * @param tag     tag
     * @see android.util.Log#i(String, String)
     */
    public static void i(String tag, String message) {
        if (sLogEnable) {
            String msg = PREFIX + message;
            Log.i(tag, msg);
            writeToFileIfNeeded(tag, msg, LogLevel.INFO);
        }
    }

    /**
     * log for information
     *
     * @param message   log message
     * @param throwable throwable
     * @param tag       tag
     * @see android.util.Log#i(String, String, Throwable)
     */
    public static void i(String tag, String message, Throwable throwable) {
        if (sLogEnable) {
            String msg = message;
            Log.i(tag, PREFIX + msg, throwable);
            writeToFileIfNeeded(tag, msg + "\n" + Log.getStackTraceString(throwable), LogLevel.INFO);
        }
    }

    /**
     * log for information
     *
     * @param tag    tag
     * @param format message format, such as "%d ..."
     * @param params message content params
     * @see android.util.Log#i(String, String)
     */
    public static void i(String tag, String format, Object... params) {
        if (sLogEnable) {
            String msg = String.format(PREFIX + format, params);
            Log.i(tag, msg);
            writeToFileIfNeeded(tag, msg, LogLevel.INFO);
        }
    }

    /**
     * log for verbos
     *
     * @param message log message
     * @param tag     tag
     * @see android.util.Log#v(String, String)
     */
    public static void v(String tag, String message) {
        if (sLogEnable) {
            String msg = PREFIX + message;
            Log.v(tag, msg);
            writeToFileIfNeeded(tag, msg, LogLevel.VERBOSE);
        }
    }

    /**
     * log for verbose
     *
     * @param message   log message
     * @param throwable throwable
     * @param tag       tag
     * @see android.util.Log#v(String, String, Throwable)
     */
    public static void v(String tag, String message, Throwable throwable) {
        if (sLogEnable) {
            String msg = PREFIX + message;
            Log.v(tag, msg, throwable);
            writeToFileIfNeeded(tag, msg + "\n" + Log.getStackTraceString(throwable), LogLevel.VERBOSE);
        }
    }

    /**
     * log for verbose
     *
     * @param tag    tag
     * @param format message format, such as "%d ..."
     * @param params message content params
     * @see android.util.Log#v(String, String)
     */
    public static void v(String tag, String format, Object... params) {
        if (sLogEnable) {
            String msg = String.format(PREFIX + format, params);
            Log.v(tag, msg);
            writeToFileIfNeeded(tag, msg, LogLevel.VERBOSE);
        }
    }

    private static void writeToFileIfNeeded(final String tag, final String msg, LogLevel logLevel) {
        if (logLevel.getValue() < sLogLevel.getValue() || sLogFileManager == null) {
            return;
        }
        sLogExecutor.execute(new Runnable() {
            @Override
            public void run() {
                appendLog(tag, msg);
            }
        });
    }
    static long lastW=System.currentTimeMillis();
    private static void appendLog(String tag, String msg) {
        String logMsg = formatLog(tag, msg);
        sMsgQueue.add(logMsg);
        // 到达缓存上限，写到文件中
        if (sMsgQueue.size() >= CACHE_QUEUE_SIZE || System.currentTimeMillis()-lastW>10*1000) {
            lastW=System.currentTimeMillis();
            if (sMsgQueue.size() >=0)
                flushLogToFile();
        }
    }

    private static void flushLogToFile() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String message : sMsgQueue) {
            stringBuilder.append(message);
        }
        sLogFileManager.writeLogToFile(stringBuilder.toString());
        sMsgQueue.clear();
    }

    private static String formatLog(String tag, String msg) {
        return String.format("%s pid=%d %s: %s\n", LOG_DATE_TIME_FORMAT.format(new Date()), android.os.Process.myPid(), tag, msg);
    }
}



