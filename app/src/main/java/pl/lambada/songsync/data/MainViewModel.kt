package pl.lambada.songsync.data

import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageInfo
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import kotlinx.serialization.json.Json
import pl.lambada.songsync.BuildConfig
import pl.lambada.songsync.MainActivity.Companion.context
import pl.lambada.songsync.R
import pl.lambada.songsync.data.dto.AccessTokenResponse
import pl.lambada.songsync.data.dto.Song
import pl.lambada.songsync.data.dto.SongInfo
import pl.lambada.songsync.data.dto.SyncedLinesResponse
import pl.lambada.songsync.data.dto.TrackSearchResult
import pl.lambada.songsync.data.ext.lowercaseWithLocale
import pl.lambada.songsync.getStringById
import java.io.BufferedReader
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * ViewModel class for the main functionality of the app.
 */
class MainViewModel : ViewModel() {

    val jsonDec = Json {
        ignoreUnknownKeys = true
    }

    // Spotify API credentials
    private var spotifyClientID = BuildConfig.SPOTIFY_CLIENT_ID
    private var spotifyClientSecret = BuildConfig.SPOTIFY_CLIENT_SECRET
    private var spotifyToken = ""
    private var tokenTime: Long = 0

    // Responses from Spotify and lyrics API
    private var spotifyResponse = ""
    private var lyricsResponse = ""

    /**
     * Refreshes the access token by sending a request to the Spotify API.
     */
    fun refreshToken() {
        val url = URL("https://accounts.spotify.com/api/token")
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        connection.doOutput = true

        val postData =
            "grant_type=client_credentials&client_id=$spotifyClientID&client_secret=$spotifyClientSecret"
        val postDataBytes = postData.toByteArray(StandardCharsets.UTF_8)

        connection.outputStream.use {
            it.write(postDataBytes)
        }

        val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)

        connection.disconnect()

        val json = jsonDec.decodeFromString<AccessTokenResponse>(response)
        this.spotifyToken = json.accessToken
        this.tokenTime = System.currentTimeMillis()
    }

    /**
     * Gets song information from the Spotify API.
     * @param query The SongInfo object with songName and artistName fields filled.
     * @param offset (optional) The offset used for trying to find a better match or searching again.
     * @return The SongInfo object containing the song information.
     */
    fun getSongInfo(query: SongInfo, offset: Int? = 0): SongInfo {
        if (System.currentTimeMillis() - tokenTime > 1800000) { // 30 minutes
            refreshToken()
        }

        val endpoint = "https://api.spotify.com/v1/search"
        val search = URLEncoder.encode(
            "${query.songName} ${query.artistName}",
            StandardCharsets.UTF_8.toString()
        )
        val url = URL("$endpoint?q=$search&type=track&limit=1&offset=$offset")
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "GET"
        connection.setRequestProperty("Authorization", "Bearer $spotifyToken")

        val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)

        connection.disconnect()

        spotifyResponse = response

        val json = jsonDec.decodeFromString<TrackSearchResult>(response)
        val track = json.tracks.items[0]

        val artists = track.artists.joinToString(", ") { it.name }

        val albumArtURL = track.album.images[0].url

        val spotifyURL: String = track.externalUrls.spotify

        return SongInfo(
            track.name,
            artists,
            spotifyURL,
            albumArtURL
        )
    }

    /**
     * Gets synced lyrics using the song link and returns them as a string formatted as an LRC file.
     * @param songLink The link to the song.
     * @return The synced lyrics as a string.
     */
    fun getSyncedLyrics(songLink: String): String {
        val url = URL("https://spotify-lyric-api.herokuapp.com/?url=$songLink&format=lrc")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)

        connection.disconnect()

        lyricsResponse = response

        val json = jsonDec.decodeFromString<SyncedLinesResponse>(response)


        if (json.error)
            return context.getString(R.string.lyrics_not_found)


        val lines = json.lines
        val syncedLyrics = StringBuilder()

        for (line in lines) {
            syncedLyrics.append("[${line.timeTag}]${line.words}\n")
        }

        return syncedLyrics.toString()
    }

    /**
     * Loads all songs from the MediaStore.
     * @param context The application context.
     * @return A list of Song objects representing the songs.
     */
    fun getAllSongs(context: Context): List<Song> {
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
                val title = it.getString(titleColumn)
                val artist = it.getString(artistColumn)
                val albumId = it.getLong(albumIdColumn)
                val filePath = it.getString(pathColumn)

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

        return songs
    }

    /**
     * Retrieves information about the contributors to the app.
     * @return A list of maps containing the contributors' information.
     */
    fun getContributorsInfo(): List<Map<ContributorsArgs, String>> {
        val lambada10 = mapOf(
            ContributorsArgs.NAME to "Lambada10",
            ContributorsArgs.ADDITIONAL_INFO to ContributionLevel.LEAD_DEVELOPER.toString(),
            ContributorsArgs.GITHUB to "https://github.com/Lambada10",
            ContributorsArgs.TELEGRAM to "https://t.me/Lambada10"
        )
        val bobbyESP = mapOf(
            ContributorsArgs.NAME to "BobbyESP",
            ContributorsArgs.ADDITIONAL_INFO to ContributionLevel.CONTRIBUTOR.toString(),
            ContributorsArgs.GITHUB to "https://github.com/BobbyESP",
        )
        val akane = mapOf(
            ContributorsArgs.NAME to "AkaneTan",
            ContributorsArgs.ADDITIONAL_INFO to ContributionLevel.CONTRIBUTOR.toString(),
            ContributorsArgs.GITHUB to "https://github.com/AkaneTan",
        )

        return listOf(
            lambada10,
            bobbyESP,
            akane
        )
    }

    /**
     * Gets the version of the app.
     * @param context The application context.
     * @return The version name of the app.
     */
    @Suppress("DEPRECATION")
    fun getVersion(context: Context): String {
        val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return pInfo.versionName
    }
}

/**
 * Defines the contribution level of a contributor.
 */
enum class ContributionLevel {
    CONTRIBUTOR,
    DEVELOPER,
    LEAD_DEVELOPER;

    /**
     * Overrides the toString method to return the translatable string representation of the contribution level.
     * @return The translatable string representation of the contribution level.
     */
    override fun toString(): String {
        return when (this) {
            CONTRIBUTOR -> getStringById(R.string.contributor)
            DEVELOPER -> getStringById(R.string.developer)
            LEAD_DEVELOPER -> getStringById(R.string.lead_developer)
        }
    }

    companion object {
        fun fromString(string: String): ContributionLevel {
            return when (string) {
                getStringById(R.string.contributor) -> CONTRIBUTOR
                getStringById(R.string.developer) -> DEVELOPER
                getStringById(R.string.lead_developer) -> LEAD_DEVELOPER
                else -> throw IllegalArgumentException("Invalid contribution level.")
            }
        }
    }
}

/**
 * Defines the arguments for the contributors' information.
 */
enum class ContributorsArgs {
    NAME,
    ADDITIONAL_INFO,
    GITHUB,
    TELEGRAM;

    override fun toString(): String {
        return when (this) {
            NAME -> "name"
            ADDITIONAL_INFO -> "additionalInfo"
            GITHUB -> "github"
            TELEGRAM -> "telegram"
        }
    }
}
