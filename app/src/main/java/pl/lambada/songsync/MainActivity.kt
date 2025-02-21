package pl.lambada.songsync

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.view.ViewCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import pl.lambada.songsync.data.UserSettingsController
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
        super.onCreate(savedInstanceState)
        // fixes weird system bars background upon app loading
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
            view.setPadding(0, 0, 0, 0)
            insets
        }

        val dataStore = this.dataStore
        val userSettingsController = UserSettingsController(dataStore)
        checkOrCreateDownloadSubFolder()
        createNotificationChannel()

        setContent {
            val navController = rememberNavController()
            var networkError by rememberSaveable { mutableStateOf<Boolean?>(null) }
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                context.cacheDir.deleteRecursively()
                if (networkError == null) lyricsProviderService
                    .refreshSpotifyToken()
                    .onFailure { networkError = true }
            }

            SongSyncTheme(pureBlack = userSettingsController.pureBlack) {
                if (networkError == true) NoInternetDialog(
                    onConfirm = ::finishAndRemoveTask,
                    onIgnore = { networkError = false }
                )

                // check in case user revoked permissions later
                if (userSettingsController.passedInit)
                    CheckForPermissions(
                        userSettingsController = userSettingsController
                    )

                Surface(modifier = Modifier.fillMaxSize()) {
                    Navigator(
                        navController = navController,
                        userSettingsController = userSettingsController,
                        lyricsProviderService = lyricsProviderService
                    )
                }
            }
        }
    }

    override fun onResume() {
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(2) // "Done" notification
        super.onResume()
    }
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
private fun MainActivity.CheckForPermissions(
    userSettingsController: UserSettingsController
) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
        if (!Environment.isExternalStorageManager()) {
            userSettingsController.updatePassedInit(false)
        }
    } else {
        val permissions = rememberMultiplePermissionsState(
            permissions = listOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )
        if (!permissions.allPermissionsGranted) {
            userSettingsController.updatePassedInit(false)
        }
    }
}

private fun checkOrCreateDownloadSubFolder() {
    val downloadsDir = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS
    )

    val songSyncDir = File(downloadsDir, "SongSync")

    if (!songSyncDir.exists()) songSyncDir.mkdir()
}

private fun Activity.createNotificationChannel() {
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
