package pl.lambada.songsync.data

import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageInfo
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import kotlinx.serialization.json.Json
import org.apache.commons.text.similarity.LevenshteinDistance
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

class MainViewModel : ViewModel() {

    val jsonDec = Json {
        ignoreUnknownKeys = true
    }

    /*
    Spotify API credentials, can be overwritten by user.
    If you want to build this app yourself, you need to create your own Spotify API credentials
    and put them in your local gradle.properties file.
     */
    private var spotifyClientID = BuildConfig.SPOTIFY_CLIENT_ID
    private var spotifyClientSecret = BuildConfig.SPOTIFY_CLIENT_SECRET
    private var spotifyToken = ""
    private var tokenTime: Long = 0

    /*
    Used for storing responses, used in Alert Dialogs (show response)
    I won't refactor functions to return responses. So we have this instead.
     */
    private var spotifyResponse = ""
    private var lyricsResponse = ""

    /*
    Refreshes token by sending a request to Spotify API.
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
        this.spotifyToken = json.access_token
        this.tokenTime = System.currentTimeMillis()
    }

    /*
    Gets song info from Spotify API.
    Parameters:
        * SongInfo object, with songName and artistName fields filled
        * offset (optional), used for trying to find a better match/searching again
     */
    fun getSongInfo(query: SongInfo, offset: Int? = 0): SongInfo {

        if (System.currentTimeMillis() - this.tokenTime > 1800000) { // 30 minutes
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

        this.spotifyResponse = response

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

    /*
    Calculates similarity between two strings.
    Used for comparing query and search results to eliminate false positives.
     */
    fun calculateStringSimilarity(string1: String, string2: String): Double {
        val levenshteinDistance = LevenshteinDistance()
        val distance = levenshteinDistance.apply(
            string1.lowercaseWithLocale(),
            string2.lowercaseWithLocale()
        )
        val maxLength = maxOf(string1.length, string2.length)
        return ((1 - distance.toDouble() / maxLength) * 10000).toInt().toDouble() / 100
    }

    /*
    Gets synced lyrics using song link, and returns them as a string (formatted as LRC file).
     */
    fun getSyncedLyrics(songLink: String): String {
        val url = URL("https://spotify-lyric-api.herokuapp.com/?url=$songLink&format=lrc")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)

        connection.disconnect()

        this.lyricsResponse = response

        val json = jsonDec.decodeFromString<SyncedLinesResponse>(response)

        if (json.error) return context.getString(R.string.lyrics_not_found)

        val lines = json.lines
        val syncedLyrics = StringBuilder()

        for (line in lines) {
            syncedLyrics.append("[${line.timeTag}]${line.words}\n")
        }

        return syncedLyrics.toString()
    }

    /*
    Loads songs from MediaStore.
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
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val title = it.getString(titleColumn)
                val artist = it.getString(artistColumn)
                val albumId = it.getLong(albumIdColumn)
                val filePath = it.getString(pathColumn)

                val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
                val imgUri = ContentUris.withAppendedId(
                    sArtworkUri,
                    albumId
                )

                val file = File(filePath)
                val fileName = file.name

                val song = Song(id, title, artist, imgUri, filePath, fileName)
                songs.add(song)
            }
        }
        cursor?.close()

        return songs
    }

    /*
    Contributors info:
        - name
        - additional info (optional)
        - github link (optional but you probably have one)
        - telegram link (optional)
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

        return listOf(
            lambada10,
            bobbyESP
        )
    }

    /*
    Get app version.
     */
    fun getVersion(context: Context): String {
        val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return pInfo.versionName
    }
}

enum class ContributionLevel {
    CONTRIBUTOR,
    DEVELOPER,
    LEAD_DEVELOPER;

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
