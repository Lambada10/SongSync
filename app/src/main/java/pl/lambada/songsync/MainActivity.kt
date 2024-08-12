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
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.domain.model.Song
import pl.lambada.songsync.ui.Navigator
import pl.lambada.songsync.ui.components.dialogs.NoInternetDialog
import pl.lambada.songsync.ui.screens.LoadingScreen
import pl.lambada.songsync.ui.screens.Providers
import pl.lambada.songsync.ui.theme.SongSyncTheme
import pl.lambada.songsync.util.dataStore
import pl.lambada.songsync.util.get
import java.io.File

/**
 * The main activity of the SongSync app.
 */
class MainActivity : ComponentActivity() {
    val viewModel: MainViewModel by viewModels()
    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState The saved instance state.
     */
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
        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val navController = rememberNavController()
            var hasLoadedPermissions by remember { mutableStateOf(false) }
            var hasPermissions by remember { mutableStateOf(false) }
            var internetConnection by remember { mutableStateOf(true) }
            var themeDefined by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                val disableMarquee = dataStore.get(booleanPreferencesKey("marquee_disable"), false)
                viewModel.disableMarquee.value = disableMarquee

                val pureBlack = dataStore.get(booleanPreferencesKey("pure_black"), false)
                viewModel.pureBlack.value = pureBlack
                themeDefined = true

                val sdCardPath = dataStore.get(stringPreferencesKey("sd_card_path"), null)
                if (sdCardPath != null) {
                    viewModel.sdCardPath = sdCardPath
                }

                val includeTranslation = dataStore.get(booleanPreferencesKey("include_translation"), false)
                viewModel.includeTranslation = includeTranslation

                val blacklist = dataStore.get(stringPreferencesKey("blacklist"), null)
                if (blacklist != null) {
                    viewModel.blacklistedFolders = blacklist.split(",").toMutableList()
                }

                val hideLyrics = dataStore.get(booleanPreferencesKey("hide_lyrics"), false)
                viewModel.hideLyrics = hideLyrics

                val provider = dataStore.get(stringPreferencesKey("provider"), Providers.SPOTIFY.displayName)
                viewModel.provider = Providers.entries.find { it.displayName == provider }!!

                // Get token upon app start
                launch(Dispatchers.IO) {
                    try {
                        viewModel.refreshToken()
                    } catch (e: Exception) {
                        internetConnection = false
                    }
                }

                // Create our subdirectory in downloads if it doesn't exist
                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val songSyncDir = File(downloadsDir, "SongSync")
                if (!songSyncDir.exists()) {
                    songSyncDir.mkdir()
                }

                createNotificationChannel()
            }

            if (themeDefined)
                SongSyncTheme(pureBlack = viewModel.pureBlack.value) {
                    // I'll cry if this crashes due to memory concerns
                    val selected = rememberSaveable(saver = Saver(
                        save = { it.toTypedArray() }, restore = { mutableStateListOf(*it) }
                    )) { mutableStateListOf<String>() }
                    var allSongs by remember { mutableStateOf<List<Song>?>(null) }

                    // Check for permissions and get all songs
                    RequestPermissions(
                        onGranted = { hasPermissions = true },
                        context = context,
                        onDone = {
                            if (hasPermissions) {
                                // Get all songs
                                scope.launch(Dispatchers.IO) {
                                    allSongs = viewModel.getAllSongs(context)
                                }
                            }
                            hasLoadedPermissions = true
                        }
                    )

                    Surface( modifier = Modifier.fillMaxSize() ) {
                        if (!hasLoadedPermissions) {
                            LoadingScreen()
                        } else if (!hasPermissions) {
                            AlertDialog(
                                onDismissRequest = { /* don't dismiss */ },
                                confirmButton = {
                                    OutlinedButton(
                                        onClick = {
                                            finishAndRemoveTask()
                                        }
                                    ) {
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
                                selected = selected,
                                allSongs = allSongs,
                                viewModel = viewModel
                            )
                        }
                        if (!internetConnection) {
                            NoInternetDialog(
                                onConfirm = { finishAndRemoveTask() },
                                onIgnore = {
                                    internetConnection = true // assume connected (if spotify is down, can use other providers)
                                }
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
        storageManager = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
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
