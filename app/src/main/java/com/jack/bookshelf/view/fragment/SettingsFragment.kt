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
import com.jack.bookshelf.view.dialog.AlertDialog

/**
 * Setting Fragment
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

@Suppress("DEPRECATION")
class SettingsFragment : PreferenceFragment(), OnSharedPreferenceChangeListener {
    private var settingActivity: SettingActivity? = null

    @Deprecated("Deprecated in Java")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferencesName = "CONFIG"
        settingActivity = activity as? SettingActivity
        settingActivity?.setTile(R.string.setting)
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
    @Deprecated("Deprecated in Java")
    override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen, preference: Preference): Boolean {
        when (preference.key) {
            "webDavSetting" -> {
                val webDavSettingsFragment = WebDavSettingsFragment()
                fragmentManager.beginTransaction().replace(R.id.settingsFrameLayout, webDavSettingsFragment, "webDavSettings").commit()
            }
            "backupPath" -> {
                settingActivity?.let { selectBackupFolder(activity, it.root) }
            }
            "clearCache" -> {
                AlertDialog.builder(activity, settingActivity!!.root, AlertDialog.NO_TITLE)
                    .setMessage(R.string.sure_delete_download_book)
                    .setNegativeButton(R.string.cancel)
                    .setPositiveButton(R.string.delete)
                    .setOnclick(object : AlertDialog.OnItemClickListener {
                        override fun forNegativeButton() {
                            BookshelfHelp.clearCaches(false)
                        }
                        override fun forPositiveButton() {
                            BookshelfHelp.clearCaches(true)
                        }
                    }).show()
            }
            "aboutRead" -> {
                AboutActivity.startThis(activity)
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference)
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