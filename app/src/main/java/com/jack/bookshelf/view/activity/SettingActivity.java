package com.jack.bookshelf.view.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.annotation.Nullable;

import com.jack.basemvplib.impl.IPresenter;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.MBaseActivity;
import com.jack.bookshelf.databinding.ActivitySettingsBinding;
import com.jack.bookshelf.help.storage.BackupRestoreUi;

/**
 * Setting Page
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class SettingActivity extends MBaseActivity<IPresenter> {
    private ActivitySettingsBinding binding;

    public static void startThis(Context context) {
        context.startActivity(new Intent(context, SettingActivity.class));
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreateActivity() {
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void bindView() {
        super.bindView();
        binding.ivBackSetting.setOnClickListener(v -> finish());
        binding.swDefaultRead.setOnClickListener(v -> binding.swDefaultRead.setChecked(!binding.swDefaultRead.getChecked()));
        binding.tvBackPath.setOnClickListener(v -> BackupRestoreUi.INSTANCE.selectBackupFolder(this,binding.getRoot()));
    }

    /**
     * 设置界面标题
     */
    public void setTile(int strId) {
        binding.tvSettingTitle.setText(strId);
    }

    /**
     * 获得根View
     */
    public View getRoot() {
        return binding.getRoot();
    }

    @Override
    protected void initData() {
        binding.swDefaultRead.initPreferenceKey(R.string.pk_default_read,false);
    }

    @Override
    public void finish() {
        super.finish();
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