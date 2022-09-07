package com.jack.bookshelf.view.activity;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.jack.basemvplib.impl.IPresenter;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.MBaseActivity;
import com.jack.bookshelf.databinding.ActivitySettingsBinding;
import com.jack.bookshelf.help.storage.BackupRestoreUi;
import com.jack.bookshelf.utils.ToastsKt;
import com.jack.bookshelf.view.fragment.GeneralSettingFragment;
import com.jack.bookshelf.widget.modialog.MoDialogHUD;

/**
 * Setting Page
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class SettingActivity extends MBaseActivity<IPresenter> {

    private ActivitySettingsBinding binding;
    private final GeneralSettingFragment settingsFragment = new GeneralSettingFragment();
    private final String generalSettingTag = "general";
    public final String webdavSettingTag = "webdav";
    public final String aboutTag = "about";

    private MoDialogHUD moDialogHUD;

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
        getSupportFragmentManager().beginTransaction().replace(R.id.settingFragment, settingsFragment,generalSettingTag).commit();
    }

    @Override
    protected void bindView() {
        super.bindView();
        binding.ivBackSetting.setOnClickListener(v -> finish());
    }

    public void setTile(int strId) {
        binding.tvSettingTitle.setText(strId);
    }

    public View getRoot() {
        return binding.getRoot();
    }

    public MoDialogHUD getMoDialogHUD() {
        return moDialogHUD;
    }

    @Override
    protected void initData() {
        moDialogHUD = new MoDialogHUD(this);
    }

    @Override
    public void finish() {
        if (getSupportFragmentManager().findFragmentByTag(generalSettingTag) == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.settingFragment, settingsFragment, generalSettingTag).commit();
        } else {
            super.finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        moDialogHUD.dismiss();
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (getSupportFragmentManager().findFragmentByTag(aboutTag) != null) {
            Boolean mo = moDialogHUD.onKeyDown(keyCode, event);
            return mo || super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void toast(int strId, int length) {
        ToastsKt.toast(this, getString(strId), length);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BackupRestoreUi.INSTANCE.onActivityResult(requestCode, resultCode, data);
    }
}