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
import com.jack.bookshelf.view.activity.SettingActivity
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.okButton

/**
 * 设置界面的Fragment
 * Created by GKF on 2017/12/16.
 * Edited by Jack Ye
 */

@Suppress("DEPRECATION")
class SettingsFragment : PreferenceFragment(), OnSharedPreferenceChangeListener {
    private var settingActivity: SettingActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferencesName = "CONFIG"
        settingActivity = activity as? SettingActivity
        settingActivity?.setupActionBar(getString(R.string.setting))
        addPreferencesFromResource(R.xml.pref_settings)
        val sharedPreferences = preferenceManager.sharedPreferences
        val editor = sharedPreferences.edit()
        val processTextEnabled = ProcessTextHelp.isProcessTextEnabled()
        editor.putBoolean("process_text", processTextEnabled)
        if (sharedPreferences.getString("downloadPath", "") == "") {
            editor.putString("downloadPath", FileHelp.getCachePath())
        }
        editor.apply()
        upPreferenceSummary("downloadPath", MApplication.downloadPath)
        upPreferenceSummary("backupPath", sharedPreferences.getString("backupPath", null))
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        settingActivity?.initImmersionBar()
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * 共享参数改变事件
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "process_text" -> ProcessTextHelp.setProcessTextEnable(sharedPreferences.getBoolean("process_text", true))
            "webPort" -> WebService.upHttpServer(activity)
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

    /**
     * 按钮点击事件
     */
    override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen, preference: Preference): Boolean {
        when (preference.key) {
            "backupPath" -> {
                selectBackupFolder(activity)
            }
            "webDavSetting" -> {
                val webDavSettingsFragment = WebDavSettingsFragment()
                fragmentManager.beginTransaction().replace(R.id.settingsFrameLayout, webDavSettingsFragment, "webDavSettings").commit()
            }
            "clearCache" -> {
                alert {
                    titleResource = R.string.pt_clear_cache
                    messageResource = R.string.sure_del_download_book
                    okButton {
                        BookshelfHelp.clearCaches(true)
                    }
                    noButton {
                        BookshelfHelp.clearCaches(false)
                    }
                }.show()
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
    }

}