package pl.lambada.songsync

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.ui.Navigator
import pl.lambada.songsync.ui.components.BottomBar
import pl.lambada.songsync.ui.components.TopBar
import pl.lambada.songsync.ui.components.dialogs.NoInternetDialog
import pl.lambada.songsync.ui.theme.SongSyncTheme
import java.io.FileNotFoundException
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val viewModel: MainViewModel by viewModels()
            val navController = rememberNavController()
            var hasPermissions by remember { mutableStateOf(false) }
            var internetConnection by remember { mutableStateOf(true) }

            // Get token upon app start
            LaunchedEffect(Unit) {
                launch(Dispatchers.IO) {
                    try {
                        viewModel.refreshToken()
                    } catch (e: Exception) {
                        if (e is UnknownHostException || e is FileNotFoundException)
                            internetConnection = false
                        else
                            throw e
                    }
                }
            }

            // Request permissions and wait with check
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = android.net.Uri.parse(
                    String.format(
                        "package:%s",
                        context.applicationContext.packageName
                    )
                )
                startActivity(intent)
            } else {
                hasPermissions = true
            }

            SongSyncTheme {
                Scaffold(
                    topBar = {
                        TopBar(navController = navController)
                    },
                    bottomBar = {
                        BottomBar(navController = navController)
                    }
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        if (!hasPermissions) {
                            AlertDialog(
                                onDismissRequest = { /* don't dismiss */ },
                                confirmButton = {
                                    Button(
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
                                        Text(stringResource(R.string.already_granted_restart))
                                    }
                                }
                            )
                        } else if (!internetConnection) {
                            NoInternetDialog {
                                finishAndRemoveTask()
                            }
                        } else {
                            Navigator(navController = navController, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}