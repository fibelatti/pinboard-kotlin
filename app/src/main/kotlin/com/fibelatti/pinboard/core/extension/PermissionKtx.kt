package com.fibelatti.pinboard.core.extension

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * Check if the given [permission] is [granted][PackageManager.PERMISSION_GRANTED].
 *
 * @param permission The name of the permission to check.
 * @param minSdk Optional min SDK where the permission is required.
 * @return true if the permission was granted, false otherwise.
 */
fun Context.isPermissionGranted(
    permission: String,
    minSdk: Int = Build.VERSION.SDK_INT,
): Boolean {
    return Build.VERSION.SDK_INT < minSdk ||
        checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}

/**
 * Signal whether the permission was denied for good. When this returns false it means that the
 * system will no longer show the permission dialog and the user must grant it through the settings
 * app.
 *
 * @param permission The name of the permission to check.
 * @return true if the permission can be requested again, false otherwise.
 */
fun Activity.canRequestPermissionAgain(permission: String): Boolean = shouldShowRequestPermissionRationale(permission)
