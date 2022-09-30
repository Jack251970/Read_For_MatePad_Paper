package com.jack.bookshelf.help.permission

import android.app.Activity
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

/**
 * Permission Compat
 * Edited by Jack251970
 */

class PermissionsCompat private constructor() {
    private var request: Request? = null

    fun request() {
        RequestManager.pushRequest(request)
    }

    class Builder {
        private val request: Request

        constructor(activity: Activity, mainView: View) {
            request = Request(activity, mainView)
        }

        constructor(fragment: Fragment, mainView: View) {
            request = Request(fragment, mainView)
        }

        fun addPermissions(vararg permissions: String): Builder {
            request.addPermissions(*permissions)
            return this
        }

        fun requestCode(requestCode: Int): Builder {
            request.setRequestCode(requestCode)
            return this
        }

        fun onGranted(callback: (requestCode: Int) -> Unit): Builder {
            request.setOnGrantedCallback(object : OnPermissionsGrantedCallback {
                override fun onPermissionsGranted(requestCode: Int) {
                    callback(requestCode)
                }
            })
            return this
        }

        fun onDenied(callback: (requestCode: Int, deniedPermissions: Array<String>) -> Unit): Builder {
            request.setOnDeniedCallback(object : OnPermissionsDeniedCallback {
                override fun onPermissionsDenied(requestCode: Int, deniedPermissions: Array<String>) {
                    callback(requestCode, deniedPermissions)
                }
            })
            return this
        }

        fun rationale(rationale: CharSequence): Builder {
            request.setRationale(rationale)
            return this
        }

        fun rationale(@StringRes resId: Int): Builder {
            request.setRationale(resId)
            return this
        }

        fun build(): PermissionsCompat {
            val compat = PermissionsCompat()
            compat.request = request
            return compat
        }

        fun request(): PermissionsCompat {
            val compat = build()
            compat.request = request
            compat.request()
            return compat
        }
    }
}