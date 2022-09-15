package com.jack.bookshelf.service.update.listener;

public interface OnUpdateListener {

    /**
     * 下载文件异常，不是完整的文件或者文件包异常
     */
    void onException();

    /**
     * 下载进度变化
     */
    void onProgress(int progress);

    /**
     * 下载成功
     */
    void onSuccess();

    /**
     * 下载失败
     */
    void onFailed();

    /**
     * 下载已暂停
     */
    void onPaused();

    /**
     * 下载已取消
     */
    void onCanceled();
}