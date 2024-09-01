package pl.lambada.songsync.ui.screens.about

import android.content.Context
import androidx.lifecycle.ViewModel
import pl.lambada.songsync.data.remote.UserSettingsController
import pl.lambada.songsync.data.remote.github.GithubAPI
import pl.lambada.songsync.domain.model.Release
import pl.lambada.songsync.util.ext.getVersion

/**
 * ViewModel class for the main functionality of the app.
 */
class AboutViewModel(
    val userSettingsController: UserSettingsController
) : ViewModel() {
    /**
     * Gets latest GitHub release information.
     * @return The latest release version.
     */
    suspend fun getLatestRelease(): Release {
        return GithubAPI.getLatestRelease()
    }

    /**
     * Checks if the latest release is newer than the current version.
     */
    suspend fun isNewerRelease(context: Context): Boolean {
        val currentVersion = context.getVersion().replace(".", "").toInt()
        val latestVersion = getLatestRelease().tagName.replace(".", "").replace("v", "").toInt()

        return latestVersion > currentVersion
    }
}