package pl.lambada.songsync.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import pl.lambada.songsync.data.remote.github.GithubAPI
import pl.lambada.songsync.data.remote.lyrics_providers.others.AppleAPI
import pl.lambada.songsync.data.remote.lyrics_providers.others.LRCLibAPI
import pl.lambada.songsync.data.remote.lyrics_providers.others.NeteaseAPI
import pl.lambada.songsync.data.remote.lyrics_providers.spotify.SpotifyAPI
import pl.lambada.songsync.data.remote.lyrics_providers.spotify.SpotifyLyricsAPI
import pl.lambada.songsync.domain.model.Release
import pl.lambada.songsync.domain.model.Song
import pl.lambada.songsync.domain.model.SongInfo
import pl.lambada.songsync.ui.screens.Providers
import pl.lambada.songsync.util.ext.getVersion
import pl.lambada.songsync.util.ext.toLrcFile
import pl.lambada.songsync.util.set
import java.io.FileNotFoundException
import java.net.UnknownHostException

/**
 * ViewModel class for the main functionality of the app.
 */
class MainViewModel : ViewModel() {
    private var cachedSongs: List<Song>? = null
    val selected = mutableStateListOf<String>()
    var allSongs by mutableStateOf<List<Song>?>(null)

    private var ableToSelect by mutableStateOf<List<Song>?>(null)

    // Filter settings
    private var cachedFolders: MutableList<String>? = null
    var blacklistedFolders = mutableListOf<String>()
    var hideLyrics by mutableStateOf(false)
    private var hideFolders = blacklistedFolders.isNotEmpty()

    // filtered folders/lyrics songs
    private var _cachedFilteredSongs: MutableStateFlow<List<Song>> = MutableStateFlow(emptyList())
    val cachedFilteredSongs = _cachedFilteredSongs.asStateFlow()

    // searching
    private var _searchResults: MutableStateFlow<List<Song>> = MutableStateFlow(emptyList())
    val searchResults = _searchResults.asStateFlow()

    // Spotify API token
    private val spotifyAPI = SpotifyAPI()

    // other settings
    var pureBlack: MutableState<Boolean> = mutableStateOf(false)
    var disableMarquee: MutableState<Boolean> = mutableStateOf(false)
    var sdCardPath = ""

    // selected provider
    var selectedProvider by mutableStateOf(Providers.SPOTIFY)

    // LRCLib Track ID
    private var lrcLibID = 0

    // Netease Track ID and stuff
    private var neteaseID = 0L
    var includeTranslation = false

    // Apple Track ID
    private var appleID = 0L
    // TODO: Use values from SongInfo object returned by search instead of storing them here

    init {
        viewModelScope.launch { updateAbleToSelect() }
    }

    /**
     * Refreshes the access token by sending a request to the Spotify API.
     */
    suspend fun refreshToken() {
        spotifyAPI.refreshToken()
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
            when (this.selectedProvider) {
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
            when (this.selectedProvider) {
                Providers.SPOTIFY -> SpotifyLyricsAPI().getSyncedLyrics(songLink, version)
                Providers.LRCLIB -> LRCLibAPI().getSyncedLyrics(this.lrcLibID)
                Providers.NETEASE -> NeteaseAPI().getSyncedLyrics(this.neteaseID, includeTranslation)
                Providers.APPLE -> AppleAPI().getSyncedLyrics(this.appleID)
            }
        } catch (e: Exception) {
            null
        }
    }

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
                cachedFilteredSongs.value.isNotEmpty() -> cachedFilteredSongs.value
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
        hideFolders = blacklistedFolders.isNotEmpty()

        when {
            hideLyrics && hideFolders -> {
                _cachedFilteredSongs?.value = cachedSongs!!
                    .filter {
                        it.filePath.toLrcFile()?.exists() != true && !blacklistedFolders.contains(
                            it.filePath!!.substring(
                                0, it.filePath.lastIndexOf("/")
                            )
                        )
                    }
            }
            hideLyrics -> {
                _cachedFilteredSongs?.value = cachedSongs!!
                    .filter { it.filePath.toLrcFile()?.exists() != true }
            }
            hideFolders -> {
                _cachedFilteredSongs?.value = cachedSongs!!.filter {
                    !blacklistedFolders.contains(
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
        searchResults.combine(cachedFilteredSongs) { searched, filtered ->
            ableToSelect = when {
                searched.isNotEmpty() -> searched
                filtered.isNotEmpty() -> filtered
                else -> allSongs
            }
        }
    }

    fun invertSongSelection() {
        val willBeSelected = ableToSelect?.map { it.filePath }?.toMutableList()

        for (song in selected) { willBeSelected?.remove(song) }

        selected.clear()
        if (willBeSelected != null) {
            for (song in willBeSelected) {
                song?.let { selected.add(it) }
            }
        }
    }

    fun selectAllSongs() {
        ableToSelect
            ?.mapNotNull { it.filePath }
            ?.forEach(selected::add)
    }

    fun onHideLyricsChange(dataStore: DataStore<Preferences>, newHideLyrics: Boolean) {
        dataStore.set(
            booleanPreferencesKey("hide_lyrics"),
            newHideLyrics
        )
        hideLyrics = newHideLyrics
    }

    fun onToggleFolderBlacklist(folder: String, blacklisted: Boolean) {

    }
}

class NoTrackFoundException : Exception()

class InternalErrorException(msg: String) : Exception(msg)

class EmptyQueryException : Exception()