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
import com.jack.bookshelf.help.update.UpdateManager;
import com.jack.bookshelf.service.update.UpdateService;
import com.jack.bookshelf.view.activity.SettingActivity;
import com.jack.bookshelf.widget.dialog.PaperAlertDialog;

/**
 * About Fragment
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class AboutFragment extends Fragment {
    private FragmentAboutBinding binding;
    private SettingActivity settingActivity;
    private PaperAlertDialog alertDialog;

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
        initDialog();
        binding.tvVersion.setText(getString(R.string.version_name, MApplication.getVersionName()));
        binding.tvUpdate.setOnClickListener(v -> checkUpdate());
        binding.tvUpdateLog.setOnClickListener(v -> settingActivity.getMoDialogHUD().showAssetMarkdown("updateLog.md"));
        binding.tvGithub.setOnClickListener(v -> openIntent(getString(R.string.this_github_url)));
        binding.tvDisclaimer.setOnClickListener(v -> settingActivity.getMoDialogHUD().showAssetMarkdown("disclaimer.md"));
    }

    private void initDialog() {
        alertDialog = new PaperAlertDialog(settingActivity)
                .setTitle(R.string.update)
                .setType(PaperAlertDialog.WITH_PROGRESS_BAR)
                .setProgressMax(100)
                .setNegativeButton(R.string.cancel)
                .setPositiveButton(R.string.confirm)
                .setOnclick(new PaperAlertDialog.OnItemClickListener() {
                    @Override
                    public void forNegativeButton() {
                        UpdateService.getInstance().cancelDownload();
                    }

                    @Override
                    public void forPositiveButton() {

                    }
                });
    }

    /**
     * 检查更新
     */
    private void checkUpdate() {
        UpdateManager.getInstance(settingActivity).checkUpdate(settingActivity, settingActivity.getRoot(),
                true, new UpdateManager.CallBack() {
            @Override
            public void setProgress(int progress) {
                alertDialog.setProgress(progress);
            }

            @Override
            public void showDialog() {
                alertDialog.setProgress(0);
                alertDialog.show(settingActivity.getRoot());
            }

            @Override
            public void dismissDialog() {
                alertDialog.setProgress(0);
                if (alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
            }
        });
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