package pl.lambada.songsync.ui.screens.home

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.util.fastMapNotNull
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import pl.lambada.songsync.data.UserSettingsController
import pl.lambada.songsync.data.remote.lyrics_providers.LyricsProviderService
import pl.lambada.songsync.domain.model.Song
import pl.lambada.songsync.domain.model.SongInfo
import pl.lambada.songsync.domain.model.SortOrders
import pl.lambada.songsync.domain.model.SortValues
import pl.lambada.songsync.util.downloadLyrics
import pl.lambada.songsync.util.ext.toLrcFile

/**
 * ViewModel class for the main functionality of the app.
 */
class HomeViewModel(
    val userSettingsController: UserSettingsController,
    private val lyricsProviderService: LyricsProviderService
) : ViewModel() {
    var cachedSongs: List<Song>? = null
    val selectedSongs = mutableStateListOf<String>()
    var allSongs by mutableStateOf<List<Song>?>(null)

    var isRefreshing by mutableStateOf(false)

    var searchQuery by mutableStateOf("")

    // Filter settings
    private var cachedFolders: MutableList<String>? = null
    private var hideFolders = userSettingsController.blacklistedFolders.isNotEmpty()

    // filtered folders/lyrics songs
    private var _cachedFilteredSongs = MutableStateFlow<List<Song>>(emptyList())

    // searching
    private var _searchResults = MutableStateFlow<List<Song>>(emptyList())

    var displaySongs by mutableStateOf(
        when {
            searchQuery.isNotEmpty() -> _searchResults.value
            _cachedFilteredSongs.value.isNotEmpty() -> _cachedFilteredSongs.value
            else -> allSongs ?: listOf()
        }
    )

    var showFilters by mutableStateOf(false)
    var showSort by mutableStateOf(false)
    var showingSearch by  mutableStateOf(false)
    var showSearch by mutableStateOf(showingSearch)

    val songsToBatchDownload by derivedStateOf {
        if (selectedSongs.isEmpty())
            displaySongs
        else
            (allSongs ?: listOf()).filter { selectedSongs.contains(it.filePath) }.toList()
    }

    init { viewModelScope.launch { updateSongsToDisplay() } }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun updateSongsToDisplay() = coroutineScope {
        snapshotFlow { allSongs }
            .filterNotNull()
            // simple .combine wasn't enough apparently, so im using this
            .flatMapLatest { all ->
                _cachedFilteredSongs.combine(_searchResults) { filtered, searchResults ->
                    when {
                        searchQuery.isNotEmpty() -> searchResults
                        filtered.isNotEmpty() -> filtered
                        else -> all
                    }
                }
            }.collect { newDisplaySongs ->
                displaySongs = newDisplaySongs
            }
    }

    fun updateAllSongs(context: Context, sortBy: SortValues, sortOrder: SortOrders) = viewModelScope.launch(Dispatchers.IO) {
        allSongs = getAllSongs(context, sortBy, sortOrder)
    }

    /**
     * Loads all songs from the MediaStore.
     * @param context The application context.
     * @return A list of Song objects representing the songs.
     */
    private fun getAllSongs(context: Context, sortBy: SortValues, sortOrder: SortOrders): List<Song> {
        return cachedSongs ?: run {
            val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
            )
            val sortOrder = sortBy.name + " " + sortOrder.queryName

            val songs = mutableListOf<Song>()
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use {
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val pathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (it.moveToNext()) {
                    val title = it.getString(titleColumn).let { str ->
                        if (str == "<unknown>") null else str
                    }
                    val artist = it.getString(artistColumn).let { str ->
                        if (str == "<unknown>") null else str
                    }
                    val albumId = it.getLong(albumIdColumn)
                    val filePath = it.getString(pathColumn)

                    @Suppress("SpellCheckingInspection")
                    val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
                    val imgUri = ContentUris.withAppendedId(
                        sArtworkUri,
                        albumId
                    )

                    val song = Song(title, artist, imgUri, filePath)
                    songs.add(song)
                }
            }
            cursor?.close()
            cachedSongs = songs
            viewModelScope.launch { filterSongs() }
            cachedSongs!!
        }
    }

    /**
     * Updates song search (filter) results based on the query.
     * @param query The search query.
     */
    fun updateSearchResults(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (query.isEmpty()) {
                _searchResults.value = emptyList()
                return@launch
            }

            val data: List<Song> = when {
                _cachedFilteredSongs.value.isNotEmpty() -> _cachedFilteredSongs.value
                cachedSongs != null -> cachedSongs!!
                else -> { return@launch }
            }

            val results = data.filter {
                it.title?.contains(query, ignoreCase = true) == true ||
                it.artist?.contains(query, ignoreCase = true) == true
            }

            _searchResults.value = results
        }
    }

    /**
     * Loads all songs' folders
     * @param context The application context.
     * @return A list of folders.
     */
    fun getSongFolders(context: Context): List<String> {
        return cachedFolders ?: run {
            val folders = mutableListOf<String>()

            for (song in getAllSongs(context, SortValues.TITLE, SortOrders.ASCENDING)) {
                val path = song.filePath
                val folder = path?.substring(0, path.lastIndexOf("/"))
                if (folder != null && !folders.contains(folder))
                    folders.add(folder)
            }

            cachedFolders = folders
            cachedFolders!!
        }
    }

    /**
     * Filter songs based on user's preferences.
     * @return A list of songs depending on the user's preferences. If no preferences are set, null is returned, so app will use all songs.
     */
    fun filterSongs() = viewModelScope.launch {
        hideFolders = userSettingsController.blacklistedFolders.isNotEmpty()

        when {
            userSettingsController.hideLyrics && hideFolders -> {
                _cachedFilteredSongs.value = cachedSongs!!
                    .filter {
                        it.filePath.toLrcFile()?.exists() != true && !userSettingsController.blacklistedFolders.contains(
                            it.filePath!!.substring(
                                0, it.filePath.lastIndexOf("/")
                            )
                        )
                    }
            }

            userSettingsController.hideLyrics -> {
                _cachedFilteredSongs.value = cachedSongs!!
                    .filter { it.filePath.toLrcFile()?.exists() != true }
            }

            hideFolders -> {
                _cachedFilteredSongs.value = cachedSongs!!.filter {
                    !userSettingsController.blacklistedFolders.contains(
                        it.filePath!!.substring(
                            0,
                            it.filePath.lastIndexOf("/")
                        )
                    )
                }
            }

            else -> {
                _cachedFilteredSongs.value = emptyList()
            }
        }
    }

    fun invertSongSelection() = viewModelScope.launch {
        val newSelectedSongs = displaySongs.filter { it.filePath !in selectedSongs }
        selectedSongs.clear()
        selectedSongs.addAll(newSelectedSongs.mapNotNull { it.filePath })
    }

    fun selectAllDisplayingSongs() = viewModelScope.launch {
        selectedSongs.clear()
        selectedSongs.addAll(displaySongs.fastMapNotNull { it.filePath })
    }

    fun onHideLyricsChange(newHideLyrics: Boolean) {
        userSettingsController.updateHideLyrics(newHideLyrics)
    }

    fun onToggleFolderBlacklist(folder: String, blacklisted: Boolean) {
        if (blacklisted) {
            userSettingsController.updateBlacklistedFolders(
                userSettingsController.blacklistedFolders + folder
            )
        } else {
            userSettingsController.updateBlacklistedFolders(
                userSettingsController.blacklistedFolders - folder
            )
        }
    }

    suspend fun getSongInfo(query: SongInfo): SongInfo? =
        lyricsProviderService.getSongInfo(query, provider = userSettingsController.selectedProvider)

    suspend fun getSyncedLyrics(link: String?, version: String): String? {
        return try {
            lyricsProviderService.getSyncedLyrics(
                link,
                version,
                provider = userSettingsController.selectedProvider,
                includeTranslationNetEase = userSettingsController.includeTranslation,
                multiPersonWordByWord = userSettingsController.multiPersonWordByWord,
                unsyncedFallbackMusixmatch = userSettingsController.unsyncedFallbackMusixmatch
            )
        } catch (e: Exception) {
            null
        }
    }

    fun selectSong(song: Song, newValue: Boolean) {
        if (newValue) {
            song.filePath?.let { selectedSongs.add(it) }
            showSearch = false
            showingSearch = false
        } else {
            selectedSongs.remove(song.filePath)

            if (selectedSongs.size == 0 && searchQuery.isNotEmpty())
                showingSearch = true // show again but don't focus
        }
    }

    fun batchDownloadLyrics(
        context: Context,
        onProgressUpdate: (successCount: Int, noLyricsCount: Int, failedCount: Int) -> Unit,
        onDownloadComplete: () -> Unit,
        onRateLimitReached: () -> Unit
    ) = viewModelScope.launch {
        downloadLyrics(
            songs = songsToBatchDownload,
            viewModel = this@HomeViewModel,
            context = context,
            onProgressUpdate = onProgressUpdate,
            onDownloadComplete = onDownloadComplete,
            onRateLimitReached = onRateLimitReached,
        )
    }
}