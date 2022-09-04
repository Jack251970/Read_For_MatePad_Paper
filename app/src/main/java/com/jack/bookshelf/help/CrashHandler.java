package com.jack.bookshelf.help;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.jack.bookshelf.utils.ToastsKt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;

/**
 * Crash Handler
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    /**
     * 系统默认UncaughtExceptionHandler
     */
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    /**
     * context
     */
    private Context mContext;

    /**
     * 存储异常和参数信息
     */
    private final Map<String, String> paramsMap = new HashMap<>();

    /**
     * 格式化时间
     */
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private final String TAG = this.getClass().getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private static CrashHandler mInstance;

    private CrashHandler() {

    }

    /**
     * 获取CrashHandler实例
     */
    public static synchronized CrashHandler getInstance() {
        if (null == mInstance) {
            mInstance = new CrashHandler();
        }
        return mInstance;
    }

    public void init(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为系统默认的
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * uncaughtException 回调函数
     */
    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果自己没处理交给系统处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            // 自己处理
            try {   // 延迟3秒杀进程
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Timber.tag(TAG).e(e, "error : ");
            }
        }

    }

    /**
     * 收集错误信息.发送到服务器
     *
     * @return 处理了该异常返回true, 否则false
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        // 收集设备参数信息
        collectDeviceInfo(mContext);
        // 添加自定义信息
        addCustomInfo();
        try {
            // 使用Toast来显示异常信息
            new Handler(Looper.getMainLooper()).post(() ->
                    ToastsKt.toast(mContext, ex.getMessage(), Toast.LENGTH_LONG));
        } catch (Exception ignored) {
        }
        // 保存日志文件
        saveCrashInfo2File(ex);
        return false;
    }


    /**
     * 收集设备参数信息
     */
    private void collectDeviceInfo(Context ctx) {
        // 获取versionName,versionCode
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.getLongVersionCode() + "";
                paramsMap.put("versionName", versionName);
                paramsMap.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Timber.tag(TAG).e(e, "an error occurred when collect package info");
        }
        // 获取所有系统信息
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                paramsMap.put(field.getName(), Objects.requireNonNull(field.get(null)).toString());
            } catch (Exception e) {
                Timber.tag(TAG).e(e, "an error occurred when collect crash info");
            }
        }
    }

    /**
     * 添加自定义参数
     */
    private void addCustomInfo() {
        Timber.tag(TAG).i("addCustomInfo: 程序出错了...");
    }

    /**
     * 保存错误信息到文件中
     */
    private void saveCrashInfo2File(Throwable ex) {

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append("=").append(value).append("\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            long timestamp = System.currentTimeMillis();
            String time = format.format(new Date());
            String fileName = "crash-" + time + "-" + timestamp + ".log";
            String path = FileHelp.getCachePath() + "/crash/";
            File dir = new File(path);
            if (!dir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(path + fileName);
            fos.write(sb.toString().getBytes());
            Timber.tag(TAG).i("saveCrashInfo2File: %s", sb);
            fos.close();
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "an error occurred while writing file...");
        }
    }
}
