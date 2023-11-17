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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.data.dto.Song
import pl.lambada.songsync.ui.Navigator
import pl.lambada.songsync.ui.components.BottomBar
import pl.lambada.songsync.ui.components.TopBar
import pl.lambada.songsync.ui.components.dialogs.NoInternetDialog
import pl.lambada.songsync.ui.screens.LoadingScreen
import pl.lambada.songsync.ui.screens.Providers
import pl.lambada.songsync.ui.theme.SongSyncTheme
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.UnknownHostException

/**
 * The main activity of the SongSync app.
 */
class MainActivity : ComponentActivity() {

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState The saved instance state.
     */
    @SuppressLint("SuspiciousIndentation")
    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val viewModel: MainViewModel by viewModels()
            val navController = rememberNavController()
            var hasLoadedPermissions by remember { mutableStateOf(false) }
            var hasPermissions by remember { mutableStateOf(false) }
            var internetConnection by remember { mutableStateOf(true) }
            var themeDefined by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                // Load user-defined settings
                val sharedPreferences = context.getSharedPreferences(
                    "pl.lambada.songsync_preferences",
                    Context.MODE_PRIVATE
                )

                val pureBlack = sharedPreferences.getBoolean("pure_black", false)
                viewModel.pureBlack.value = pureBlack
                themeDefined = true

                val sdCardPath = sharedPreferences.getString("sd_card_path", null)
                if (sdCardPath != null) {
                    viewModel.sdCardPath = sdCardPath
                }

                val blacklist = sharedPreferences.getString("blacklist", null)
                if (blacklist != null) {
                    viewModel.blacklistedFolders = blacklist.split(",").toMutableList()
                }
                val hideLyrics = sharedPreferences.getBoolean("hide_lyrics", false)
                viewModel.hideLyrics = hideLyrics

                val provider =
                    sharedPreferences.getString("provider", Providers.SPOTIFY.displayName)
                viewModel.provider = Providers.values().find { it.displayName == provider }!!

                // Get token upon app start
                launch(Dispatchers.IO) {
                    try {
                        viewModel.refreshToken()
                    } catch (e: Exception) {
                        if (e is UnknownHostException || e is FileNotFoundException || e is IOException)
                            internetConnection = false
                        else
                            throw e
                    }
                }

                // Create our subdirectory in downloads if it doesn't exist
                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val songSyncDir = File(downloadsDir, "SongSync")
                if (!songSyncDir.exists()) {
                    songSyncDir.mkdir()
                }

                // Register notification channel
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

            if (themeDefined)
                SongSyncTheme(pureBlack = viewModel.pureBlack.value) {
                    // I'll cry if this crashes due to memory concerns
                    val selected = rememberSaveable(saver = Saver(
                        save = { it.toTypedArray() }, restore = { mutableStateListOf(*it) }
                    )) { mutableStateListOf<String>() }
                    var allSongs by remember { mutableStateOf<List<Song>?>(null) }
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

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

                    Scaffold(
                        topBar = {
                            TopBar(
                                selected = selected,
                                currentRoute = currentRoute,
                                allSongs = allSongs
                            )
                        },
                        bottomBar = {
                            BottomBar(currentRoute = currentRoute, navController = navController)
                        }
                    ) { paddingValues ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .imePadding()
                                .let {
                                    if (WindowInsets.isImeVisible) {
                                        // exclude bottom bar if ime is visible
                                        it.padding(
                                            PaddingValues(
                                                top = paddingValues.calculateTopPadding(),
                                                start = paddingValues.calculateStartPadding(
                                                    LocalLayoutDirection.current
                                                ),
                                                end = paddingValues.calculateEndPadding(
                                                    LocalLayoutDirection.current
                                                )
                                            )
                                        )
                                    } else {
                                        it.padding(paddingValues)
                                    }
                                }
                        ) {
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
                            } else if (!internetConnection) {
                                NoInternetDialog {
                                    finishAndRemoveTask()
                                }
                            } else {
                                Navigator(
                                    navController = navController, selected = selected,
                                    allSongs = allSongs, viewModel = viewModel
                                )
                            }
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
