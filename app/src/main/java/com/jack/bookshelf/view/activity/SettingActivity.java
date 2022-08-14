package com.jack.bookshelf.view.activity;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.jack.basemvplib.impl.IPresenter;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.MBaseActivity;
import com.jack.bookshelf.databinding.ActivitySettingsBinding;
import com.jack.bookshelf.help.storage.BackupRestoreUi;
import com.jack.bookshelf.utils.theme.ThemeStore;
import com.jack.bookshelf.view.fragment.SettingsFragment;

/**
 * 设置界面
 * Created by GKF on 2017/12/16.
 * Edited by Jack Ye
 * 启用了SettingsFragment.kt
 */

public class SettingActivity extends MBaseActivity<IPresenter> {

    private ActivitySettingsBinding binding;
    private final SettingsFragment settingsFragment = new SettingsFragment();

    public static void startThis(Context context) {
        context.startActivity(new Intent(context, SettingActivity.class));
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        this.setSupportActionBar(binding.toolbar);
        setupActionBar(getString(R.string.setting));

        getFragmentManager().beginTransaction()
                .replace(R.id.settingsFrameLayout, settingsFragment, "settings")
                .commit();
    }

    @Override
    protected void initData() {

    }

    //设置ToolBar
    public void setupActionBar(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(title);
        }
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        if (getFragmentManager().findFragmentByTag("settings") == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.settingsFrameLayout, settingsFragment, "settings")
                    .commit();
        } else {
            super.finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void initImmersionBar() {
        super.initImmersionBar();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BackupRestoreUi.INSTANCE.onActivityResult(requestCode, resultCode, data);
    }
}
