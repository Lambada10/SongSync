package pl.lambada.songsync.ui.screens.init.components.permissions

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import pl.lambada.songsync.ui.screens.init.InitScreenViewModel

fun LoadPartialPermissionTicks(
    viewModel: InitScreenViewModel,
    context: Context,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        viewModel.allFilesPermissionGranted = Environment.isExternalStorageManager()
    } else {
        viewModel.allFilesPermissionGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        viewModel.notificationPermissionGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}