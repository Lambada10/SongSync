package pl.lambada.songsync.activities.quicksearch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pl.lambada.songsync.data.UserSettingsController
import pl.lambada.songsync.data.remote.lyrics_providers.LyricsProviderService

class QuickLyricsSearchViewModelFactory(
    private val userSettingsController: UserSettingsController,
    private val lyricsProviderService: LyricsProviderService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuickLyricsSearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuickLyricsSearchViewModel(userSettingsController, lyricsProviderService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}