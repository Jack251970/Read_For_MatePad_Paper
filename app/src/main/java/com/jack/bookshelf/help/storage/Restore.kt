package com.jack.bookshelf.help.storage

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.jack.bookshelf.DbHelper
import com.jack.bookshelf.MApplication
import com.jack.bookshelf.R
import com.jack.bookshelf.base.observer.MySingleObserver
import com.jack.bookshelf.bean.*
import com.jack.bookshelf.help.FileHelp
import com.jack.bookshelf.help.ReadBookControl
import com.jack.bookshelf.model.BookSourceManager
import com.jack.bookshelf.model.ReplaceRuleManager
import com.jack.bookshelf.model.TxtChapterRuleManager
import com.jack.bookshelf.utils.DocumentUtil
import com.jack.bookshelf.utils.GSON
import com.jack.bookshelf.utils.StringUtils
import com.jack.bookshelf.utils.fromJsonArray
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File

/**
 * Restore Helper
 * Edited by Jack251970
 */

object Restore {

    /**
     * 恢复数据
     */
    fun restore(context: Context, uri: Uri, callBack: CallBack?) {
        Single.create(SingleOnSubscribe<Boolean> { e ->
            DocumentFile.fromTreeUri(context, uri)?.listFiles()?.forEach { doc ->
                for (fileName in Backup.backupFileNames) {
                    if (doc.name == fileName) {
                        DocumentUtil.readBytes(context, doc.uri)?.let {
                            FileHelp.createFileIfNotExist(Backup.backupPath + File.separator + fileName)
                                    .writeBytes(it)
                        }
                    }
                }
            }
            e.onSuccess(true)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySingleObserver<Boolean>() {
                    override fun onSuccess(t: Boolean) {
                        restore(Backup.backupPath, callBack)
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        callBack?.restoreError(e.localizedMessage ?: StringUtils.getString(R.string.error))
                    }
                })
    }

    /**
     * 恢复数据
     */
    fun restore(path: String, callBack: CallBack?) {
        Single.create(SingleOnSubscribe<Boolean> { e ->
            try {
                val file = FileHelp.createFileIfNotExist(path + File.separator + "myBookShelf.json")
                val json = file.readText()
                GSON.fromJsonArray<BookShelfBean>(json)?.forEach { bookshelf ->
                    if (bookshelf.noteUrl != null) {
                        DbHelper.getDaoSession().bookShelfBeanDao.insertOrReplace(bookshelf)
                    }
                    if (bookshelf.bookInfoBean.noteUrl != null) {
                        DbHelper.getDaoSession().bookInfoBeanDao.insertOrReplace(bookshelf.bookInfoBean)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val file = FileHelp.createFileIfNotExist(path + File.separator + "myBookSource.json")
                val json = file.readText()
                GSON.fromJsonArray<BookSourceBean>(json)?.let {
                    BookSourceManager.addBookSource(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val file = FileHelp.createFileIfNotExist(path + File.separator + "myBookSearchHistory.json")
                val json = file.readText()
                GSON.fromJsonArray<SearchHistoryBean>(json)?.let {
                    DbHelper.getDaoSession().searchHistoryBeanDao.insertOrReplaceInTx(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val file = FileHelp.createFileIfNotExist(path + File.separator + "myBookReplaceRule.json")
                val json = file.readText()
                GSON.fromJsonArray<ReplaceRuleBean>(json)?.let {
                    ReplaceRuleManager.addDataS(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val file = FileHelp.createFileIfNotExist(path + File.separator + "myTxtChapterRule.json")
                val json = file.readText()
                GSON.fromJsonArray<TxtChapterRuleBean>(json)?.let {
                    TxtChapterRuleManager.save(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Preferences.getSharedPreferences(MApplication.getInstance(), path, "config")?.all?.map {
                val edit = MApplication.getConfigPreferences().edit()
                when (val value = it.value) {
                    is Int -> edit.putInt(it.key, value)
                    is Boolean -> edit.putBoolean(it.key, value)
                    is Long -> edit.putLong(it.key, value)
                    is Float -> edit.putFloat(it.key, value)
                    is String -> edit.putString(it.key, value)
                    else -> Unit
                }
                edit.putLong("versionCode", MApplication.getVersionCode())
                edit.apply()
            }
            e.onSuccess(true)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySingleObserver<Boolean>() {
                    override fun onSuccess(t: Boolean) {
                        ReadBookControl.getInstance().updateReaderSettings()
                        callBack?.restoreSuccess()
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        callBack?.restoreError(e.localizedMessage ?: StringUtils.getString(R.string.error))
                    }
                })
    }

    interface CallBack {
        fun restoreSuccess()
        fun restoreError(msg: String)
    }
}