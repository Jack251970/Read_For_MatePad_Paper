package com.jack.bookshelf.help.storage

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.jack.bookshelf.DbHelper
import com.jack.bookshelf.MApplication
import com.jack.bookshelf.R
import com.jack.bookshelf.base.observer.MySingleObserver
import com.jack.bookshelf.help.BookshelfHelp
import com.jack.bookshelf.help.FileHelp
import com.jack.bookshelf.model.BookSourceManager
import com.jack.bookshelf.model.ReplaceRuleManager
import com.jack.bookshelf.model.TxtChapterRuleManager
import com.jack.bookshelf.utils.DocumentUtil
import com.jack.bookshelf.utils.FileUtils
import com.jack.bookshelf.utils.GSON
import com.jack.bookshelf.utils.StringUtils
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Backup Helper
 * Edited by Jack251970
 */

object Backup {
    val backupPath = MApplication.getInstance().filesDir.absolutePath + File.separator + "backup"

    val defaultPath by lazy {
        FileUtils.getSdCardPath() + File.separator + "Read"
    }

    val backupFileNames by lazy {
        arrayOf(
                "myBookShelf.json",
                "myBookSource.json",
                "myBookSearchHistory.json",
                "myBookReplaceRule.json",
                "myTxtChapterRule.json",
                "config.xml"
        )
    }

    /**
     * 自动本地备份
     */
    fun autoBack() {
        val lastBackup = MApplication.getConfigPreferences().getLong("lastBackup", 0)
        if (System.currentTimeMillis() - lastBackup < TimeUnit.DAYS.toMillis(1)) {
            return
        }
        val path = MApplication.getConfigPreferences().getString("backupPath", defaultPath)
        if (path == null) {
            backup(MApplication.getInstance(), defaultPath, null, true, BackupRestoreUi.backupRestoreLocal)
        } else {
            backup(MApplication.getInstance(), path, null, true, BackupRestoreUi.backupRestoreLocal)
        }
    }

    /**
     * 备份数据
     */
    fun backup(context: Context, path: String?, callBack: CallBack?, isAutoBackup: Boolean, backupWay: Int) {
        MApplication.getConfigPreferences().edit().putLong("lastBackup", System.currentTimeMillis()).apply()
        Single.create(SingleOnSubscribe<Boolean> { e ->
            if (backupWay == BackupRestoreUi.backupRestoreWebDav) {
                createBackupFile(context, FileHelp.getCachePath())
                if (!WebDavHelp.backUpWebDav()) {
                    return@SingleOnSubscribe
                }
            }
            if (backupWay == BackupRestoreUi.backupRestoreLocal) {
                createBackupFile(context, backupPath)
                if (path.isContentPath()) {
                    copyBackup(context, Uri.parse(path), isAutoBackup)
                } else {
                    path?.let { copyBackup(it, isAutoBackup) }
                }
            }
            e.onSuccess(true)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySingleObserver<Boolean>() {
                    override fun onSuccess(t: Boolean) {
                        callBack?.backupSuccess()
                    }
                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        callBack?.backupError(e.localizedMessage ?: StringUtils.getString(R.string.error))
                    }
                })
    }

    /**
     * 指定位置生成备份文件
     */
    private fun createBackupFile(context: Context, backupPath: String) {
        BookshelfHelp.getAllBook().let {
            if (it.isNotEmpty()) {
                val json = GSON.toJson(it)
                FileHelp.createFileIfNotExist(backupPath + File.separator + "myBookShelf.json").writeText(json)
            }
        }
        BookSourceManager.getAllBookSource().let {
            if (it.isNotEmpty()) {
                val json = GSON.toJson(it)
                FileHelp.createFileIfNotExist(backupPath + File.separator + "myBookSource.json").writeText(json)
            }
        }
        DbHelper.getDaoSession().searchHistoryBeanDao.queryBuilder().list().let {
            if (it.isNotEmpty()) {
                val json = GSON.toJson(it)
                FileHelp.createFileIfNotExist(backupPath + File.separator + "myBookSearchHistory.json")
                    .writeText(json)
            }
        }
        ReplaceRuleManager.getAll().blockingGet().let {
            if (it.isNotEmpty()) {
                val json = GSON.toJson(it)
                FileHelp.createFileIfNotExist(backupPath + File.separator + "myBookReplaceRule.json").writeText(json)
            }
        }
        TxtChapterRuleManager.getAll().let {
            if (it.isNotEmpty()) {
                val json = GSON.toJson(it)
                FileHelp.createFileIfNotExist(backupPath + File.separator + "myTxtChapterRule.json")
                    .writeText(json)
            }
        }
        Preferences.getSharedPreferences(context, backupPath, "config")?.let { sharedPreferences ->
            val edit = sharedPreferences.edit()
            MApplication.getConfigPreferences().all.map {
                when (val value = it.value) {
                    is Int -> edit.putInt(it.key, value)
                    is Boolean -> edit.putBoolean(it.key, value)
                    is Long -> edit.putLong(it.key, value)
                    is Float -> edit.putFloat(it.key, value)
                    is String -> edit.putString(it.key, value)
                    else -> Unit
                }
            }
            edit.commit()
        }
    }

    @Throws(Exception::class)
    private fun copyBackup(context: Context, uri: Uri, isAuto: Boolean) {
        synchronized(this) {
            DocumentFile.fromTreeUri(context, uri)?.let { treeDoc ->
                for (fileName in backupFileNames) {
                    val file = File(backupPath + File.separator + fileName)
                    if (file.exists()) {
                        if (isAuto) {
                            treeDoc.findFile("auto")?.findFile(fileName)?.delete()
                            var autoDoc = treeDoc.findFile("auto")
                            if (autoDoc == null) {
                                autoDoc = treeDoc.createDirectory("auto")
                            }
                            autoDoc?.createFile("", fileName)?.let {
                                DocumentUtil.writeBytes(context, file.readBytes(), it)
                            }
                        } else {
                            treeDoc.findFile(fileName)?.delete()
                            treeDoc.createFile("", fileName)?.let {
                                DocumentUtil.writeBytes(context, file.readBytes(), it)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 拷贝文件
     */
    @Throws(java.lang.Exception::class)
    private fun copyBackup(path: String, isAuto: Boolean) {
        synchronized(this) {
            for (fileName in backupFileNames) {
                if (isAuto) {
                    val file = File(backupPath + File.separator + fileName)
                    if (file.exists()) {
                        file.copyTo(FileHelp.createFileIfNotExist(path + File.separator + "auto" + File.separator + fileName), true)
                    }
                } else {
                    val file = File(backupPath + File.separator + fileName)
                    if (file.exists()) {
                        file.copyTo(FileHelp.createFileIfNotExist(path + File.separator + fileName), true)
                    }
                }
            }
        }
    }

    interface CallBack {
        fun backupSuccess()

        fun backupError(msg: String)
    }
}