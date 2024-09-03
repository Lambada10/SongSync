package pl.lambada.songsync.ui.screens.about

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.lambada.songsync.R
import pl.lambada.songsync.data.remote.UpdateService
import pl.lambada.songsync.data.remote.UserSettingsController
import pl.lambada.songsync.ui.screens.about.components.UpdateState
import pl.lambada.songsync.util.showToast

/**
 * ViewModel class for the main functionality of the app.
 */
class AboutViewModel(
    val userSettingsController: UserSettingsController,
    private val updateService: UpdateService = UpdateService()
) : ViewModel() {
    var updateState by mutableStateOf<UpdateState>(UpdateState.Idle)

    fun dismissUpdate() { updateState = UpdateState.Idle }

    fun checkForUpdates(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { updateService.checkForUpdates(context) }.collect {
                updateState = it

                when (it) {
                    UpdateState.Checking -> showToast(
                        context,
                        context.getString(R.string.checking_for_updates),
                        long = false
                    )

                    is UpdateState.Error -> showToast(
                        context,
                        context.getString(R.string.error_checking_for_updates),
                        long = false
                    )

                    UpdateState.UpToDate -> showToast(
                        context,
                        context.getString(R.string.up_to_date),
                        long = false
                    )
                    else -> { }
                }
            }
        }
    }
}