package com.jack.bookshelf.help.permission

interface OnPermissionsDeniedCallback {
    fun onPermissionsDenied(requestCode: Int, deniedPermissions: Array<String>)
}
