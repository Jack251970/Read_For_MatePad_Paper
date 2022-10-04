package com.jack.bookshelf.constant

import android.annotation.SuppressLint
import android.provider.Settings
import splitties.init.appCtx

@SuppressLint("SimpleDateFormat")
object AppConst {

    val androidId: String by lazy {
        Settings.System.getString(appCtx.contentResolver, Settings.Secure.ANDROID_ID)
    }

    val charsets = arrayListOf("UTF-8", "GB2312", "GB18030", "GBK", "Unicode", "UTF-16", "UTF-16LE", "ASCII")
}
