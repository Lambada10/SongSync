package pl.lambada.songsync.ui.screens.init.components.permissions

import android.app.ActivityManager
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AllFilesAccess(
    onGranted: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var storageManager: ActivityResultLauncher<Intent>? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        storageManager =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (Environment.isExternalStorageManager()) {
                    onGranted()
                }
                onDismiss()
            }
    }
    val storagePermissionState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ),
        onPermissionsResult = {
            if (
                it[android.Manifest.permission.READ_EXTERNAL_STORAGE]!! &&
                it[android.Manifest.permission.WRITE_EXTERNAL_STORAGE]!!
            ) {
                onGranted()
            }
            onDismiss()
        }
    )
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.addCategory("android.intent.category.DEFAULT")
            intent.data = android.net.Uri.parse(
                String.format(
                    "package:%s",
                    context.applicationContext.packageName
                )
            )
            storageManager!!.launch(intent)
        } else {
            storagePermissionState.launchMultiplePermissionRequest()
        }
    }
}