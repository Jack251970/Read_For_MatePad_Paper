package com.jack.bookshelf.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jack.bookshelf.R;
import com.jack.bookshelf.databinding.FragmentLocalBackupSettingBinding;
import com.jack.bookshelf.help.storage.BackupRestoreUi;
import com.jack.bookshelf.view.activity.SettingActivity;

/**
 * Local Backup Setting Fragment
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class LocalBackupSettingFragment extends Fragment {
    private FragmentLocalBackupSettingBinding binding;
    private SettingActivity settingActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingActivity = (SettingActivity) getActivity();
        assert settingActivity != null;
        settingActivity.setTile(R.string.local_backup_setting);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLocalBackupSettingBinding.inflate(inflater, container, false);
        bindView();
        return binding.getRoot();
    }

    private void bindView() {
        binding.tvBackPath.setOnClickListener(v -> selectBackPath());
        binding.swAutoBackup.setPreferenceKey(R.string.pk_auto_backup, true);
        binding.tvLocalBackup.setOnClickListener(v -> backup());
        binding.tvLocalRestore.setOnClickListener(v -> restore());
    }

    private void selectBackPath() {
        BackupRestoreUi.INSTANCE.selectBackupFolder(settingActivity, settingActivity.getRoot(), false);
    }

    private void backup() {
        BackupRestoreUi.INSTANCE.backup(settingActivity, settingActivity.getRoot(), BackupRestoreUi.backupRestoreLocal);
    }

    private void restore() {
        BackupRestoreUi.INSTANCE.restore(settingActivity, settingActivity.getRoot(), BackupRestoreUi.backupRestoreLocal);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}