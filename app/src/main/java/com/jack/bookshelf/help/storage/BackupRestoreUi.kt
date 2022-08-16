package com.jack.bookshelf.help.storage

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.hwangjr.rxbus.RxBus
import com.jack.bookshelf.MApplication
import com.jack.bookshelf.R
import com.jack.bookshelf.base.observer.MySingleObserver
import com.jack.bookshelf.constant.RxBusTag
import com.jack.bookshelf.help.permission.Permissions
import com.jack.bookshelf.help.permission.PermissionsCompat
import com.jack.bookshelf.help.storage.WebDavHelp.getWebDavFileNames
import com.jack.bookshelf.help.storage.WebDavHelp.showRestoreDialog
import com.jack.bookshelf.widget.filepicker.picker.FilePicker
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast

object BackupRestoreUi : Backup.CallBack, Restore.CallBack {

    private const val backupSelectRequestCode = 22
    private const val restoreSelectRequestCode = 33

    private fun getBackupPath(): String? {
        return MApplication.getConfigPreferences().getString("backupPath", null)
    }

    private fun setBackupPath(path: String?) {
        if (path.isNullOrEmpty()) {
            MApplication.getConfigPreferences().edit().remove("backupPath").apply()
        } else {
            MApplication.getConfigPreferences().edit().putString("backupPath", path).apply()
        }
    }

    override fun backupSuccess() {
        MApplication.getInstance().toast(R.string.backup_success)
    }

    override fun backupError(msg: String) {
        MApplication.getInstance().toast(msg)
    }

    override fun restoreSuccess() {
        MApplication.getInstance().toast(R.string.restore_success)
        RxBus.get().post(RxBusTag.RECREATE, true)
    }

    override fun restoreError(msg: String) {
        MApplication.getInstance().toast(msg)
    }

    fun backup(activity: Activity) {
        val backupPath = getBackupPath()
        if (backupPath.isNullOrEmpty()) {
            selectBackupFolder(activity)
        } else if (backupPath.isContentPath()) {
            val uri = Uri.parse(backupPath)
            val doc = DocumentFile.fromTreeUri(activity, uri)
            if (doc?.canWrite() == true) {
                Backup.backup(activity, backupPath, this)
            } else {
                selectBackupFolder(activity)
            }
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            selectBackupFolder(activity)
        } else {
            backupUsePermission(activity)
        }
    }

    private fun backupUsePermission(activity: Activity, path: String = Backup.defaultPath) {
        PermissionsCompat.Builder(activity)
            .addPermissions(*Permissions.Group.STORAGE)
            .rationale(R.string.need_storage_permission_to_backup_book_information)
            .onGranted {
                setBackupPath(path)
                Backup.backup(activity, path, this)
            }
            .request()
    }

    fun selectBackupFolder(activity: Activity) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            try {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                activity.startActivityForResult(intent, backupSelectRequestCode)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                activity.toast(e.localizedMessage ?: "ERROR")
            }
            return
        }
        activity.alert {
            titleResource = R.string.select_folder
            items(activity.resources.getStringArray(R.array.select_folder).toList()) { _, index ->
                when (index) {
                    0 -> {
                        try {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            activity.startActivityForResult(intent, backupSelectRequestCode)
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                            activity.toast(e.localizedMessage ?: "ERROR")
                        }
                    }
                    1 -> {
                        PermissionsCompat.Builder(activity)
                            .addPermissions(*Permissions.Group.STORAGE)
                            .rationale(R.string.need_storage_permission_to_backup_book_information)
                            .onGranted {
                                selectBackupFolderApp(activity, false)
                            }
                            .request()
                    }
                    2 -> {
                        setBackupPath(Backup.defaultPath)
                        backupUsePermission(activity)
                    }
                }
            }
        }.show()
    }

    private fun selectBackupFolderApp(activity: Activity, isRestore: Boolean) {
        val picker = FilePicker(activity, FilePicker.DIRECTORY)
        picker.setBackgroundColor(ContextCompat.getColor(activity, R.color.background))
        picker.setTopBackgroundColor(ContextCompat.getColor(activity, R.color.background))
        picker.setItemHeight(30)
        picker.setOnFilePickListener { currentPath: String ->
            setBackupPath(currentPath)
            if (isRestore) {
                Restore.restore(currentPath, this)
            } else {
                Backup.backup(activity, currentPath, this)
            }
        }
        picker.show()
    }

    fun restore(activity: Activity) {
        Single.create { emitter: SingleEmitter<ArrayList<String>?> ->
            emitter.onSuccess(getWebDavFileNames())
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MySingleObserver<ArrayList<String>?>() {
                override fun onSuccess(strings: ArrayList<String>) {
                    if (!showRestoreDialog(activity, strings, this@BackupRestoreUi)) {
                        val path = getBackupPath()
                        if (TextUtils.isEmpty(path)) {
                            selectRestoreFolder(activity)
                        } else if (path.isContentPath()) {
                            val uri = Uri.parse(path)
                            val doc = DocumentFile.fromTreeUri(activity, uri)
                            if (doc?.canWrite() == true) {
                                Restore.restore(activity, Uri.parse(path), this@BackupRestoreUi)
                            } else {
                                selectRestoreFolder(activity)
                            }
                        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                            selectRestoreFolder(activity)
                        } else {
                            restoreUsePermission(activity)
                        }
                    }
                }
            })
    }

    private fun restoreUsePermission(activity: Activity, path: String = Backup.defaultPath) {
        PermissionsCompat.Builder(activity)
            .addPermissions(*Permissions.Group.STORAGE)
            .rationale(R.string.need_storage_permission_to_backup_book_information)
            .onGranted {
                setBackupPath(path)
                Restore.restore(path, this)
            }
            .request()
    }

    private fun selectRestoreFolder(activity: Activity) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            try {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                activity.startActivityForResult(intent, restoreSelectRequestCode)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                activity.toast(e.localizedMessage ?: "ERROR")
            }
            return
        }
        activity.alert {
            titleResource = R.string.select_folder
            items(activity.resources.getStringArray(R.array.select_folder).toList()) { _, index ->
                when (index) {
                    0 -> {
                        try {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            activity.startActivityForResult(intent, restoreSelectRequestCode)
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                            activity.toast(e.localizedMessage ?: "ERROR")
                        }
                    }
                    1 -> {
                        PermissionsCompat.Builder(activity)
                            .addPermissions(*Permissions.Group.STORAGE)
                            .rationale(R.string.need_storage_permission_to_backup_book_information)
                            .onGranted {
                                selectBackupFolderApp(activity, true)
                            }
                            .request()
                    }
                    2 -> restoreUsePermission(activity)
                }
            }
        }.show()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            backupSelectRequestCode -> if (resultCode == RESULT_OK) {
                data?.data?.let { uri ->
                    MApplication.getInstance().contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    setBackupPath(uri.toString())
                    Backup.backup(MApplication.getInstance(), uri.toString(), this)
                }
            }
            restoreSelectRequestCode -> if (resultCode == RESULT_OK) {
                data?.data?.let { uri ->
                    MApplication.getInstance().contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    setBackupPath(uri.toString())
                    Restore.restore(MApplication.getInstance(), uri, this)
                }
            }
        }
    }

}

fun String?.isContentPath(): Boolean = this?.startsWith("content://") == true