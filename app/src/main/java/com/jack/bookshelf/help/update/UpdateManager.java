package com.jack.bookshelf.help.update;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
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
import com.jack.bookshelf.service.update.listener.OnUpdateListener;
import com.jack.bookshelf.service.update.UpdateService;
import com.jack.bookshelf.utils.MarkdownUtils;
import com.jack.bookshelf.utils.ToastsKt;
import com.jack.bookshelf.widget.dialog.PaperAlertDialog;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Update Management
 * Edited by Jack251970
 */

public class UpdateManager {
    private final Activity activity;
    private CallBack callBack;

    private UpdateManager(Activity activity) {
        this.activity = activity;
    }

    public static UpdateManager getInstance(Activity activity) {
        return new UpdateManager(activity);
    }

    /**
     * 检查Github更新
     */
    public void checkUpdate(Context context, View mainView, boolean showMsg, CallBack callBack) {
        this.callBack = callBack;
        BaseModelImpl.getInstance().getRetrofitString("https://api.github.com")
                .create(IHttpGetApi.class)
                .get(MApplication.getInstance().getString(R.string.latest_release_api), AnalyzeHeaders.getDefaultHeader())
                .flatMap(response -> analyzeLastReleaseApi(response.body()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<>() {
                    @Override
                    public void onNext(UpdateInfoBean updateInfo) {
                        if (updateInfo.getUpDate()) {
                            PaperAlertDialog.builder(context)
                                    .setType(PaperAlertDialog.NO_APPEND_MESSAGE)
                                    .setTitle(R.string.check_update)
                                    .setMessage(String.valueOf(MarkdownUtils.simpleMarkdownConverter(updateInfo.getDetail())))
                                    .setNegativeButton(R.string.cancel)
                                    .setPositiveButton(R.string.confirm)
                                    .setOnclick(new PaperAlertDialog.OnItemClickListener() {
                                        @Override
                                        public void forNegativeButton() {

                                        }

                                        @Override
                                        public void forPositiveButton() {
                                            callBack.showDialog();
                                            startUpdate(context, updateInfo);
                                        }
                                    })
                                    .show(mainView);
                        } else if (showMsg) {
                            ToastsKt.toast(activity, R.string.up_to_date, Toast.LENGTH_SHORT);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (showMsg) {
                            ToastsKt.toast(activity, R.string.check_update_error, Toast.LENGTH_SHORT);
                        }
                    }
                });
    }

    /**
     * 分析更新信息
     */
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
                    updateInfo.setUpDate(Integer.parseInt(lastVersion.split("\\.")[2]) > Integer.parseInt(thisVersion.split("\\.")[2]));
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
     * 开始更新与下载
     */
    public void startUpdate(Context context, UpdateInfoBean updateInfo) {
        UpdateService.getInstance().startDownload(updateInfo, new OnUpdateListener() {
            @Override
            public void onException() {
                callBack.dismissDialog();
            }

            @Override
            public void onProgress(int progress) {
                callBack.setProgress(progress);
            }

            @Override
            public void onSuccess() {
                callBack.dismissDialog();
                installApk(getApkPath(context.getString(R.string.app_name) + updateInfo.getLastVersion()));
            }

            @Override
            public void onFailed() {
                callBack.dismissDialog();
                ToastsKt.toast(context, R.string.download_fail, Toast.LENGTH_SHORT);
            }

            @Override
            public void onPaused() {

            }

            @Override
            public void onCanceled() {

            }});
    }

    /**
     * 安装apk文件
     */
    private void installApk(String apkPath) {
        File apkFile = new File(apkPath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri apkUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".fileProvider", apkFile);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        try {
            activity.startActivity(intent);
        } catch (Exception e) {
            ToastsKt.toast(activity, R.string.cannot_go_to_install_page, Toast.LENGTH_SHORT);
        }
    }

    /**
     * 获取安装包路径
     */
    private String getApkPath(String fileName) {
        return Environment.getExternalStoragePublicDirectory(DOWNLOAD_SERVICE).getPath() + File.separator + fileName + ".apk";
    }

    public interface CallBack {
        void setProgress(int progress);

        void showDialog();

        void dismissDialog();
    }
}
