package com.jack.bookshelf;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import androidx.multidex.MultiDex;

import com.jack.bookshelf.help.AppFrontBackHelper;
import com.jack.bookshelf.help.CrashHandler;
import com.jack.bookshelf.model.UpLastChapterModel;
import com.jack.bookshelf.widget.dialog.ProgressDialog;

import java.util.Arrays;

import io.reactivex.internal.functions.Functions;
import io.reactivex.plugins.RxJavaPlugins;
import timber.log.Timber;

/**
 * MApplication
 * Edited by Jack251970
 */

public class MApplication extends Application {
    public final static String channelIdDownload = "channel_download";
    public final static String channelIdReadAloud = "channel_read_aloud";
    public final static String channelIdWeb = "channel_web";
    public static String SEARCH_GROUP = null;
    private static MApplication instance;
    private static String versionName;
    private static long versionCode;
    private SharedPreferences configPreferences;

    private ProgressDialog progressDialog;

    public static MApplication getInstance() {
        return instance;
    }

    public static String getVersionName() {
        return versionName;
    }

    public static long getVersionCode() {
        return versionCode;
    }

    public static Resources getAppResources() {
        return getInstance().getResources();
    }

    public static SharedPreferences getConfigPreferences() {
        return getInstance().configPreferences;
    }

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public void setProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        CrashHandler.getInstance().init(this);
        Timber.plant(new Timber.DebugTree());
        RxJavaPlugins.setErrorHandler(Functions.emptyConsumer());
        try {
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).getLongVersionCode();
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionCode = 0;
            versionName = "0.0.0";
        }
        createChannelId();
        configPreferences = getSharedPreferences("CONFIG", 0);
        AppFrontBackHelper.getInstance().register(this, new AppFrontBackHelper.OnAppStatusListener() {
            @Override
            public void onFront() {}

            @Override
            public void onBack() {
                UpLastChapterModel.destroy();
            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * ????????????ID
     */
    private void createChannelId() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //????????????ID??????????????????
        NotificationChannel downloadChannel = new NotificationChannel(channelIdDownload,
                getString(R.string.download_offline), NotificationManager.IMPORTANCE_LOW);
        //?????????channel
        downloadChannel.enableLights(false);
        downloadChannel.enableVibration(false);
        downloadChannel.setSound(null, null);

        //????????????ID??????????????????
        NotificationChannel readAloudChannel = new NotificationChannel(channelIdReadAloud,
                getString(R.string.read_aloud), NotificationManager.IMPORTANCE_LOW);
        //?????????channel
        readAloudChannel.enableLights(false);
        readAloudChannel.enableVibration(false);
        readAloudChannel.setSound(null, null);

        //????????????ID??????????????????
        NotificationChannel webChannel = new NotificationChannel(channelIdWeb,
                getString(R.string.web_service), NotificationManager.IMPORTANCE_LOW);
        //?????????channel
        webChannel.enableLights(false);
        webChannel.enableVibration(false);
        webChannel.setSound(null, null);

        //???notification manager ??????channel
        if (notificationManager != null) {
            notificationManager.createNotificationChannels(Arrays.asList(downloadChannel, readAloudChannel, webChannel));
        }
    }
}
