@file:Suppress("DEPRECATION")

package com.jack.bookshelf.view.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceScreen

import com.jack.bookshelf.MApplication
import com.jack.bookshelf.R
import com.jack.bookshelf.help.BookshelfHelp
import com.jack.bookshelf.help.FileHelp
import com.jack.bookshelf.help.ProcessTextHelp
import com.jack.bookshelf.help.storage.BackupRestoreUi.selectBackupFolder
import com.jack.bookshelf.service.WebService
import com.jack.bookshelf.view.activity.AboutActivity
import com.jack.bookshelf.view.activity.SettingActivity
import com.jack.bookshelf.view.dialog.PaperAlertDialog

/**
 * Setting Fragment Deprecation
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

class SettingsFragment : PreferenceFragment(), OnSharedPreferenceChangeListener {

    @Deprecated("Deprecated in Java")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = preferenceManager.sharedPreferences
        upPreferenceSummary("backupPath", sharedPreferences.getString("backupPath", null))
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onDestroy() {
        super.onDestroy()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * 共享参数改变事件
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "backupPath" -> upPreferenceSummary(key, sharedPreferences.getString(key, null))
        }
    }

    /**
     * 更新共享参数
     */
    private fun upPreferenceSummary(preferenceKey: String, value: String?) {
        val preference = findPreference(preferenceKey) ?: return
        if (preference is ListPreference) {
            val index = preference.findIndexOfValue(value)
            // Set the summary to reflect the new value.
            preference.summary = if (index >= 0) preference.entries[index] else null
        } else {
            preference.summary = value
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith(
        "super.onActivityResult(requestCode, resultCode, data)",
        "android.preference.PreferenceFragment"
    )
    )
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}