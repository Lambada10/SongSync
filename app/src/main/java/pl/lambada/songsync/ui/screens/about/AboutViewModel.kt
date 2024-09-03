package pl.lambada.songsync.ui.screens.about

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.lambada.songsync.R
import pl.lambada.songsync.data.remote.UserSettingsController
import pl.lambada.songsync.data.remote.github.GithubAPI
import pl.lambada.songsync.domain.model.Release
import pl.lambada.songsync.ui.screens.about.components.UpdateState
import pl.lambada.songsync.util.ext.getVersion
import pl.lambada.songsync.util.showToast

/**
 * ViewModel class for the main functionality of the app.
 */
class AboutViewModel(
    val userSettingsController: UserSettingsController
) : ViewModel() {
    // Mutable states to observe from the UI
    var updateState by mutableStateOf<UpdateState>(UpdateState.Checking)

    // Function to check for updates
    fun checkForUpdates(context: Context) {
        viewModelScope.launch {
            updateState = UpdateState.Checking
            showToast(
                context,
                context.getString(R.string.checking_for_updates),
                long = false
            )

            try {
                val latest = GithubAPI.getLatestRelease()
                val isUpdate = isNewerRelease(context, latest)

                updateState = if (isUpdate)
                    UpdateState.UpdateAvailable(latest)
                else  {
                    showToast(
                        context,
                        context.getString(R.string.up_to_date),
                        long = false
                    )
                    UpdateState.UpToDate
                }

            } catch (e: Exception) {
                showToast(
                    context,
                    context.getString(R.string.error_checking_for_updates),
                    long = false
                )
                updateState = UpdateState.Error(e)
            }
        }
    }

    /**
     * Checks if the latest release is newer than the current version.
     */
    private fun isNewerRelease(context: Context, latestRelease: Release): Boolean {
        val currentVersion = context
            .getVersion()
            .replace(".", "")
            .toInt()
        val latestVersion = latestRelease.tagName
            .replace(".", "")
            .replace("v", "")
            .toInt()

        return latestVersion > currentVersion
    }
}