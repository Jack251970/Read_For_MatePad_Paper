package com.jack.bookshelf.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.SparseArray;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;
import com.jack.bookshelf.bean.DownloadBookBean;
import com.jack.bookshelf.bean.DownloadChapterBean;
import com.jack.bookshelf.model.impl.IDownloadTask;
import com.jack.bookshelf.model.task.DownloadTaskImpl;
import com.jack.bookshelf.utils.ToastsKt;
import com.jack.bookshelf.view.activity.DownloadActivity;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

/**
 * Download Service
 * Edited by Jack251970
 */

public class DownloadService extends Service {
    public static final String cancelAction = "cancelAction";
    public static final String addDownloadAction = "addDownload";
    public static final String removeDownloadAction = "removeDownloadAction";
    public static final String progressDownloadAction = "progressDownloadAction";
    public static final String obtainDownloadListAction = "obtainDownloadListAction";
    public static final String finishDownloadAction = "finishDownloadAction";
    private final int notificationId = 19901122;
    private int downloadTaskId = 0;
    private long currentTime;

    public static boolean isRunning = false;

    private ExecutorService executor;
    private Scheduler scheduler;
    private int threadsNum;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final SparseArray<IDownloadTask> downloadTasks = new SparseArray<>();

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        // 创建 Notification.Builder 对象
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIdDownload)
                .setSmallIcon(R.drawable.ic_download_noti)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setOngoing(false)
                .setContentTitle(getString(R.string.download_offline_t))
                .setContentText(getString(R.string.download_offline_s));
        // 发送通知
        Notification notification = builder.build();
        startForeground(notificationId, notification);

        SharedPreferences preferences = getSharedPreferences("CONFIG", 0);
        threadsNum = preferences.getInt(this.getString(R.string.pk_threads_num), 16);
        executor = Executors.newFixedThreadPool(threadsNum);
        scheduler = Schedulers.from(executor);
    }

    @Override
    public void onDestroy() {
        cancelDownload();
        isRunning = false;
        executor.shutdown();
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action == null) {
                finishSelf();
            } else {
                switch (action) {
                    case addDownloadAction:
                        DownloadBookBean downloadBook = intent.getParcelableExtra("downloadBook");
                        if (downloadBook != null) {
                            addDownload(downloadBook);
                        }
                        break;
                    case removeDownloadAction:
                        String noteUrl = intent.getStringExtra("noteUrl");
                        removeDownload(noteUrl);
                        break;
                    case cancelAction:
                        finishSelf();
                        break;
                    case obtainDownloadListAction:
                        refreshDownloadList();
                        break;

                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private synchronized void addDownload(DownloadBookBean downloadBook) {
        if (checkDownloadTaskExist(downloadBook)) {
            return;
        }
        downloadTaskId++;
        new DownloadTaskImpl(downloadTaskId, downloadBook) {
            @Override
            public void onDownloadPrepared(DownloadBookBean downloadBook) {
                if (canStartNextTask()) {
                    startDownload(scheduler);
                }
                downloadTasks.put(getId(), this);
                sendUpDownloadBook(addDownloadAction, downloadBook);
            }

            @Override
            public void onDownloadProgress(DownloadChapterBean chapterBean) {
                isProgress(chapterBean);
            }

            @Override
            public void onDownloadChange(DownloadBookBean downloadBook) {
                sendUpDownloadBook(progressDownloadAction, downloadBook);
            }

            @Override
            public void onDownloadError(DownloadBookBean downloadBook) {
                if (downloadTasks.indexOfValue(this) >= 0) {
                    downloadTasks.remove(getId());
                }
                ToastsKt.toast(DownloadService.this, downloadBook.getName() + "：" + getString(R.string.download_fail), Toast.LENGTH_SHORT);
                startNextTaskAfterRemove(downloadBook);
            }

            @Override
            public void onDownloadComplete(DownloadBookBean downloadBook) {
                if (downloadTasks.indexOfValue(this) >= 0) {
                    downloadTasks.remove(getId());
                }
                startNextTaskAfterRemove(downloadBook);
            }
        };
    }

    private void cancelDownload() {
        for (int i = downloadTasks.size() - 1; i >= 0; i--) {
            IDownloadTask downloadTask = downloadTasks.valueAt(i);
            downloadTask.stopDownload();
        }
    }

    private void removeDownload(String noteUrl) {
        if (noteUrl == null) {
            return;
        }

        for (int i = downloadTasks.size() - 1; i >= 0; i--) {
            IDownloadTask downloadTask = downloadTasks.valueAt(i);
            DownloadBookBean downloadBook = downloadTask.getDownloadBook();
            if (downloadBook != null && TextUtils.equals(noteUrl, downloadBook.getNoteUrl())) {
                downloadTask.stopDownload();
                break;
            }
        }
    }

    private void refreshDownloadList() {
        ArrayList<DownloadBookBean> downloadBookBeans = new ArrayList<>();
        for (int i = downloadTasks.size() - 1; i >= 0; i--) {
            IDownloadTask downloadTask = downloadTasks.valueAt(i);
            DownloadBookBean downloadBook = downloadTask.getDownloadBook();
            if (downloadBook != null) {
                downloadBookBeans.add(downloadBook);
            }
        }
        if (!downloadBookBeans.isEmpty()) {
            sendUpDownloadBooks(downloadBookBeans);
        }
    }

    private void startNextTaskAfterRemove(DownloadBookBean downloadBook) {
        sendUpDownloadBook(removeDownloadAction, downloadBook);
        handler.postDelayed(() -> {
            if (downloadTasks.size() == 0) {
                finishSelf();
            } else {
                startNextTask();
            }
        }, 1000);
    }

    private void startNextTask() {
        if (!canStartNextTask()) {
            return;
        }
        for (int i = 0; i < downloadTasks.size(); i++) {
            IDownloadTask downloadTask = downloadTasks.valueAt(i);
            if (!downloadTask.isDownloading()) {
                downloadTask.startDownload(scheduler);
                break;
            }
        }
    }


    private boolean canStartNextTask() {
        int downloading = 0;
        for (int i = downloadTasks.size() - 1; i >= 0; i--) {
            IDownloadTask downloadTask = downloadTasks.valueAt(i);
            if (downloadTask.isDownloading()) {
                downloading += 1;
            }
        }
        return downloading < threadsNum;
    }


    private synchronized boolean checkDownloadTaskExist(DownloadBookBean downloadBook) {
        for (int i = downloadTasks.size() - 1; i >= 0; i--) {
            IDownloadTask downloadTask = downloadTasks.valueAt(i);
            if (Objects.equals(downloadTask.getDownloadBook(), downloadBook)) {
                return true;
            }
        }
        return false;
    }


    private void sendUpDownloadBook(String action, DownloadBookBean downloadBook) {
        Intent intent = new Intent(action);
        intent.putExtra("downloadBook", downloadBook);
        sendBroadcast(intent);
    }

    private void sendUpDownloadBooks(ArrayList<DownloadBookBean> downloadBooks) {
        Intent intent = new Intent(obtainDownloadListAction);
        intent.putParcelableArrayListExtra("downloadBooks", downloadBooks);
        sendBroadcast(intent);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private PendingIntent getChancelPendingIntent() {
        Intent intent = new Intent(this, DownloadService.class);
        intent.setAction(DownloadService.cancelAction);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private synchronized void isProgress(DownloadChapterBean downloadChapterBean) {
        if (!isRunning) {
            return;
        }
        if (System.currentTimeMillis() - currentTime < 1000) { //更新太快无法取消
            return;
        }
        currentTime = System.currentTimeMillis();
        Intent mainIntent = new Intent(this, DownloadActivity.class);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // 创建 Notification.Builder 对象
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIdDownload)
                .setSmallIcon(R.drawable.ic_download_noti)
                // 通知栏大图标
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                // 点击通知后自动清除
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.is_downloading) + downloadChapterBean.getBookName())
                .setContentText(downloadChapterBean.getDurChapterName() == null ? "  " : downloadChapterBean.getDurChapterName())
                .setContentIntent(mainPendingIntent);
        builder.addAction(R.drawable.ic_stop_white, getString(R.string.cancel), getChancelPendingIntent());
        // 发送通知
        startForeground(notificationId, builder.build());
    }

    private void finishSelf() {
        sendBroadcast(new Intent(finishDownloadAction));
        stopSelf();
    }

    public static void addDownload(Context context, DownloadBookBean downloadBook) {
        if (context == null || downloadBook == null) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(addDownloadAction);
        intent.putExtra("downloadBook", downloadBook);
        context.startService(intent);
    }

    public static void removeDownload(Context context, String noteUrl) {
        if (noteUrl == null || !isRunning) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(removeDownloadAction);
        intent.putExtra("noteUrl", noteUrl);
        context.startService(intent);
    }

    public static void cancelDownload(Context context) {
        if (!isRunning) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(cancelAction);
        context.startService(intent);
    }

    public static void obtainDownloadList(Context context) {
        if (!isRunning) {
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(obtainDownloadListAction);
        context.startService(intent);
    }
}