package pl.lambada.songsync.ui.screens.init

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import pl.lambada.songsync.R
import pl.lambada.songsync.data.UserSettingsController
import pl.lambada.songsync.ui.ScreenHome
import pl.lambada.songsync.ui.screens.init.components.InitScreenTopBar
import pl.lambada.songsync.ui.screens.init.components.PermissionItem
import pl.lambada.songsync.ui.screens.init.components.permissions.AllFilesAccess
import pl.lambada.songsync.ui.screens.init.components.permissions.LoadPartialPermissionTicks
import pl.lambada.songsync.ui.screens.init.components.permissions.PostNotifications

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitScreen(
    navController: NavController,
    viewModel: InitScreenViewModel = viewModel(),
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        LoadPartialPermissionTicks(
            viewModel = viewModel,
            context = context,
        )
    }
    Scaffold(
        topBar = {
            InitScreenTopBar()
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.padding(paddingValues).padding(16.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.init_screen_description),
                    modifier = Modifier.padding(8.dp)
                )
                PermissionItem(
                    title = R.string.all_files_access,
                    description = R.string.all_files_access_description,
                    onClick = { viewModel.allFilesClicked = true },
                    granted = viewModel.allFilesPermissionGranted
                )
                if (viewModel.allFilesClicked) {
                    AllFilesAccess(
                        onGranted = { viewModel.allFilesPermissionGranted = true },
                        onDismiss = { viewModel.allFilesClicked = false }
                    )
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PermissionItem(
                        title = R.string.notification_access,
                        description = R.string.notification_access_description,
                        onClick = { viewModel.notificationClicked = true },
                        granted = viewModel.notificationPermissionGranted
                    )
                    if (viewModel.notificationClicked) {
                        PostNotifications(
                            onGranted = { viewModel.notificationPermissionGranted = true },
                            onDismiss = { viewModel.notificationClicked = false }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            viewModel.onProceed()
                            navController.navigate(ScreenHome) {
                                popUpTo(ScreenHome) { inclusive = true }
                            }
                        },
                        enabled = viewModel.allFilesPermissionGranted
                    ) {
                        Text(stringResource(R.string._continue))
                    }
                }
            }
        }
    }
}