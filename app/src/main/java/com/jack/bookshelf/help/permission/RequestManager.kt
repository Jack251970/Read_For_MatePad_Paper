package com.jack.bookshelf.help.permission

import android.os.Handler
import android.os.Looper
import java.util.*

internal object RequestManager : OnPermissionsResultCallback {

    private var requests: Stack<Request>? = null
    private var request: Request? = null

    private val handler = Handler(Looper.getMainLooper())

    private val requestRunnable = Runnable {
        request?.start()
    }

    private val isCurrentRequestInvalid: Boolean
        get() = request?.let { System.currentTimeMillis() - it.requestTime > 5 * 1000L } ?: true

    init {
        RequestPlugins.setOnPermissionsResultCallback(this)
    }

    fun pushRequest(request: Request?) {
        if (request == null) return

        if (requests == null) {
            requests = Stack()
        }

        requests?.let {
            val index = it.indexOf(request)
            if (index >= 0) {
                val to = it.size - 1
                if (index != to) {
                    Collections.swap(requests, index, to)
                }
            } else {
                it.push(request)
            }

            if (!it.empty() && isCurrentRequestInvalid) {
                this.request = it.pop()
                handler.post(requestRunnable)
            }
        }
    }

    private fun startNextRequest() {
        request?.clear()
        request = null

        requests?.let {
            request = if (it.empty()) null else it.pop()
            request?.let { handler.post(requestRunnable) }
        }
    }

    override fun onPermissionsGranted(requestCode: Int) {
        startNextRequest()
    }

    override fun onPermissionsDenied(requestCode: Int, deniedPermissions: Array<String>) {
        startNextRequest()
    }

}
