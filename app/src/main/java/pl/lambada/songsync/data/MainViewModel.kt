package pl.lambada.songsync.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import pl.lambada.songsync.data.api.GithubAPI
import pl.lambada.songsync.data.api.NeteaseAPI
import pl.lambada.songsync.data.api.SpotifyAPI
import pl.lambada.songsync.data.api.SpotifyLyricsAPI
import pl.lambada.songsync.data.dto.Release
import pl.lambada.songsync.data.dto.Song
import pl.lambada.songsync.data.dto.SongInfo
import pl.lambada.songsync.data.ext.getVersion
import pl.lambada.songsync.data.ext.toLrcFile
import java.io.FileNotFoundException
import java.net.UnknownHostException

/**
 * ViewModel class for the main functionality of the app.
 */
class MainViewModel : ViewModel() {
    private var cachedSongs: List<Song>? = null
    var nextSong: Song? = null // for fullscreen downloader dialog

    // Filter settings
    private var cachedFolders: MutableList<String>? = null
    var blacklistedFolders = mutableListOf<String>()
    var hideLyrics = false
    private var hideFolders = blacklistedFolders.isNotEmpty()

    // Spotify API token
    private val spotifyAPI = SpotifyAPI()

    // other settings
    var pureBlack = false
    var sdCardPath = ""

    // LRCLib Track ID
    // var lrcLibID = 0

    // Netease Track ID
    var neteaseID = 0

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
    @Throws(UnknownHostException::class, FileNotFoundException::class, NoTrackFoundException::class)
    suspend fun getSongInfo(query: SongInfo, offset: Int? = 0): SongInfo {
        return spotifyAPI.getSongInfo(query, offset)
        /*
        val result = LRCLibAPI().getSongInfo(query)
        this.id = result?.id ?: throw NoTrackFoundException()
        return result
         */
        /*
        val result = NeteaseAPI().getSongInfo(query, offset?: 0)!!
        this.neteaseID = result.neteaseID!!
        return result
         */
    }

    /**
     * Gets synced lyrics using the song link and returns them as a string formatted as an LRC file.
     * @param songLink The link to the song.
     * @return The synced lyrics as a string.
     */
    suspend fun getSyncedLyrics(songLink: String): String? {
        return SpotifyLyricsAPI().getSyncedLyrics(songLink)
        // return LRCLibAPI().getSyncedLyrics(this.lrcLibID)
        // return NeteaseAPI().getSyncedLyrics(this.neteaseID)
    }

    /**
     * Gets latest GitHub release information.
     * @return The latest release version.
     */
    suspend fun getLatestRelease(): Release {
        return GithubAPI().getLatestRelease()
    }

    /**
     * Checks if the latest release is newer than the current version.
     */
    suspend fun isNewerRelease(context: Context): Boolean {
        val currentVersion = context.getVersion().replace(".", "").toInt()
        val latestVersion = getLatestRelease().tagName?.replace(".", "")?.replace("v", "")?.toInt()

        return latestVersion!! > currentVersion
    }

    /**
     * Loads all songs from the MediaStore.
     * @param context The application context.
     * @return A list of Song objects representing the songs.
     */
    fun getAllSongs(context: Context): List<Song> {
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
    fun filterSongs(): List<Song>? {
        hideFolders = blacklistedFolders.isNotEmpty()
        return when {
            hideLyrics && hideFolders -> {
                cachedSongs!!.filter { it.filePath.toLrcFile()?.exists() != true && !blacklistedFolders.contains(it.filePath!!.substring(0, it.filePath.lastIndexOf("/"))) }
            }
            hideLyrics -> {
                cachedSongs!!.filter { it.filePath.toLrcFile()?.exists() != true }
            }
            hideFolders -> {
                cachedSongs!!.filter { !blacklistedFolders.contains(it.filePath!!.substring(0, it.filePath.lastIndexOf("/"))) }
            }
            else -> {
                null
            }
        }
    }
}

class NoTrackFoundException : Exception()

class EmptyQueryException : Exception()