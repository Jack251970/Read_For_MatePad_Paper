package com.jack.bookshelf.view.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;

import com.jack.basemvplib.impl.IPresenter;
import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.MBaseActivity;
import com.jack.bookshelf.databinding.ActivityAboutBinding;
import com.jack.bookshelf.utils.theme.ThemeStore;
import com.jack.bookshelf.widget.modialog.MoDialogHUD;

/**
 * 关于界面
 * Created by GKF on 2017/12/15.
 * Edited by Jack Ye
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

    /**
     * 仅在非E-Ink模式下才显示CardView的内部颜色
     */
    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        @SuppressLint("UseCompatLoadingForDrawables")
        Drawable bk_e_ink = getResources().getDrawable(R.drawable.bg_card_view);
        findViewById(R.id.vm_app_name).setBackground(bk_e_ink);
        findViewById(R.id.vw_version).setBackground(bk_e_ink);
        findViewById(R.id.vw_update).setBackground(bk_e_ink);
        findViewById(R.id.vw_update_log).setBackground(bk_e_ink);
        findViewById(R.id.vw_git).setBackground(bk_e_ink);
        findViewById(R.id.vw_disclaimer).setBackground(bk_e_ink);
    }

    @Override
    protected void initData() {
        moDialogHUD = new MoDialogHUD(this);
    }

    // 初始化关于界面
    @Override
    protected void bindView() {
        this.setSupportActionBar(binding.toolbar);
        setupActionBar();
        binding.tvVersion.setText(getString(R.string.version_name, MApplication.getVersionName()));
    }

    // 设置按键监听
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

    // 打开外部网址
    void openIntent(String intentName, String address) {
        try {
            Intent intent = new Intent(intentName);
            intent.setData(Uri.parse(address));
            startActivity(intent);
        } catch (Exception e) {
            toast(R.string.can_not_open, ERROR);
        }
    }

    // 设置顶部信息栏
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.about);
        }
    }

    // 菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean mo = moDialogHUD.onKeyDown(keyCode, event);
        return mo || super.onKeyDown(keyCode, event);
    }

}
