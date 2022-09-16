package com.jack.bookshelf.service.update;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.os.Environment;

import com.jack.bookshelf.R;
import com.jack.bookshelf.bean.UpdateInfoBean;
import com.jack.bookshelf.service.update.listener.OnUpdateListener;
import com.jack.bookshelf.utils.StringUtils;

import java.io.File;

/**
 * Update Service
 * Edited by Jack251970
 */

public class UpdateService {
    private static volatile UpdateService manager = null;
    private UpdateDownloadTask updateDownloadTask;

    private final String fileParentPath;
    private String fileName;

    private UpdateService() {
        fileParentPath = Environment.getExternalStoragePublicDirectory(DOWNLOAD_SERVICE).getPath();
    }

    public static UpdateService getInstance() {
        if (manager == null) {
            synchronized (UpdateService.class) {
                if (manager == null) {
                    manager = new UpdateService();
                }
            }
        }
        return manager;
    }

    /**
     * 开始下载更新
     */
    public void startDownload(UpdateInfoBean updateInfo, OnUpdateListener onUpdateListener) {
        fileName = StringUtils.getString(R.string.app_name) + updateInfo.getLastVersion() + ".apk";
        if (updateDownloadTask == null) {
            updateDownloadTask = new UpdateDownloadTask(onUpdateListener);
            updateDownloadTask.execute(updateInfo.getUrl(), fileParentPath, fileName);
            updateDownloadTask.setOnDownloadTaskFinishedListener(new UpdateDownloadTask.OnUpdateDownloadTaskListener() {
                @Override
                public void onFinished() {
                    updateDownloadTask = null;
                }

                @Override
                public void onCanceled() {
                    clearCache();
                }

                @Override
                public void onException() {
                    clearCache();
                }
            });
        }
    }

    /**
     * 暂停下载任务
     */
    public void pauseDownload() {
        if (updateDownloadTask != null) {
            updateDownloadTask.pauseDownload();
        }
    }

    /**
     * 取消下载任务
     */
    public void cancelDownload() {
        if (updateDownloadTask != null) {
            updateDownloadTask.cancelDownload();
        }
    }

    /**
     * 清除下载缓存
     */
    public void clearCache() {
        File file = new File(fileParentPath + File.separator + fileName);
        if (file.exists()) {
            file.delete();
        }
    }
}
