package com.jack.bookshelf.view.fragment;

import static com.jack.bookshelf.constant.AppConstant.DEFAULT_WEB_DAV_URL;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jack.bookshelf.R;
import com.jack.bookshelf.base.observer.MySingleObserver;
import com.jack.bookshelf.databinding.FragmentWebdavSettingBinding;
import com.jack.bookshelf.help.storage.BackupRestoreUi;
import com.jack.bookshelf.help.storage.WebDavHelp;
import com.jack.bookshelf.utils.ToastsKt;
import com.jack.bookshelf.view.activity.SettingActivity;
import com.jack.bookshelf.view.dialog.InputDialog;

import java.util.ArrayList;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Webdav Setting Fragment
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class WebdavSettingFragment extends Fragment {
    private FragmentWebdavSettingBinding binding;
    private SettingActivity settingActivity;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingActivity = (SettingActivity) getActivity();
        assert settingActivity != null;
        settingActivity.setTile(R.string.web_dav_setting);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWebdavSettingBinding.inflate(inflater, container, false);
        bindView();
        return binding.getRoot();
    }

    private void bindView() {
        initText();
        binding.tvWebDavUrl.setOnClickListener(v -> InputDialog.builder(settingActivity, InputDialog.PREF_WITH_BIND_TV)
                .setTitle(getString(R.string.web_dav_url))
                .setDefaultValue()
                .setBindTextView(binding.tvWebDavUrlBindTv, DEFAULT_WEB_DAV_URL, false)
                .setPreferenceKey("web_dav_url", DEFAULT_WEB_DAV_URL).show());
        binding.tvWebDavAccount.setOnClickListener(v -> InputDialog.builder(settingActivity, InputDialog.PREF_WITH_BIND_TV)
                .setTitle(getString(R.string.web_dav_account))
                .setDefaultValue()
                .setBindTextView(binding.tvWebDavAccountBindTv,getString(R.string.input_web_dav_account), false)
                .setPreferenceKey("web_dav_account","").show());
        binding.tvWebDavPassword.setOnClickListener(v -> InputDialog.builder(settingActivity, InputDialog.PREF_WITH_BIND_TV)
                .setTitle(getString(R.string.web_dav_password))
                .setBindTextView(binding.tvWebDavPasswordBindTv,getString(R.string.input_web_dav_password), true)
                .setPreferenceKey("web_dav_password","").show());
        binding.tvWebDavBackup.setOnClickListener(v -> backup());
        binding.tvWebDavRestore.setOnClickListener(v -> restore());
    }

    private void initText() {
        InputDialog.builder(settingActivity)
                .setBindTextView(binding.tvWebDavUrlBindTv, DEFAULT_WEB_DAV_URL, false)
                .setPreferenceKey("web_dav_url", DEFAULT_WEB_DAV_URL);
        InputDialog.builder(settingActivity)
                .setBindTextView(binding.tvWebDavAccountBindTv,getString(R.string.input_web_dav_account), false)
                .setPreferenceKey("web_dav_account","");
        InputDialog.builder(settingActivity)
                .setBindTextView(binding.tvWebDavPasswordBindTv,getString(R.string.input_web_dav_password), true)
                .setPreferenceKey("web_dav_password","");
    }

    private void backup() {
        BackupRestoreUi.INSTANCE.backup(settingActivity, settingActivity.getRoot(), BackupRestoreUi.backupRestoreWebDav);
    }

    private void restore() {
        Single.create((SingleOnSubscribe<ArrayList<String>>) emitter -> emitter.onSuccess(WebDavHelp.INSTANCE.getWebDavFileNames())).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MySingleObserver<>() {
                    @Override
                    public void onSuccess(ArrayList<String> strings) {
                        if (!WebDavHelp.INSTANCE.showRestoreDialog(settingActivity, settingActivity.getRoot(), strings, BackupRestoreUi.INSTANCE)) {
                            ToastsKt.toast(settingActivity, R.string.no_backup, Toast.LENGTH_SHORT);
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }
}