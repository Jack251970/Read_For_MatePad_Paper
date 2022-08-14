package com.jack.bookshelf.help;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jack.bookshelf.BuildConfig;
import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.BaseModelImpl;
import com.jack.bookshelf.base.observer.MyObserver;
import com.jack.bookshelf.bean.UpdateInfoBean;
import com.jack.bookshelf.model.analyzeRule.AnalyzeHeaders;
import com.jack.bookshelf.model.impl.IHttpGetApi;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class UpdateManager {
    private final Activity activity;

    public static UpdateManager getInstance(Activity activity) {
        return new UpdateManager(activity);
    }

    private UpdateManager(Activity activity) {
        this.activity = activity;
    }

    public void checkUpdate(boolean showMsg) {
        BaseModelImpl.getInstance().getRetrofitString("https://api.github.com")
                .create(IHttpGetApi.class)
                .get(MApplication.getInstance().getString(R.string.latest_release_api), AnalyzeHeaders.getDefaultHeader())
                .flatMap(response -> analyzeLastReleaseApi(response.body()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<UpdateInfoBean>() {
                    @Override
                    public void onNext(UpdateInfoBean updateInfo) {
                        if (updateInfo.getUpDate()) {

                        } else if (showMsg) {
                            Toast.makeText(activity, "已是最新版本", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (showMsg) {
                            Toast.makeText(activity, "检测新版本出错", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private Observable<UpdateInfoBean> analyzeLastReleaseApi(String jsonStr) {
        return Observable.create(emitter -> {
            try {
                UpdateInfoBean updateInfo = new UpdateInfoBean();
                JsonObject version = new JsonParser().parse(jsonStr).getAsJsonObject();
                if (version.get("prerelease").getAsBoolean())
                    return;
                JsonArray assets = version.get("assets").getAsJsonArray();
                if (assets.size() > 0) {
                    String lastVersion = version.get("tag_name").getAsString();
                    String url = assets.get(0).getAsJsonObject().get("browser_download_url").getAsString();
                    String detail = version.get("body").getAsString();
                    String thisVersion = MApplication.getVersionName().split("\\s")[0];
                    updateInfo.setUrl(url);
                    updateInfo.setLastVersion(lastVersion);
                    updateInfo.setDetail("# " + lastVersion + "\n" + detail);
                    updateInfo.setUpDate(Integer.valueOf(lastVersion.split("\\.")[2]) > Integer.valueOf(thisVersion.split("\\.")[2]));
                }
                emitter.onNext(updateInfo);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
                emitter.onComplete();
            }
        });
    }

    /**
     * 安装apk
     */
    public void installApk(File apkFile) {
        if (!apkFile.exists()) {
            return;
        }
        Intent intent = new Intent();
        //执行动作
        intent.setAction(Intent.ACTION_VIEW);
        //判读版本是否在7.0以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".fileProvider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        try {
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.d("wwd", "Failed to launcher installing activity");
        }
    }

    public static String getSavePath(String fileName) {
        return Environment.getExternalStoragePublicDirectory(DOWNLOAD_SERVICE).getPath() + fileName;
    }
}
