package com.jack.bookshelf.view.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.jack.basemvplib.impl.IPresenter;
import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.MBaseActivity;
import com.jack.bookshelf.databinding.ActivityAboutBinding;
import com.jack.bookshelf.utils.ToastsKt;
import com.jack.bookshelf.utils.theme.ThemeStore;
import com.jack.bookshelf.widget.modialog.MoDialogHUD;

/**
 * About Page
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class AboutActivity extends MBaseActivity<IPresenter> {

    private MoDialogHUD moDialogHUD;
    private ActivityAboutBinding binding;

    public static void startThis(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void initData() {
        moDialogHUD = new MoDialogHUD(this);
    }

    @Override
    protected void bindView() {
        binding.ivBackAbout.setOnClickListener(v -> finish());
        binding.tvVersion.setText(getString(R.string.version_name, MApplication.getVersionName()));
    }

    /**
     * 设置按键监听
     */
    @Override
    protected void bindEvent() {
        // 查找更新
        binding.vwUpdate.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, getString(R.string.latest_release_url)));
        // 更新日志
        binding.vwUpdateLog.setOnClickListener(view -> moDialogHUD.showAssetMarkdown("updateLog.md"));
        // 项目主页(github)
        binding.vwGit.setOnClickListener(view -> openIntent(Intent.ACTION_VIEW, getString(R.string.this_github_url)));
        // 免责声明
        binding.vwDisclaimer.setOnClickListener(view -> moDialogHUD.showAssetMarkdown("disclaimer.md"));
    }

    /**
     * 打开外部网址
     */
    void openIntent(String intentName, String address) {
        try {
            Intent intent = new Intent(intentName);
            intent.setData(Uri.parse(address));
            startActivity(intent);
        } catch (Exception e) {
            ToastsKt.toast(this,R.string.can_not_open,Toast.LENGTH_LONG);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean mo = moDialogHUD.onKeyDown(keyCode, event);
        return mo || super.onKeyDown(keyCode, event);
    }
}
