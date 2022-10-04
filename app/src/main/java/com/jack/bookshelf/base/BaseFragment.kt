package com.jack.bookshelf.base

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.view.SupportMenuInflater
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseFragment(@LayoutRes layoutID: Int) : Fragment(layoutID),
    CoroutineScope by MainScope() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onMultiWindowModeChanged()
        onFragmentCreated(view, savedInstanceState)
        observeLiveBus()
    }

    abstract fun onFragmentCreated(view: View, savedInstanceState: Bundle?)

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean) {
        super.onMultiWindowModeChanged(isInMultiWindowMode)
        onMultiWindowModeChanged()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        onMultiWindowModeChanged()
    }

    private fun onMultiWindowModeChanged() {

    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    open fun observeLiveBus() {
    }

}
