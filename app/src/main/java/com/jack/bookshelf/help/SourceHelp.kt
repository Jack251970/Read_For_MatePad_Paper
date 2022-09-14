package com.jack.bookshelf.help

import android.os.Handler
import android.os.Looper
import com.jack.bookshelf.MApplication
import com.jack.bookshelf.R
import com.jack.bookshelf.bean.BookSourceBean
import com.jack.bookshelf.model.BookSourceManager
import com.jack.bookshelf.utils.EncoderUtils
import com.jack.bookshelf.utils.StringUtils.getString
import com.jack.bookshelf.utils.splitNotBlank
import com.jack.bookshelf.utils.toastOnUi
import org.jetbrains.anko.toast

/**
 * Source Helper
 * Edited by Jack251970
 */

object SourceHelp {

    private val handler = Handler(Looper.getMainLooper())
    private val list18Plus by lazy {
        try {
            return@lazy String(MApplication.getInstance().assets.open("18PlusList.txt").readBytes())
                    .splitNotBlank("\n")
        } catch (e: Exception) {
            return@lazy arrayOf<String>()
        }
    }

    fun insertBookSource(vararg bookSources: BookSourceBean) {
        bookSources.forEach { bookSource ->
            if (is18Plus(bookSource.bookSourceUrl)) {
                handler.post {
                    MApplication.getInstance().toastOnUi(getString(R.string.website_illegal_cannot_import, bookSource.bookSourceName + ""))
                }
            } else {
                BookSourceManager.addBookSource(bookSource)
            }
        }
    }

    private fun is18Plus(url: String?): Boolean {
        url ?: return false
        val baseUrl = getBaseUrl(url)
        baseUrl ?: return false
        try {
            val host = baseUrl.split("//", ".")
            val base64Url = EncoderUtils.base64Encode("${host[host.lastIndex - 1]}.${host.last()}")
            list18Plus.forEach {
                if (base64Url == it) {
                    return true
                }
            }
        } catch (e: Exception) {
        }
        return false
    }

    fun getBaseUrl(url: String?): String? {
        if (url == null || !url.startsWith("http")) return null
        val index = url.indexOf("/", 9)
        return if (index == -1) {
            url
        } else url.substring(0, index)
    }

}