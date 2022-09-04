package com.jack.bookshelf.view.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;
import com.jack.bookshelf.base.observer.MySingleObserver;
import com.jack.bookshelf.databinding.FragmentWebdavSettingBinding;
import com.jack.bookshelf.help.ProcessTextHelp;
import com.jack.bookshelf.help.storage.BackupRestoreUi;
import com.jack.bookshelf.help.storage.WebDavHelp;
import com.jack.bookshelf.utils.ToastsKt;
import com.jack.bookshelf.view.activity.SettingActivity;

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

    private final SharedPreferences pref = MApplication.getConfigPreferences();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingActivity = (SettingActivity) getActivity();
        assert settingActivity != null;
        settingActivity.setTile(R.string.webdav_setting);
        initData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWebdavSettingBinding.inflate(inflater, container, false);
        bindView();
        return binding.getRoot();
    }

    private void initData() {
        pref.edit().putBoolean("process_text", ProcessTextHelp.isProcessTextEnabled()).apply();
    }

    private void bindView() {
        binding.tvWebDavRestore.setOnClickListener(v -> restore());
        /*binding.tvWebdavSetting.setOnClickListener(null);
        binding.tvBackPath.setOnClickListener(v -> BackupRestoreUi.INSTANCE.selectBackupFolder(settingActivity,binding.getRoot()));
        binding.tvFilterGradeNumber.setText(String.valueOf());
        binding.tvSearchResultFilterGrade.setOnClickListener(v ->
                NumberPickerDialog.builder(settingActivity,binding.tvFilterGradeNumber)
                        .setTitle(R.string.search_result_filter_grade)
                        .setMinValue(0)
                        .setMaxValue(9)
                        .setPreferenceKey(R.string.pk_search_result_filter_grade,0)
                        .show(binding.getRoot()));
        binding.tvUpdateSearchThreadsNum.setOnClickListener(v ->
                NumberPickerDialog.builder(settingActivity,binding.tvThreadsNumber)
                        .setTitle(R.string.update_search_threads_num)
                        .setMinValue(1)
                        .setMaxValue(1024)
                        .setPreferenceKey(R.string.pk_threads_num,16)
                        .show(binding.getRoot()));
        binding.tvWebPort.setOnClickListener(v ->
                NumberPickerDialog.builder(settingActivity,binding.tvWebPortNumber)
                        .setTitle(R.string.web_port_title)
                        .setMinValue(1024)
                        .setMaxValue(60000)
                        .setPreferenceKey("webPort",1122)
                        .setAddedListener((oldValue, value) -> {
                            if (value != oldValue) {
                                WebService.upHttpServer(settingActivity);
                            }
                        })
                        .show(binding.getRoot()));
        binding.tvClearCache.setOnClickListener(v ->
                PaperAlertDialog.builder(settingActivity)
                    .setType(PaperAlertDialog.ONLY_CENTER_TITLE)
                    .setTitle(R.string.sure_delete_download_book)
                    .setNegativeButton(R.string.cancel)
                    .setPositiveButton(R.string.delete)
                    .setOnclick(new PaperAlertDialog.OnItemClickListener() {
                        @Override
                        public void forNegativeButton() {
                            BookshelfHelp.clearCaches(false);
                        }

                        @Override
                        public void forPositiveButton() {
                            BookshelfHelp.clearCaches(true);
                        }
                    }).show(binding.getRoot()));
        binding.tvAboutRead.setOnClickListener(v -> AboutActivity.startThis(settingActivity));*/
    }

    private void restore() {
        Single.create((SingleOnSubscribe<ArrayList<String>>) emitter -> emitter.onSuccess(WebDavHelp.INSTANCE.getWebDavFileNames())).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MySingleObserver<>() {
                    @Override
                    public void onSuccess(ArrayList<String> strings) {
                        if (!WebDavHelp.INSTANCE.showRestoreDialog(settingActivity, strings, BackupRestoreUi.INSTANCE)) {
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
