package pl.lambada.songsync.util

import android.content.Context
import android.os.Build
import android.widget.Toast

fun isLegacyFileAccessRequired(filePath: String?): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.R
            || filePath?.contains("/storage/emulated/0/") == true
}

fun showToast(context: Context, messageResId: Int, vararg args: Any, long: Boolean = true) {
    Toast
        .makeText(
            context,
            context.getString(messageResId, *args),
            if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        )
        .show()
}

fun showToast(context: Context, message: String, long: Boolean = true) {
    Toast
        .makeText(
            context,
            message,
            if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        )
        .show()
}
