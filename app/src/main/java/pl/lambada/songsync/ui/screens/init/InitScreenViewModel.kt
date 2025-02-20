package pl.lambada.songsync.ui.screens.init

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import pl.lambada.songsync.data.UserSettingsController

class InitScreenViewModel(
    val userSettingsController: UserSettingsController,
): ViewModel() {
    var allFilesClicked by mutableStateOf(false)
    var notificationClicked by mutableStateOf(false)

    var allFilesPermissionGranted by mutableStateOf(false)
    var notificationPermissionGranted by mutableStateOf(false)

    fun onProceed() {
        userSettingsController.updatePassedInit(true)
    }
}