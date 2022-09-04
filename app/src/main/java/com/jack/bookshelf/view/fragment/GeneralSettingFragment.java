package com.jack.bookshelf.view.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;
import com.jack.bookshelf.databinding.FragmentGeneralSettingBinding;
import com.jack.bookshelf.help.BookshelfHelp;
import com.jack.bookshelf.help.ProcessTextHelp;
import com.jack.bookshelf.help.storage.BackupRestoreUi;
import com.jack.bookshelf.service.WebService;
import com.jack.bookshelf.view.activity.AboutActivity;
import com.jack.bookshelf.view.activity.SettingActivity;
import com.jack.bookshelf.view.dialog.PaperAlertDialog;
import com.jack.bookshelf.widget.number.NumberPickerDialog;

/**
 * General Setting Fragment
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class GeneralSettingFragment extends Fragment {

    private FragmentGeneralSettingBinding binding;
    private SettingActivity settingActivity;
    private final SharedPreferences pref = MApplication.getConfigPreferences();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingActivity = (SettingActivity) getActivity();
        assert settingActivity != null;
        settingActivity.setTile(R.string.setting);
        initData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGeneralSettingBinding.inflate(inflater, container, false);
        bindView();
        return binding.getRoot();
    }

    private void initData() {
        pref.edit().putBoolean("process_text", ProcessTextHelp.isProcessTextEnabled()).apply();
    }

    private void bindView() {
        bindSwitch();
        binding.tvWebdavSetting.setOnClickListener(v -> {
            WebdavSettingFragment webDavSettingFragment = new WebdavSettingFragment();
            settingActivity.getSupportFragmentManager().beginTransaction().replace(R.id.settingFragment, webDavSettingFragment,settingActivity.webdavSettingTag).commit();
        });
        binding.tvBackPath.setOnClickListener(v -> BackupRestoreUi.INSTANCE.selectBackupFolder(settingActivity,settingActivity.getRoot()));
        binding.tvFilterGradeNumber.setText(String.valueOf(pref.getInt(getString(R.string.pk_search_result_filter_grade),0)));
        binding.tvSearchResultFilterGrade.setOnClickListener(v ->
                NumberPickerDialog.builder(settingActivity)
                        .setBindTextView(binding.tvFilterGradeNumber)
                        .setTitle(R.string.search_result_filter_grade)
                        .setMinValue(0)
                        .setMaxValue(9)
                        .setPreferenceKey(R.string.pk_search_result_filter_grade,0)
                        .show(settingActivity.getRoot()));
        binding.tvThreadsNumber.setText(String.valueOf(pref.getInt(getString(R.string.pk_threads_num),16)));
        binding.tvUpdateSearchThreadsNum.setOnClickListener(v ->
                NumberPickerDialog.builder(settingActivity)
                        .setBindTextView(binding.tvThreadsNumber)
                        .setTitle(R.string.update_search_threads_num)
                        .setMinValue(1)
                        .setMaxValue(1024)
                        .setPreferenceKey(R.string.pk_threads_num,16)
                        .show(settingActivity.getRoot()));
        binding.tvThreadsNumber.setText(String.valueOf(pref.getInt("webPort",1122)));
        binding.tvWebPort.setOnClickListener(v ->
                NumberPickerDialog.builder(settingActivity)
                        .setBindTextView(binding.tvWebPortNumber)
                        .setTitle(R.string.web_port_title)
                        .setMinValue(1024)
                        .setMaxValue(60000)
                        .setPreferenceKey("webPort",1122)
                        .setAddedListener((oldValue, value) -> {
                            if (value != oldValue) {
                                WebService.upHttpServer(settingActivity);
                            }
                        })
                        .show(settingActivity.getRoot()));
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
                    }).show(settingActivity.getRoot()));
        binding.tvAboutRead.setOnClickListener(v -> AboutActivity.startThis(settingActivity));
    }

    private void bindSwitch() {
        binding.swDefaultRead.setPreferenceKey(R.string.pk_default_read,false);
        binding.swAutoUpdate.setPreferenceKey(R.string.pk_auto_refresh,false);
        binding.swAutoDownload.setPreferenceKey(R.string.pk_auto_download,false);
        binding.swUpChangeSourceLastChapter.setPreferenceKey(R.string.pk_change_source_update_chapter,false);
        binding.swDefaultPurify.setPreferenceKey(R.string.pk_default_purify,true);
        binding.swShowAllFind.setPreferenceKey(R.string.pk_show_all_find,true);
        binding.swProcessTextShowSearch.setPreferenceKey("process_text_show_search",true)
                .setAddedListener(ProcessTextHelp::setProcessTextEnable);
    }
}