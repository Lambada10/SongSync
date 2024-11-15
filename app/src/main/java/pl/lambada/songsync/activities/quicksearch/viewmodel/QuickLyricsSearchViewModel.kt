package pl.lambada.songsync.activities.quicksearch.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.lambada.songsync.R
import pl.lambada.songsync.data.UserSettingsController
import pl.lambada.songsync.data.remote.lyrics_providers.LyricsProviderService
import pl.lambada.songsync.domain.model.SongInfo
import pl.lambada.songsync.util.ResourceState
import pl.lambada.songsync.util.ScreenState
import pl.lambada.songsync.util.ext.getVersion
import pl.lambada.songsync.util.parseLyrics

class QuickLyricsSearchViewModel(
    val userSettingsController: UserSettingsController,
    private val lyricsProviderService: LyricsProviderService
) : ViewModel() {
    private val mutableState = MutableStateFlow(QuickSearchViewState())
    val state = mutableState.asStateFlow()

    data class QuickSearchViewState(
        val song: Pair<String, String>? = null, // Pair of song title and artist's name
        val screenState: ScreenState<SongInfo> = ScreenState.Loading,
        val lyricsState: ResourceState<String> = ResourceState.Loading(),
        val parsedLyrics: List<Pair<String, String>> = emptyList()
    )

    private fun fetchSongData(song: Pair<String, String>, context: Context) {
        updateScreenState(ScreenState.Loading)

        viewModelScope.launch(Dispatchers.IO) {
            val songInfoCall = runCatching {
                lyricsProviderService
                    .getSongInfo(
                        query = SongInfo(song.first, song.second),
                        offset = 0,
                        provider = userSettingsController.selectedProvider
                    )
            }

            if (songInfoCall.isSuccess) {
                val result = songInfoCall.getOrNull()

                if (result == null) {
                    updateScreenState(
                        ScreenState.Error(
                            Exception("The song information retrieved is null")
                        )
                    )
                } else {
                    updateScreenState(ScreenState.Success(result))
                    fetchLyrics(result.songLink, context)
                }

            } else {
                val exception = songInfoCall.exceptionOrNull()
                updateScreenState(
                    ScreenState.Error(
                        exception ?: Exception("An unknown error has occurred")
                    )
                )
            }
        }
    }

    private fun fetchLyrics(songLink: String?, context: Context) {
        updateLyricsState(ResourceState.Loading())
        viewModelScope.launch(Dispatchers.IO) {

            val lyricsCall = runCatching {
                getSyncedLyrics(
                    link = songLink,
                    version = context.getVersion()
                )
            }

            if (lyricsCall.isSuccess) {
                val syncedLyrics = lyricsCall.getOrNull()

                if (syncedLyrics == null) updateLyricsState(
                    ResourceState.Error("The fetched lyrics content is null.")
                ) else {
                    updateLyricsState(ResourceState.Success(syncedLyrics))
                    parseLyrics(syncedLyrics).let { parsedLyrics ->
                        mutableState.update {
                            it.copy(parsedLyrics = parsedLyrics)
                        }
                    }
                }

            } else {
                val exception = lyricsCall.exceptionOrNull()
                updateLyricsState(
                    ResourceState.Error(
                        exception?.localizedMessage
                            ?: (context.getString(R.string.unknown) + exception?.stackTrace.toString())
                    )
                )
            }
        }
    }

    private suspend fun getSyncedLyrics(link: String?, version: String): String? =
        lyricsProviderService.getSyncedLyrics(
            link,
            version,
            userSettingsController.selectedProvider,
            userSettingsController.includeTranslation,
            userSettingsController.multiPersonWordByWord,
            userSettingsController.syncedMusixmatch
        )

    private fun updateScreenState(screenState: ScreenState<SongInfo>) {
        if (screenState != mutableState.value.screenState) {
            mutableState.update {
                it.copy(screenState = screenState)
            }
        }
    }

    private fun updateLyricsState(lyricsState: ResourceState<String>) {
        if (lyricsState != mutableState.value.lyricsState) {
            mutableState.update {
                it.copy(lyricsState = lyricsState)
            }
        }
    }

    fun onEvent(event: Event) {
        when (event) {
            is Event.Fetch -> {
                mutableState.update {
                    it.copy(song = event.song)
                }
                fetchSongData(event.song, event.context)
            }
        }
    }

    interface Event {
        data class Fetch(val song: Pair<String, String>, val context: Context) : Event
    }
}