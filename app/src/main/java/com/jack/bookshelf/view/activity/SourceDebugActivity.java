package com.jack.bookshelf.view.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.jack.basemvplib.impl.IPresenter;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.MBaseActivity;
import com.jack.bookshelf.constant.RxBusTag;
import com.jack.bookshelf.databinding.ActivitySourceDebugBinding;
import com.jack.bookshelf.model.content.Debug;
import com.jack.bookshelf.utils.SoftInputUtil;
import com.jack.bookshelf.view.adapter.SourceDebugAdapter;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Book Source Debug Page
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class SourceDebugActivity extends MBaseActivity<IPresenter> {

    private ActivitySourceDebugBinding binding;
    private SourceDebugAdapter adapter;
    private CompositeDisposable compositeDisposable;
    private String sourceTag;

    public static void startThis(Context context, String sourceUrl) {
        if (TextUtils.isEmpty(sourceUrl)) return;
        Intent intent = new Intent(context, SourceDebugActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("sourceUrl", sourceUrl);
        context.startActivity(intent);
    }

    /**
     * P层绑定   若无则返回null;
     */
    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);
    }

    @Override
    protected void onDestroy() {
        Debug.SOURCE_DEBUG_TAG = null;
        RxBus.get().unregister(this);
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        super.onDestroy();
    }

    /**
     * 布局载入  setContentView()
     */
    @Override
    protected void onCreateActivity() {
        binding = ActivitySourceDebugBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    /**
     * 数据初始化
     */
    @Override
    protected void initData() {
        sourceTag = getIntent().getStringExtra("sourceUrl");
    }

    @Override
    protected void bindView() {
        super.bindView();
        initSearchView();
        binding.ivBack.setOnClickListener(v -> finish());
        adapter = new SourceDebugAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private void initSearchView() {
        LinearLayout editFrame = binding.searchView.findViewById(R.id.search_edit_frame);
        ImageView closeButton = binding.searchView.findViewById(R.id.search_close_btn);
        ImageView goButton = binding.searchView.findViewById(R.id.search_go_btn);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) editFrame.getLayoutParams();
        params.setMargins(20, 0, 10, 0);
        editFrame.setLayoutParams(params);
        closeButton.setScaleX(0.9f);
        closeButton.setScaleY(0.9f);
        closeButton.setPadding(0, 0, 0, 0);
        closeButton.setBackgroundColor(Color.TRANSPARENT);
        closeButton.setImageResource(R.drawable.ic_close);
        goButton.setScaleX(0.8f);
        goButton.setScaleY(0.8f);
        goButton.setPadding(0, 0, 0, 0);
        goButton.setBackgroundColor(Color.TRANSPARENT);
        goButton.setImageResource(R.drawable.ic_search);
        binding.searchView.setQueryHint(getString(R.string.debug_hint));
        binding.searchView.setSubmitButtonEnabled(true);
        binding.searchView.onActionViewExpanded();
        binding.searchView.setSubmitButtonEnabled(true);
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (TextUtils.isEmpty(query))
                    return false;
                startDebug(query);
                SoftInputUtil.hideIMM(binding.searchView);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void startDebug(String key) {
        if (TextUtils.isEmpty(sourceTag) || TextUtils.isEmpty(key)) {
            toast(R.string.cannot_empty);
            return;
        }
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        compositeDisposable = new CompositeDisposable();
        adapter.clearData();
        Debug.newDebug(sourceTag, key, compositeDisposable);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.PRINT_DEBUG_LOG)})
    public void printDebugLog(String msg) {
        adapter.addData(msg);
    }
}