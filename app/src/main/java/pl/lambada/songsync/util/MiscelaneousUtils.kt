package pl.lambada.songsync.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

fun isLegacyFileAccessRequired(filePath: String?): Boolean {
    // Before Android 11, not in internal storage
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.R
            && filePath?.contains("/storage/emulated/0/") == false
}

fun openFileFromPath(context: Context, filePath: String) {
    val file = File(filePath)
    if (!file.exists()) {
        showToast(context, "File does not exist")
        return
    }

    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        FileProvider.getUriForFile(context, context.packageName + ".provider", file)
    } else {
        Uri.fromFile(file)
    }

    val intent = Intent(Intent.ACTION_VIEW)
        .setDataAndType(uri, "audio/mp3")
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "No app found to open the music file.", Toast.LENGTH_SHORT).show()
    }
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
