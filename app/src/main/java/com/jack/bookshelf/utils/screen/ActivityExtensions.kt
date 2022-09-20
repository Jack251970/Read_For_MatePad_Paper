@file:Suppress("unused")

package com.jack.bookshelf.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment

inline fun <reified T : DialogFragment> AppCompatActivity.showDialogFragment(
    arguments: Bundle.() -> Unit = {}
) {
    val dialog = T::class.java.newInstance()
    val bundle = Bundle()
    bundle.apply(arguments)
    dialog.arguments = bundle
    dialog.show(supportFragmentManager, T::class.simpleName)
}

fun AppCompatActivity.showDialogFragment(dialogFragment: DialogFragment) {
    dialogFragment.show(supportFragmentManager, dialogFragment::class.simpleName)
}

val Activity.windowSize: DisplayMetrics
    get() {
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            displayMetrics.widthPixels = windowMetrics.bounds.width() - insets.left - insets.right
            displayMetrics.heightPixels = windowMetrics.bounds.height() - insets.top - insets.bottom
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        return displayMetrics
    }

fun Activity.fullScreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.setDecorFitsSystemWindows(true)
    }
    @Suppress("DEPRECATION")
    window.decorView.systemUiVisibility =
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    @Suppress("DEPRECATION")
    window.clearFlags(
        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
    )
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
}

/**
 * 设置状态栏颜色白色
 */
fun Activity.setStatusBarColorWhite(
    isTransparent: Boolean,
) {
    if (isTransparent) {
        window.statusBarColor = Color.TRANSPARENT
    }
    window.statusBarColor = Color.WHITE
    setLightStatusBar(true)
}

/**
 * 设置状态栏图标深浅
 */
@Suppress("DEPRECATION")
fun Activity.setLightStatusBar(isLightBar: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.let {
            if (isLightBar) {
                it.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                it.setSystemBarsAppearance(
                    0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
        }
    }
    val decorView = window.decorView
    val systemUiVisibility = decorView.systemUiVisibility
    if (isLightBar) {
        decorView.systemUiVisibility =
            systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    } else {
        decorView.systemUiVisibility =
            systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
    }
}

/**
 * 设置导航栏白色
 */
@Suppress("DEPRECATION")
fun Activity.setNavigationBarColorWhite() {
    window.navigationBarColor = Color.WHITE
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.setSystemBarsAppearance(
            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
        )
    }
    val decorView = window.decorView
    val systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    decorView.systemUiVisibility = systemUiVisibility
}