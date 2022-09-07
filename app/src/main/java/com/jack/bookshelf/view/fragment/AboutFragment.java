package com.jack.bookshelf.view.fragment;

import android.content.Intent;
import android.net.Uri;
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
import com.jack.bookshelf.databinding.FragmentAboutBinding;
import com.jack.bookshelf.view.activity.SettingActivity;

/**
 * About Fragment
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class AboutFragment extends Fragment {

    private FragmentAboutBinding binding;
    private SettingActivity settingActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingActivity = (SettingActivity) getActivity();
        assert settingActivity != null;
        settingActivity.setTile(R.string.about_read);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAboutBinding.inflate(inflater, container, false);
        bindView();
        return binding.getRoot();
    }

    private void bindView() {
        binding.tvVersion.setText(getString(R.string.version_name, MApplication.getVersionName()));
        binding.tvUpdate.setOnClickListener(view -> openIntent(getString(R.string.latest_release_url)));
        binding.tvUpdateLog.setOnClickListener(view -> settingActivity.getMoDialogHUD().showAssetMarkdown("updateLog.md"));
        binding.tvGithub.setOnClickListener(view -> openIntent(getString(R.string.this_github_url)));
        binding.tvDisclaimer.setOnClickListener(view -> settingActivity.getMoDialogHUD().showAssetMarkdown("disclaimer.md"));
    }

    /**
     * 打开外部网址
     */
    private void openIntent(String address) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(address));
            startActivity(intent);
        } catch (Exception e) {
            settingActivity.toast(R.string.can_not_open, Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}