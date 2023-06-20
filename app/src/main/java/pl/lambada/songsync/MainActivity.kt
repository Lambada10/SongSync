package pl.lambada.songsync

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.ui.Navigator
import pl.lambada.songsync.ui.common.BottomBar
import pl.lambada.songsync.ui.common.TopBar
import pl.lambada.songsync.ui.theme.SongSyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = MainViewModel()
            val navController = rememberNavController()
            var hasPermissions by rememberSaveable { mutableStateOf(false) }
            val context = LocalContext.current

            // Get token upon app start
            Thread {
                viewModel.refreshToken()
            }.start()

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
                        TopBar(navController = navController) },
                    bottomBar = {
                        BottomBar(navController = navController) }
                ) {paddingValues ->
                    Surface(modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                    ) {
                        if (hasPermissions)
                            Navigator(navController = navController, viewModel = viewModel)
                        else {
                            AlertDialog(
                                onDismissRequest = { /* don't dismiss */ },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            finishAndRemoveTask()
                                        }
                                    ) {
                                        Text("Close app")
                                    }
                                },
                                title = { Text("Permission denied") },
                                text = {
                                    Column {
                                        Text("This app requires storage access for scanning your music library and saving lyrics.")
                                        Text("Already granted? Try restarting the app.")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
