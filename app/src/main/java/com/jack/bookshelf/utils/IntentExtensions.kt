@file:Suppress("unused")

package com.jack.bookshelf.utils

import android.content.Intent

fun Intent.putJson(key: String, any: Any?) {
    any?.let {
        putExtra(key, GSON.toJson(any))
    }
}

inline fun <reified T> Intent.getJsonObject(key: String): T? {
    val value = getStringExtra(key)
    return GSON.fromJsonObject<T>(value)
}

inline fun <reified T> Intent.getJsonArray(key: String): List<T>? {
    val value = getStringExtra(key)
    return GSON.fromJsonArray(value)
}