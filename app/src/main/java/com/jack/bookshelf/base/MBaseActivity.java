package com.jack.bookshelf.base;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.jack.basemvplib.BaseActivity;
import com.jack.basemvplib.impl.IPresenter;
import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;
import com.jack.bookshelf.utils.ActivityExtensionsKt;
import com.jack.bookshelf.utils.SoftInputUtil;
import com.jack.bookshelf.utils.ToastsKt;

/**
 * MBase Activity
 * Copyright (c) 2017. 章钦豪. All rights reserved.
 * Edited by Jack251970
 */

public abstract class MBaseActivity<T extends IPresenter> extends BaseActivity<T> {
    public final SharedPreferences preferences = MApplication.getConfigPreferences();
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initTheme();
        super.onCreate(savedInstanceState);
        // disable auto fill
        getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        initImmersionBar();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void initTheme() {
        setTheme(R.style.CAppTheme);
    }

    /********************************************Bar***********************************************/

    protected void initImmersionBar() {
        try {
            View actionBar = findViewById(R.id.action_bar);
            ActivityExtensionsKt.fullScreen(this);
            boolean isShowActionBar = (actionBar != null) && (actionBar.getVisibility() == View.VISIBLE);
            ActivityExtensionsKt.setStatusBarColorAutoWhite(this, isShowActionBar , isShowActionBar);
        } catch (Exception ignored) {
        }
        try {
            ActivityExtensionsKt.setNavigationBarColorWhite(this);
        } catch (Exception ignored) {
        }
    }

    /******************************************Screen**********************************************/

    @SuppressLint("SourceLockedOrientationActivity")
    public void setOrientation(int screenDirection) {
        switch (screenDirection) {
            case 0:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case 1:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            default:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                break;
        }
    }

    /*****************************************SnackBar*********************************************/

    public void showSnackBar(View view, String msg, int length) {
        if (snackbar == null) {
            snackbar = Snackbar.make(view, msg, length);
        } else {
            snackbar.setText(msg);
            snackbar.setDuration(length);
        }
        snackbar.show();
    }

    public void hideSnackBar() {
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }

    /******************************************Toast***********************************************/

    public void toast(String msg) {
        ToastsKt.toast(this, msg, Toast.LENGTH_SHORT);
    }

    public void toast(String msg, int length) {
        ToastsKt.toast(this, msg, length);
    }

    public void toast(int strId) {
        toast(getString(strId));
    }

    public void toast(int strId, int length) {
        toast(getString(strId), length);
    }

    /*****************************************Activity*********************************************/

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
    }

    @Override
    public void finish() {
        SoftInputUtil.hideIMM(getCurrentFocus());
        super.finish();
    }
}