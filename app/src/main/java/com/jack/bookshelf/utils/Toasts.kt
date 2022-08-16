@file:Suppress("unused")

package com.jack.bookshelf.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jack.bookshelf.MApplication
import com.jack.bookshelf.R

/**
 * Toast Util
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

private var toast: Toast? = null
private val inflater = LayoutInflater.from(MApplication.getInstance())

fun toast(context: Context?, msg: String?, length: Int) {
    val view = inflater.inflate(com.monke.basemvplib.R.layout.dialog_toast, null)
    if (toast == null) {
        toast = Toast(context)
    }
    (view.findViewById<View>(com.monke.basemvplib.R.id.mpp_tv_toast) as TextView).text = msg
    toast!!.view = view
    toast!!.duration = length
    toast?.show()
}

fun toast(context: Context?, strId: Int, length: Int) {
    toast(context, StringUtils.getString(strId), length)
}

fun Context.toastOnUi(message: Int) {
    runOnUI {
        val view: View = inflater.inflate(com.monke.basemvplib.R.layout.dialog_toast, null)
        if (toast == null) {
            toast = Toast(this)
        }
        (view.findViewById<View>(R.id.mpp_tv_toast) as TextView).text = StringUtils.getString(message)
        toast!!.view = view
        toast!!.duration = Toast.LENGTH_SHORT
        toast?.show()
    }
}

fun Context.toastOnUi(message: CharSequence?) {
    runOnUI {
        val view: View = inflater.inflate(com.monke.basemvplib.R.layout.dialog_toast, null)
        if (toast == null) {
            toast = Toast(this)
        }
        (view.findViewById<View>(R.id.mpp_tv_toast) as TextView).text = message
        toast!!.view = view
        toast!!.duration = Toast.LENGTH_SHORT
        toast?.show()
    }
}

fun Context.longToastOnUi(message: Int) {
    runOnUI {
        val view: View = inflater.inflate(com.monke.basemvplib.R.layout.dialog_toast, null)
        if (toast == null) {
            toast = Toast(this)
        }
        (view.findViewById<View>(R.id.mpp_tv_toast) as TextView).text = StringUtils.getString(message)
        toast!!.view = view
        toast!!.duration = Toast.LENGTH_LONG
        toast?.show()
    }
}

fun Context.longToastOnUi(message: CharSequence?) {
    runOnUI {
        if (toast == null) {
            val view: View = inflater.inflate(com.monke.basemvplib.R.layout.dialog_toast, null)
            toast = Toast(this)
            (view.findViewById<View>(R.id.mpp_tv_toast) as TextView).text = message
            toast!!.view = view
            toast!!.duration = Toast.LENGTH_LONG
        } else {
            toast?.setText(message)
            toast?.duration = Toast.LENGTH_LONG
        }
        toast?.show()
    }
}

/**
 * Display the simple Toast message with the [Toast.LENGTH_SHORT] duration.
 *
 * @param message the message text resource.
 */
fun Fragment.toastOnUi(message: Int) = requireActivity().toastOnUi(message)

/**
 * Display the simple Toast message with the [Toast.LENGTH_SHORT] duration.
 *
 * @param message the message text.
 */
fun Fragment.toastOnUi(message: CharSequence) = requireActivity().toastOnUi(message)

/**
 * Display the simple Toast message with the [Toast.LENGTH_LONG] duration.
 *
 * @param message the message text resource.
 */
fun Fragment.longToast(message: Int) = requireContext().longToastOnUi(message)

/**
 * Display the simple Toast message with the [Toast.LENGTH_LONG] duration.
 *
 * @param message the message text.
 */
fun Fragment.longToast(message: CharSequence) = requireContext().longToastOnUi(message)