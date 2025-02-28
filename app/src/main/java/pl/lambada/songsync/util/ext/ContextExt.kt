package pl.lambada.songsync.util.ext

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

/**
 * Extension function to get the version name of the application.
 *
 * @return The version name as a [String].
 */
fun Context.getVersion(): String {
    val pInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("deprecation")
        packageManager.getPackageInfo(packageName, 0)
    }
    return pInfo.versionName.toString()
}