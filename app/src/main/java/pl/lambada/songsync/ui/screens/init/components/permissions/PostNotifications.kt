package pl.lambada.songsync.ui.screens.init.components.permissions

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PostNotifications(
    onGranted: () -> Unit,
    onDismiss: () -> Unit
) {
    var notificationPermission = rememberPermissionState(
        permission = android.Manifest.permission.POST_NOTIFICATIONS,
        onPermissionResult = {
            if (it) {
                onGranted()
            }
            onDismiss()
        }
    )
    LaunchedEffect(Unit) {
        notificationPermission.launchPermissionRequest()
    }
}