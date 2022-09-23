package com.jack.bookshelf.view.activity;

import static com.jack.bookshelf.service.DownloadService.addDownloadAction;
import static com.jack.bookshelf.service.DownloadService.finishDownloadAction;
import static com.jack.bookshelf.service.DownloadService.obtainDownloadListAction;
import static com.jack.bookshelf.service.DownloadService.progressDownloadAction;
import static com.jack.bookshelf.service.DownloadService.removeDownloadAction;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.jack.basemvplib.impl.IPresenter;
import com.jack.bookshelf.base.MBaseActivity;
import com.jack.bookshelf.bean.DownloadBookBean;
import com.jack.bookshelf.databinding.ActivityDownloadBinding;
import com.jack.bookshelf.service.DownloadService;
import com.jack.bookshelf.view.adapter.DownloadAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Download Page
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class DownloadActivity extends MBaseActivity<IPresenter> {

    private ActivityDownloadBinding binding;
    private DownloadAdapter adapter;
    private DownloadReceiver receiver;

    public static void startThis(Activity activity) {
        activity.startActivity(new Intent(activity, DownloadActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    /**
     * P层绑定   若无则返回null;
     */
    @Override
    protected IPresenter initInjector() {
        return null;
    }

    /**
     * 布局载入  setContentView()
     */
    @Override
    protected void onCreateActivity() {
        binding = ActivityDownloadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    /**
     * 数据初始化
     */
    @Override
    protected void initData() {
        receiver = new DownloadReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(addDownloadAction);
        filter.addAction(removeDownloadAction);
        filter.addAction(progressDownloadAction);
        filter.addAction(obtainDownloadListAction);
        filter.addAction(finishDownloadAction);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void bindView() {
        // 初始化RecyclerView
        initRecyclerView();
        // 返回
        binding.ivBack.setOnClickListener(v -> finish());
        // 停止下载
        binding.ivStopDownload.setOnClickListener(v -> DownloadService.cancelDownload(this));
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DownloadAdapter(this);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setItemAnimator(null);
        DownloadService.obtainDownloadList(this);
    }

    private static class DownloadReceiver extends BroadcastReceiver {

        WeakReference<DownloadActivity> ref;

        public DownloadReceiver(DownloadActivity activity) {
            this.ref = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadAdapter adapter = ref.get().adapter;
            if (adapter == null || intent == null) {
                return;
            }
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case addDownloadAction:
                        DownloadBookBean downloadBook =
                                intent.getParcelableExtra("downloadBook");
                        adapter.addData(downloadBook);
                        break;
                    case removeDownloadAction:
                        downloadBook = intent.getParcelableExtra("downloadBook");
                        adapter.removeData(downloadBook);
                        break;
                    case progressDownloadAction:
                        downloadBook = intent.getParcelableExtra("downloadBook");
                        adapter.upData(downloadBook);
                        break;
                    case finishDownloadAction:
                        adapter.upDataS(null);
                        break;
                    case obtainDownloadListAction:
                        ArrayList<DownloadBookBean> downloadBooks =
                                intent.getParcelableArrayListExtra("downloadBooks");
                        adapter.upDataS(downloadBooks);
                        break;

                }
            }
        }
    }
}
