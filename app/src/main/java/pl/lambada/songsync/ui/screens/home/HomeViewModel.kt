package pl.lambada.songsync.ui.screens.home

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import pl.lambada.songsync.data.remote.UserSettingsController
import pl.lambada.songsync.data.remote.lyrics_providers.others.AppleAPI
import pl.lambada.songsync.data.remote.lyrics_providers.others.LRCLibAPI
import pl.lambada.songsync.data.remote.lyrics_providers.others.NeteaseAPI
import pl.lambada.songsync.data.remote.lyrics_providers.spotify.SpotifyAPI
import pl.lambada.songsync.data.remote.lyrics_providers.spotify.SpotifyLyricsAPI
import pl.lambada.songsync.domain.model.Song
import pl.lambada.songsync.domain.model.SongInfo
import pl.lambada.songsync.ui.screens.Providers
import pl.lambada.songsync.util.ext.toLrcFile
import java.io.FileNotFoundException
import java.net.UnknownHostException

/**
 * ViewModel class for the main functionality of the app.
 */
class HomeViewModel(val userSettingsController: UserSettingsController) : ViewModel() {
    private var cachedSongs: List<Song>? = null
    val selected = mutableStateListOf<String>()
    var allSongs by mutableStateOf<List<Song>?>(null)

    var searchQuery by mutableStateOf("")

    private var ableToSelect by mutableStateOf<List<Song>?>(null)

    // Filter settings
    private var cachedFolders: MutableList<String>? = null
    private var hideFolders = userSettingsController.blacklistedFolders.isNotEmpty()

    // filtered folders/lyrics songs
    private var _cachedFilteredSongs = MutableStateFlow<List<Song>>(emptyList())

    // searching
    private var _searchResults = MutableStateFlow<List<Song>>(emptyList())

    // Spotify API token
    private val spotifyAPI = SpotifyAPI()

    var displaySongs by mutableStateOf(
        when {
            searchQuery.isNotEmpty() -> _searchResults.value
            _cachedFilteredSongs.value.isNotEmpty() -> _cachedFilteredSongs.value
            else -> allSongs ?: listOf()
        }
    )

    var showFilters by mutableStateOf(false)
    var showingSearch by  mutableStateOf(false)
    var showSearch by mutableStateOf(showingSearch)

    val songsToBatchDownload = if (selected.isEmpty())
        displaySongs
    else
        (allSongs ?: listOf()).filter { selected.contains(it.filePath) }.toList()

    // LRCLib Track ID
    private var lrcLibID = 0

    // Netease Track ID and stuff
    private var neteaseID = 0L

    // Apple Track ID
    private var appleID = 0L
    // TODO: Use values from SongInfo object returned by search instead of storing them here

    init {
        viewModelScope.launch {
            launch { updateAbleToSelect() }
            launch { updateSongsToDisplay() }
        }
    }

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

    /**
     * Gets song information from the Spotify API.
     * @param query The SongInfo object with songName and artistName fields filled.
     * @param offset (optional) The offset used for trying to find a better match or searching again.
     * @return The SongInfo object containing the song information.
     */
    @Throws(
        UnknownHostException::class, FileNotFoundException::class, NoTrackFoundException::class,
        EmptyQueryException::class, InternalErrorException::class
    )
    suspend fun getSongInfo(query: SongInfo, offset: Int? = 0): SongInfo {
        return try {
            when (userSettingsController.selectedProvider) {
                Providers.SPOTIFY -> spotifyAPI.getSongInfo(query, offset)
                Providers.LRCLIB -> LRCLibAPI().getSongInfo(query).also {
                    this.lrcLibID = it?.lrcLibID ?: 0
                } ?: throw NoTrackFoundException()

                Providers.NETEASE -> NeteaseAPI().getSongInfo(query, offset).also {
                    this.neteaseID = it?.neteaseID ?: 0
                } ?: throw NoTrackFoundException()

                Providers.APPLE -> AppleAPI().getSongInfo(query).also {
                    this.appleID = it?.appleID ?: 0
                } ?: throw NoTrackFoundException()
            }
        } catch (e: InternalErrorException) {
            throw e
        } catch (e: NoTrackFoundException) {
            throw e
        } catch (e: EmptyQueryException) {
            throw e
        } catch (e: Exception) {
            throw InternalErrorException(Log.getStackTraceString(e))
        }
    }

    /**
     * Gets synced lyrics using the song link and returns them as a string formatted as an LRC file.
     * @param songLink The link to the song.
     * @return The synced lyrics as a string.
     */
    suspend fun getSyncedLyrics(songLink: String, version: String): String? {
        return try {
            when (userSettingsController.selectedProvider) {
                Providers.SPOTIFY -> SpotifyLyricsAPI().getSyncedLyrics(songLink, version)
                Providers.LRCLIB -> LRCLibAPI().getSyncedLyrics(this.lrcLibID)
                Providers.NETEASE -> NeteaseAPI().getSyncedLyrics(this.neteaseID, userSettingsController.includeTranslation)
                Providers.APPLE -> AppleAPI().getSyncedLyrics(this.appleID)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun updateAllSongs(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        allSongs = getAllSongs(context)
    }

    /**
     * Loads all songs from the MediaStore.
     * @param context The application context.
     * @return A list of Song objects representing the songs.
     */
    private fun getAllSongs(context: Context): List<Song> {
        return cachedSongs ?: run {
            val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
            )
            val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"

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

            for (song in getAllSongs(context)) {
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
                _cachedFilteredSongs?.value = cachedSongs!!
                    .filter {
                        it.filePath.toLrcFile()?.exists() != true && !userSettingsController.blacklistedFolders.contains(
                            it.filePath!!.substring(
                                0, it.filePath.lastIndexOf("/")
                            )
                        )
                    }
            }

            userSettingsController.hideLyrics -> {
                _cachedFilteredSongs?.value = cachedSongs!!
                    .filter { it.filePath.toLrcFile()?.exists() != true }
            }

            hideFolders -> {
                _cachedFilteredSongs?.value = cachedSongs!!.filter {
                    !userSettingsController.blacklistedFolders.contains(
                        it.filePath!!.substring(
                            0,
                            it.filePath.lastIndexOf("/")
                        )
                    )
                }
            }

            else -> {
                _cachedFilteredSongs?.value = emptyList()
            }
        }
    }

    private suspend fun updateAbleToSelect() = coroutineScope {
        _searchResults.combine(_cachedFilteredSongs) { searched, filtered ->
            ableToSelect = when {
                searched.isNotEmpty() -> searched
                filtered.isNotEmpty() -> filtered
                else -> allSongs
            }
        }
    }

    fun invertSongSelection() {
        val willBeSelected = ableToSelect?.map { it.filePath }?.toMutableList()

        for (song in selected) {
            willBeSelected?.remove(song)
        }

        selected.clear()
        if (willBeSelected != null) {
            for (song in willBeSelected) {
                song?.let { selected.add(it) }
            }
        }
    }

    /**
     * Refreshes the access token by sending a request to the Spotify API.
     */
    suspend fun refreshToken() {
        spotifyAPI.refreshToken()
    }


    fun selectAllSongs() {
        ableToSelect
            ?.mapNotNull { it.filePath }
            ?.forEach(selected::add)
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
}

class NoTrackFoundException : Exception()

class InternalErrorException(msg: String) : Exception(msg)

class EmptyQueryException : Exception()