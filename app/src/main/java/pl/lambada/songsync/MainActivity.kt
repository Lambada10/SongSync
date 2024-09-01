package pl.lambada.songsync

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import pl.lambada.songsync.data.remote.UserSettingsController
import pl.lambada.songsync.data.remote.lyrics_providers.LyricsProviderService
import pl.lambada.songsync.ui.Navigator
import pl.lambada.songsync.ui.components.dialogs.NoInternetDialog
import pl.lambada.songsync.ui.screens.home.LoadingScreen
import pl.lambada.songsync.ui.theme.SongSyncTheme
import pl.lambada.songsync.util.dataStore
import java.io.File

/**
 * The main activity of the SongSync app.
 */
class MainActivity : ComponentActivity() {
    private val lyricsProviderService = LyricsProviderService()

    @SuppressLint("SuspiciousIndentation")

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
            view.setPadding(0, 0, 0, 0)
            insets
        }

        val dataStore = this.dataStore
        val userSettingsController = UserSettingsController(dataStore)

        var networkError by mutableStateOf(false)

        setContent {
            val scope = rememberCoroutineScope()
            val viewModel = viewModel {
                MainViewModel(userSettingsController)
            }
            val context = LocalContext.current
            val navController = rememberNavController()
            var hasLoadedPermissions by remember { mutableStateOf(false) }
            var hasPermissions by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                // Create our subdirectory in downloads if it doesn't exist
                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val songSyncDir = File(downloadsDir, "SongSync")
                if (!songSyncDir.exists()) {
                    songSyncDir.mkdir()
                }

                createNotificationChannel()

                lyricsProviderService.refreshSpotifyToken()
                    .onFailure { networkError = true }
            }

            SongSyncTheme(pureBlack = userSettingsController.pureBlack) {
                // Check for permissions and get all songs
                RequestPermissions(
                    onGranted = { hasPermissions = true },
                    context = context,
                    onDone = { hasLoadedPermissions = true }
                )

                if (networkError) NoInternetDialog(
                    onConfirm = ::finishAndRemoveTask,
                    onIgnore = { networkError = false }
                )

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (!hasLoadedPermissions) {
                        LoadingScreen()
                    } else if (!hasPermissions) {
                        AlertDialog(
                            onDismissRequest = { /* don't dismiss */ },
                            confirmButton = {
                                OutlinedButton(onClick = ::finishAndRemoveTask) {
                                    Text(stringResource(R.string.close_app))
                                }
                            },
                            title = { Text(stringResource(R.string.permission_denied)) },
                            text = {
                                Column {
                                    Text(stringResource(R.string.requires_higher_storage_permissions))
                                }
                            }
                        )
                    } else {
                        Navigator(
                            navController = navController,
                            viewModel = viewModel,
                            userSettingsController = userSettingsController,
                            lyricsProviderService = lyricsProviderService
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(2) // "Done" notification
        super.onResume()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channelId = getString(R.string.batch_download_lyrics)
            val channelName = getString(R.string.batch_download_lyrics)
            val channelDescription = getString(R.string.batch_download_lyrics)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, channelName, importance)
            channel.description = channelDescription

            notificationManager.createNotificationChannel(channel)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermissions(onGranted: () -> Unit, context: Context, onDone: () -> Unit) {
    var storageManager: ActivityResultLauncher<Intent>? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        storageManager =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (Environment.isExternalStorageManager()) {
                    onGranted()
                }
                onDone()
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
            onDone()
        }
    )
    var notificationPermission: PermissionState? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        notificationPermission = rememberPermissionState(
            permission = android.Manifest.permission.POST_NOTIFICATIONS
        )
    }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notificationPermission!!.status.isGranted)
                notificationPermission.launchPermissionRequest()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
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
                onGranted()
                onDone()
            }
        } else {
            if (storagePermissionState.allPermissionsGranted) {
                onGranted()
                onDone()
            } else {
                storagePermissionState.launchMultiplePermissionRequest()
            }
        }
    }
}
