package pl.lambada.songsync.ui.screens.init

import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import pl.lambada.songsync.data.UserSettingsController

class InitScreenViewModel(
    val userSettingsController: UserSettingsController,
): ViewModel() {
    var allFilesClicked by mutableStateOf(false)
    var notificationClicked by mutableStateOf(false)
    var notificationAccessClicked by mutableStateOf(false)

    var allFilesPermissionGranted by mutableStateOf(false)
    var notificationPermissionGranted by mutableStateOf(false)
    var notificationAccessPermissionGranted by mutableStateOf(false)

    fun onLoad(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            allFilesPermissionGranted = Environment.isExternalStorageManager()
        } else {
            allFilesPermissionGranted = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionGranted = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        notificationAccessPermissionGranted = NotificationManagerCompat
            .getEnabledListenerPackages(context).contains(context.packageName)
    }

    fun onProceed() {
        userSettingsController.updatePassedInit(true)
    }
}