package pl.lambada.songsync

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.data.Screens
import pl.lambada.songsync.ui.Navigator
import pl.lambada.songsync.ui.common.BottomBar
import pl.lambada.songsync.ui.common.TopBar
import pl.lambada.songsync.ui.theme.SongSyncTheme
import java.lang.Thread.sleep

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = MainViewModel()
            val navController = rememberNavController()
            val context = LocalContext.current

            // Get token upon app start
            Thread {
                viewModel.refreshToken()
            }.start()

            var hasPermissions by rememberSaveable {
                mutableStateOf(false)
            }

            // get permissions
            val permission = android.Manifest.permission.READ_MEDIA_AUDIO
            val mediaPermissionState = rememberPermissionState(permission = permission)
            if(!mediaPermissionState.status.isGranted)
                LaunchedEffect(Unit) {
                    mediaPermissionState.launchPermissionRequest()
                }

            if(!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = android.net.Uri.parse(
                    String.format(
                        "package:%s",
                        context.applicationContext.packageName
                    )
                )
                startActivity(intent)
            }

            if(mediaPermissionState.status == PermissionStatus.Granted && Environment.isExternalStorageManager())
                hasPermissions = true

            SongSyncTheme {
                Scaffold(
                    topBar = {
                        TopBar(navController = navController) },
                    bottomBar = {
                        BottomBar(navController = navController) }
                ) {paddingValues ->
                    Surface(modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)) {
                        if (hasPermissions)
                            Navigator(navController = navController, viewModel = viewModel)
                        else
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
                                text = { Column {
                                    Text("Required permissions not granted.")
                                    Text("This app needs media access for scanning your music library, and storage access for saving lyrics.")
                                    Text("Please restart the app and grant all permissions.")
                                } }
                            )
                    }
                }
            }
        }
    }
}
