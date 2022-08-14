package com.jack.bookshelf.help.permission

interface OnPermissionsResultCallback {

    fun onPermissionsGranted(requestCode: Int)

    fun onPermissionsDenied(requestCode: Int, deniedPermissions: Array<String>)

}