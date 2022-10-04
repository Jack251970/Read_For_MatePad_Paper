package com.jack.bookshelf.base

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.annotation.CallSuper
import com.jack.bookshelf.help.coroutine.Coroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

abstract class BaseService : Service(), CoroutineScope by MainScope() {

    fun <T> execute(
        scope: CoroutineScope = this,
        context: CoroutineContext = Dispatchers.IO,
        block: suspend CoroutineScope.() -> T
    ) = Coroutine.async(scope, context) { block() }

    @CallSuper
    override fun onCreate() {
        super.onCreate()
    }

    @CallSuper
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}