package com.jack.bookshelf.help.storage

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import com.jack.bookshelf.MApplication
import com.jack.bookshelf.R
import com.jack.bookshelf.base.observer.MySingleObserver
import com.jack.bookshelf.constant.AppConstant
import com.jack.bookshelf.help.FileHelp
import com.jack.bookshelf.utils.StringUtils
import com.jack.bookshelf.utils.StringUtils.getString
import com.jack.bookshelf.utils.ZipUtils
import com.jack.bookshelf.utils.toastOnUi
import com.jack.bookshelf.utils.webdav.WebDav
import com.jack.bookshelf.utils.webdav.http.HttpAuth
import com.jack.bookshelf.widget.menu.SelectMenu
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

/**
 * WebDav Helper
 * Edited by Jack251970
 */

object WebDavHelp {
    private val zipFilePath = FileHelp.getCachePath() + "/backup" + ".zip"

    private val unzipFilesPath by lazy {
        FileHelp.getCachePath()
    }

    /**
     * 获取WebDav服务器地址
     */
    private fun getWebDavUrl(): String {
        var url = MApplication.getConfigPreferences().getString("web_dav_url", AppConstant.DEFAULT_WEB_DAV_URL)
        if (url.isNullOrEmpty()) {
            url = AppConstant.DEFAULT_WEB_DAV_URL
        }
        if (!url.endsWith("/")) url += "/"
        return url
    }


    /**
     * 初始化WebDav账号
     */
    private fun initWebDav(): Boolean {
        val account = MApplication.getConfigPreferences().getString("web_dav_account", "")
        val password = MApplication.getConfigPreferences().getString("web_dav_password", "")
        if (!account.isNullOrBlank() && !password.isNullOrBlank()) {
            HttpAuth.auth = HttpAuth.Auth(account, password)
            return true
        }
        return false
    }

    /**
     * 获取WebDav备份文件名字
     */
    fun getWebDavFileNames(): ArrayList<String> {
        val url = getWebDavUrl()
        val names = arrayListOf<String>()
        try {
            if (initWebDav()) {
                var files = WebDav(url + "Read/").listFiles()
                files = files.reversed()
                for (index: Int in 0 until min(10, files.size)) {
                    files[index].displayName?.let {
                        names.add(it)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return names
    }

    /**
     * 选择WebDav备份文件
     */
    fun showRestoreDialog(context: Context, mainView: View, names: ArrayList<String>, callBack: Restore.CallBack?): Boolean {
        return if (names.isNotEmpty()) {
            SelectMenu.builder(context)
                .setTitle(getString(R.string.choose_restore_file))
                .setBottomButton(getString(R.string.cancel))
                .setMenu(names)
                .setListener(object : SelectMenu.OnItemClickListener {
                    override fun forBottomButton() {}
                    override fun forListItem(lastChoose: Int, position: Int) {
                        restoreWebDav(names[position], callBack)
                    }
                }).show(mainView)
            /*context.selector(title = getString(R.string.choose_restore_file), items = names) { _, index ->
                if (index in 0 until names.size) {
                    restoreWebDav(names[index], callBack)
                }
            }*/
            true
        } else {
            false
        }
    }

    /**
     * 恢复指定文件
     */
    private fun restoreWebDav(name: String, callBack: Restore.CallBack?) {
        Single.create(SingleOnSubscribe<Boolean> { e ->
            getWebDavUrl().let {
                val file = WebDav(it + "Read/" + name)
                file.downloadTo(zipFilePath, true)
                @Suppress("BlockingMethodInNonBlockingContext")
                ZipUtils.unzipFile(zipFilePath, unzipFilesPath)
            }
            e.onSuccess(true)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySingleObserver<Boolean>() {
                    override fun onSuccess(t: Boolean) {
                        Restore.restore(unzipFilesPath, callBack)
                    }
                })
    }

    /**
     * WebDav数据备份
     */
    fun backUpWebDav(): Boolean {
        try {
            if (initWebDav()) {
                val paths = arrayListOf(*Backup.backupFileNames)
                for (i in 0 until paths.size) {
                    paths[i] = FileHelp.getCachePath() + File.separator + paths[i]
                }
                FileHelp.deleteFile(zipFilePath)
                if (ZipUtils.zipFiles(paths, zipFilePath)) {
                    WebDav(getWebDavUrl() + "Read").makeAsDir()
                    val putUrl = getWebDavUrl() + "Read/backup" + SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis())) + ".zip"
                    WebDav(putUrl).upload(zipFilePath)
                }
                return true
            } else {
                MApplication.getInstance().toastOnUi(StringUtils.getString(R.string.backup_fail))
            }
        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                MApplication.getInstance().toastOnUi("WebDav\n${e.localizedMessage}")
            }
        }
        return false
    }
}