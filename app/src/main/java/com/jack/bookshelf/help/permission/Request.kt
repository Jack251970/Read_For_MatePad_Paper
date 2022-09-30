package com.jack.bookshelf.help.permission

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.jack.bookshelf.R
import com.jack.bookshelf.widget.dialog.PaperAlertDialog
import org.jetbrains.anko.startActivity
import java.util.*

/**
 * Permission Request
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

internal class Request : OnRequestPermissionsResultCallback {
    internal val requestTime: Long
    private var requestCode: Int = TYPE_REQUEST_PERMISSION
    private var source: RequestSource? = null
    private var permissions: ArrayList<String>? = null
    private var grantedCallback: OnPermissionsGrantedCallback? = null
    private var deniedCallback: OnPermissionsDeniedCallback? = null
    private var rationaleResId: Int = 0
    private var rationale: CharSequence? = null
    private var rationaleDialog: PaperAlertDialog? = null
    private val mainView: View

    private val deniedPermissions: Array<String>?
        get() {
            return getDeniedPermissions(this.permissions?.toTypedArray())
        }

    constructor(activity: Activity, mainView: View) {
        source = ActivitySource(activity)
        permissions = ArrayList()
        requestTime = System.currentTimeMillis()
        this.mainView = mainView
    }

    constructor(fragment: Fragment, mainView: View) {
        source = FragmentSource(fragment)
        permissions = ArrayList()
        requestTime = System.currentTimeMillis()
        this.mainView = mainView
    }

    fun addPermissions(vararg permissions: String) {
        this.permissions?.addAll(listOf(*permissions))
    }

    fun setRequestCode(requestCode: Int) {
        this.requestCode = requestCode
    }

    fun setOnGrantedCallback(callback: OnPermissionsGrantedCallback) {
        grantedCallback = callback
    }

    fun setOnDeniedCallback(callback: OnPermissionsDeniedCallback) {
        deniedCallback = callback
    }

    fun setRationale(@StringRes resId: Int) {
        rationaleResId = resId
        rationale = null
    }

    fun setRationale(rationale: CharSequence) {
        this.rationale = rationale
        rationaleResId = 0
    }

    fun start() {
        RequestPlugins.setOnRequestPermissionsCallback(this)

        val deniedPermissions = deniedPermissions

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (deniedPermissions == null) {
                onPermissionsGranted(requestCode)
            } else {
                val rationale = if (rationaleResId != 0) source?.context?.getText(rationaleResId) else rationale
                if (rationale != null) {
                    showSettingDialog(rationale) { onPermissionsDenied(requestCode, deniedPermissions) }
                } else {
                    onPermissionsDenied(requestCode, deniedPermissions)
                }
            }
        } else {
            if (deniedPermissions != null) {
                source?.context?.startActivity<PermissionActivity>(
                        Pair(PermissionActivity.KEY_INPUT_REQUEST_TYPE, TYPE_REQUEST_PERMISSION),
                        Pair(PermissionActivity.KEY_INPUT_PERMISSIONS_CODE, requestCode),
                        Pair(PermissionActivity.KEY_INPUT_PERMISSIONS, deniedPermissions)
                )
            } else {
                onPermissionsGranted(requestCode)
            }
        }
    }

    fun clear() {
        grantedCallback = null
        deniedCallback = null
    }

    private fun getDeniedPermissions(permissions: Array<String>?): Array<String>? {
        if (permissions != null) {
            val deniedPermissionList = ArrayList<String>()
            for (permission in permissions) {
                if (source?.context?.let {
                            ContextCompat.checkSelfPermission(
                                    it,
                                    permission
                            )
                        } != PackageManager.PERMISSION_GRANTED
                ) {
                    deniedPermissionList.add(permission)
                }
            }
            val size = deniedPermissionList.size
            if (size > 0) {
                return deniedPermissionList.toTypedArray()
            }
        }
        return null
    }

    private fun showSettingDialog(rationale: CharSequence, cancel: () -> Unit) {
        rationaleDialog?.dismiss()
        source?.context?.let {
            runCatching {
                rationaleDialog = PaperAlertDialog(it)
                    .setTitle(R.string.dialog_title)
                    .setMessage(rationale)
                    .setNegativeButton(R.string.cancel)
                    .setPositiveButton(R.string.dialog_setting)
                    .setOnclick(object : PaperAlertDialog.OnItemClickListener {
                        override fun forNegativeButton() {
                            cancel()
                        }
                        override fun forPositiveButton() {
                            it.startActivity<PermissionActivity>(
                                Pair(
                                    PermissionActivity.KEY_INPUT_REQUEST_TYPE,
                                    TYPE_REQUEST_SETTING
                                )
                            )
                        }
                    })
            }
        }
        rationaleDialog?.show(mainView)
    }

    private fun onPermissionsGranted(requestCode: Int) {
        try {
            grantedCallback?.onPermissionsGranted(requestCode)
        } catch (ignore: Exception) {
        }

        RequestPlugins.sResultCallback?.onPermissionsGranted(requestCode)
    }

    private fun onPermissionsDenied(requestCode: Int, deniedPermissions: Array<String>) {
        try {
            deniedCallback?.onPermissionsDenied(requestCode, deniedPermissions)
        } catch (ignore: Exception) {
        }

        RequestPlugins.sResultCallback?.onPermissionsDenied(requestCode, deniedPermissions)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        val deniedPermissions = getDeniedPermissions(permissions)
        if (deniedPermissions != null) {
            val rationale = if (rationaleResId != 0) source?.context?.getText(rationaleResId) else rationale
            if (rationale != null) {
                showSettingDialog(rationale) { onPermissionsDenied(requestCode, deniedPermissions) }
            } else {
                onPermissionsDenied(requestCode, deniedPermissions)
            }
        } else {
            onPermissionsGranted(requestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val deniedPermissions = deniedPermissions
        if (deniedPermissions == null) {
            onPermissionsGranted(this.requestCode)
        } else {
            onPermissionsDenied(this.requestCode, deniedPermissions)
        }
    }

    companion object {
        const val TYPE_REQUEST_PERMISSION = 1
        const val TYPE_REQUEST_SETTING = 2
    }
}