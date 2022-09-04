package com.jack.bookshelf.view.fragment;

import static com.jack.bookshelf.constant.AppConstant.DEFAULT_WEB_DAV_URL;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.TextUtils;

import com.jack.bookshelf.R;

/**
 * WebDav Settings
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

public class WebDavSettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("CONFIG");
        addPreferencesFromResource(R.xml.pref_settings_web_dav);
        bindPreferenceSummaryToValue(findPreference("web_dav_url"));
        bindPreferenceSummaryToValue(findPreference("web_dav_account"));
        bindPreferenceSummaryToValue(findPreference("web_dav_password"));
    }

    private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener
            = (Preference preference, Object value) -> {
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
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}