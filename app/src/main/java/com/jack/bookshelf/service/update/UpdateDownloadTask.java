package com.jack.bookshelf.service.update;

import android.os.AsyncTask;

import com.jack.bookshelf.service.update.listener.OnUpdateListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Update Download Task
 * Edited by Jack251970
 */

public class UpdateDownloadTask extends AsyncTask<String, Integer, Integer> {
    private static final int TYPE_SUCCESS = 0;
    private static final int TYPE_FAILED = 1;
    private static final int TYPE_PAUSED = 2;
    private static final int TYPE_CANCELED = 3;

    private final OnUpdateListener mOnUpdateListener;
    private OnDownloadTaskFinishedListener mOnDownloadTaskFinishedListener;

    private boolean isCanceled = false;
    private boolean isPaused = false;

    private int lastProgress;
    private File mDownloadFile = null;
    private long mContentLength;    // 记录url下载文件的长度

    public UpdateDownloadTask(OnUpdateListener onUpdateListener) {
        mOnUpdateListener = onUpdateListener;
    }

    public void setOnDownloadTaskFinishedListener(OnDownloadTaskFinishedListener onDownloadTaskFinishedListener) {
        mOnDownloadTaskFinishedListener = onDownloadTaskFinishedListener;
    }

    @Override
    protected Integer doInBackground(String... params) {
        InputStream is = null;
        RandomAccessFile savedFile = null;
        try {

            long downloadLength = 0; // 记录已下载的文件长度
            String downloadUrl = params[0];
            String fileParentPath = params[1];
            String fileName = params[2];
            mDownloadFile = new File(fileParentPath, fileName);
            if (mDownloadFile.exists()) {
                downloadLength = mDownloadFile.length();
            }

            mContentLength = getContentLength(downloadUrl);
            if (mContentLength == 0) {
                return TYPE_FAILED;
            } else if (mContentLength == downloadLength) {
                // 已下载字节和文件总字节相等，说明已经下载完成了
                return TYPE_SUCCESS;
            }
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    // 断点下载，指定从哪个字节开始下载
                    .addHeader("RANGE", "bytes=" + downloadLength + "-")
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            is = Objects.requireNonNull(response.body()).byteStream();
            savedFile = new RandomAccessFile(mDownloadFile, "rw");
            savedFile.seek(downloadLength); // 跳过已下载的字节
            byte[] b = new byte[1024];
            int total = 0;
            int len;
            while ((len = is.read(b)) != -1) {
                if (isCanceled) {
                    return TYPE_CANCELED;
                } else if (isPaused) {
                    return TYPE_PAUSED;
                } else {
                    total += len;
                    savedFile.write(b, 0, len);
                    // 计算已下载的百分比
                    int progress = (int) ((total + downloadLength) * 100 / mContentLength);
                    publishProgress(progress);
                }
            }
            Objects.requireNonNull(response.body()).close();
            return TYPE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (savedFile != null) {
                    savedFile.close();
                }
                if (isCanceled && mDownloadFile != null) {
                    mDownloadFile.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress) {
            mOnUpdateListener.onProgress(progress);
            lastProgress = progress;
        }
    }

    @Override
    protected void onPostExecute(Integer status) {
        switch (status) {
            case TYPE_SUCCESS:
                if (mContentLength != mDownloadFile.length()) {
                    if (mOnUpdateListener != null)
                        mOnUpdateListener.onException();
                    // 下载数据异常，告知downManager下载任务已失败
                    if (mOnDownloadTaskFinishedListener != null)
                        mOnDownloadTaskFinishedListener.onException();
                } else {
                    if (mOnUpdateListener != null)
                        mOnUpdateListener.onSuccess();
                }
                break;
            case TYPE_FAILED:
                if (mOnUpdateListener != null)
                    mOnUpdateListener.onFailed();
                break;
            case TYPE_PAUSED:
                if (mOnUpdateListener != null)
                    mOnUpdateListener.onPaused();
                break;
            case TYPE_CANCELED:
                if (mOnUpdateListener != null)
                    mOnUpdateListener.onCanceled();

                if (mOnDownloadTaskFinishedListener != null)
                    mOnDownloadTaskFinishedListener.onCanceled();
            default:
                break;
        }

        if (mOnDownloadTaskFinishedListener != null)
            mOnDownloadTaskFinishedListener.onFinished();
    }

    /**
     * 暂停下载任务
     */
    public void pauseDownload() {
        isPaused = true;
    }

    /**
     * 取消下载任务
     */
    public void cancelDownload() {
        isCanceled = true;
    }

    /**
     * 获取下载文件长度
     * @param downloadUrl 下载文件url
     * @return 下载文件长度
     * @throws IOException IOException
     */
    private long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            long contentLength = Objects.requireNonNull(response.body()).contentLength();
            response.close();
            return contentLength;
        }
        return 0;
    }

    public interface OnDownloadTaskFinishedListener {
        /**
         * 下载任务已结束
         */
        void onFinished();

        /**
         * 下载任务已取消
         */
        void onCanceled();

        /**
         * 下载文件异常，不是完整的文件或者文件包异常
         */
        void onException();
    }
}