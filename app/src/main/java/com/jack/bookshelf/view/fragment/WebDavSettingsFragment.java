package com.jack.bookshelf.view.fragment;

import static com.jack.bookshelf.constant.AppConstant.DEFAULT_WEB_DAV_URL;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.widget.Toast;

import com.jack.bookshelf.R;
import com.jack.bookshelf.base.observer.MySingleObserver;
import com.jack.bookshelf.help.FileHelp;
import com.jack.bookshelf.help.ProcessTextHelp;
import com.jack.bookshelf.help.storage.BackupRestoreUi;
import com.jack.bookshelf.help.storage.WebDavHelp;
import com.jack.bookshelf.utils.ToastsKt;
import com.jack.bookshelf.view.activity.SettingActivity;

import java.util.ArrayList;
import java.util.Objects;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * WebDav Settings
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class WebDavSettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SettingActivity settingActivity;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("CONFIG");
        settingActivity = (SettingActivity) this.getActivity();
        settingActivity.setupActionBar("WebDav");
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean processTextEnabled = ProcessTextHelp.isProcessTextEnabled();
        editor.putBoolean("process_text", processTextEnabled);
        if (Objects.equals(sharedPreferences.getString("downloadPath", ""), "")) {
            editor.putString("downloadPath", FileHelp.getCachePath());
        }
        editor.apply();
        addPreferencesFromResource(R.xml.pref_settings_web_dav);
        bindPreferenceSummaryToValue(findPreference("web_dav_url"));
        bindPreferenceSummaryToValue(findPreference("web_dav_account"));
        bindPreferenceSummaryToValue(findPreference("web_dav_password"));
    }

    private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (Preference preference, Object value) -> {
        String stringValue = value.toString();

        if (preference.getKey().equals("web_dav_url")) {
            if (TextUtils.isEmpty(stringValue)) {
                preference.setSummary(DEFAULT_WEB_DAV_URL);
            } else {
                preference.setSummary(stringValue);
            }
        } else if (preference.getKey().equals("web_dav_account")) {
            if (TextUtils.isEmpty(stringValue)) {
                preference.setSummary("输入你的WebDav账号");
            } else {
                preference.setSummary(stringValue);
            }
        } else if (preference.getKey().equals("web_dav_password")) {
            if (TextUtils.isEmpty(stringValue)) {
                preference.setSummary("输入你的WebDav授权密码");
            } else {
                preference.setSummary("************");
            }
        } else if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            // Set the summary to reflect the new value.
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
        } else {
            // For all other preferences, set the summary to the value's
            preference.setSummary(stringValue);
        }
        return true;
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                preference.getContext().getSharedPreferences("CONFIG", Context.MODE_PRIVATE).getString(preference.getKey(), ""));
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals("web_dav_restore")) {
            restore();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void restore() {
        Single.create((SingleOnSubscribe<ArrayList<String>>) emitter -> {
            emitter.onSuccess(WebDavHelp.INSTANCE.getWebDavFileNames());
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MySingleObserver<ArrayList<String>>() {
                    @Override
                    public void onSuccess(ArrayList<String> strings) {
                        if (!WebDavHelp.INSTANCE.showRestoreDialog(getActivity(), strings, BackupRestoreUi.INSTANCE)) {
                            ToastsKt.toast(getActivity(),"没有备份",Toast.LENGTH_SHORT);
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}
