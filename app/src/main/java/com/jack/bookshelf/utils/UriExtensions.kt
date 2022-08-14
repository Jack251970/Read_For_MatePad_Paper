package com.jack.bookshelf.utils

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import com.jack.bookshelf.R
import com.jack.bookshelf.help.permission.Permissions
import com.jack.bookshelf.help.permission.PermissionsCompat
import timber.log.Timber
import java.io.File
import java.io.IOException

fun Uri.isContentScheme() = this.scheme == "content"

/**
 * 读取URI
 */
fun AppCompatActivity.readUri(uri: Uri?, success: (name: String, bytes: ByteArray) -> Unit) {
    uri ?: return
    try {
        if (uri.isContentScheme()) {
            val doc = DocumentFile.fromSingleUri(this, uri)
            doc ?: throw IOException("未获取到文件")
            val name = doc.name ?: throw IOException("未获取到文件名")
            val fileBytes = DocumentUtils.readBytes(this, doc.uri)
            success.invoke(name, fileBytes)
        } else {
            PermissionsCompat.Builder(this)
                .addPermissions(
                    Permissions.READ_EXTERNAL_STORAGE,
                    Permissions.WRITE_EXTERNAL_STORAGE
                )
                .rationale(R.string.bg_image_per)
                .onGranted {
                    RealPathUtil.getPath(this, uri)?.let { path ->
                        val imgFile = File(path)
                        success.invoke(imgFile.name, imgFile.readBytes())
                    }
                }
                .request()
        }
    } catch (e: Exception) {
        Timber.e(e)
        toastOnUi(e.localizedMessage ?: "read uri error")
    }
}

/**
 * 读取URI
 */
fun Fragment.readUri(uri: Uri?, success: (name: String, bytes: ByteArray) -> Unit) {
    uri ?: return
    try {
        if (uri.isContentScheme()) {
            val doc = DocumentFile.fromSingleUri(requireContext(), uri)
            doc ?: throw IOException("未获取到文件")
            val name = doc.name ?: throw IOException("未获取到文件名")
            val fileBytes = DocumentUtils.readBytes(requireContext(), doc.uri)
            success.invoke(name, fileBytes)
        } else {
            PermissionsCompat.Builder(this)
                .addPermissions(
                    Permissions.READ_EXTERNAL_STORAGE,
                    Permissions.WRITE_EXTERNAL_STORAGE
                )
                .rationale(R.string.bg_image_per)
                .onGranted {
                    RealPathUtil.getPath(requireContext(), uri)?.let { path ->
                        val imgFile = File(path)
                        success.invoke(imgFile.name, imgFile.readBytes())
                    }
                }
                .request()
        }
    } catch (e: Exception) {
        Timber.e(e)
        toastOnUi(e.localizedMessage ?: "read uri error")
    }
}

@Throws(Exception::class)
fun Uri.readBytes(context: Context): ByteArray {
    return if (this.isContentScheme()) {
        DocumentUtils.readBytes(context, this)
    } else {
        val path = RealPathUtil.getPath(context, this)
        if (path?.isNotEmpty() == true) {
            File(path).readBytes()
        } else {
            throw IOException("获取文件真实地址失败\n${this.path}")
        }
    }
}

@Throws(Exception::class)
fun Uri.readText(context: Context): String {
    readBytes(context).let {
        return String(it)
    }
}

@Throws(Exception::class)
fun Uri.writeBytes(
    context: Context,
    byteArray: ByteArray
): Boolean {
    if (this.isContentScheme()) {
        return DocumentUtils.writeBytes(context, byteArray, this)
    } else {
        val path = RealPathUtil.getPath(context, this)
        if (path?.isNotEmpty() == true) {
            File(path).writeBytes(byteArray)
            return true
        }
    }
    return false
}

@Throws(Exception::class)
fun Uri.writeText(context: Context, text: String): Boolean {
    return writeBytes(context, text.toByteArray())
}

fun Uri.writeBytes(
    context: Context,
    fileName: String,
    byteArray: ByteArray
): Boolean {
    if (this.isContentScheme()) {
        DocumentFile.fromTreeUri(context, this)?.let { pDoc ->
            DocumentUtils.createFileIfNotExist(pDoc, fileName)?.let {
                return it.uri.writeBytes(context, byteArray)
            }
        }
    } else {
        FileUtils.createFileWithReplace(path + File.separatorChar + fileName)
            .writeBytes(byteArray)
        return true
    }
    return false
}