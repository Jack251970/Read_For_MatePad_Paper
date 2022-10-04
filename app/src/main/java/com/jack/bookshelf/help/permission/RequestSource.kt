package com.jack.bookshelf.help.permission

import android.content.Context
import android.content.Intent

interface RequestSource {

    val context: Context?

    fun startActivity(intent: Intent)
}
