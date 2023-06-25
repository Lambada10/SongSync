package pl.lambada.songsync.data

import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageInfo
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import org.apache.commons.text.similarity.LevenshteinDistance
import org.json.JSONObject
import pl.lambada.songsync.BuildConfig
import pl.lambada.songsync.data.ext.lowercaseWithLocale
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainViewModel : ViewModel() {
    /*
    Spotify API credentials, can be overwritten by user.
    If you want to build this app yourself, you need to create your own Spotify API credentials
    and put them in your local gradle.properties file.
     */
    private var spotifyClientID = BuildConfig.SPOTIFY_CLIENT_ID
    private var spotifyClientSecret = BuildConfig.SPOTIFY_CLIENT_SECRET
    private var spotifyToken = ""

    /*
    Used for storing responses, used in Alert Dialogs (show response)
    I won't refactor functions to return responses. So we have this instead.
     */
    var spotifyResponse = ""
    var lyricsResponse = ""

    /*
    Checks if token is valid by sending a request to Spotify API.
     */
    fun checkToken(): Boolean {
        try {
            val endpoint = "https://api.spotify.com/v1/search"
            val query = "test"
            val type = "track"
            val limit = 1
            val url = URL("$endpoint?q=$query&type=$type&limit=$limit")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $spotifyToken")
            if (connection.responseCode != 200) {
                throw FileNotFoundException()
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }

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

        val json = JSONObject(response)
        this.spotifyToken = json.getString("access_token")
    }

    /*
    Gets song info from Spotify API.
    Parameters:
        * SongInfo object, with songName and artistName fields filled
        * offset (optional), used for trying to find a better match/searching again
     */
    fun getSongInfo(query: SongInfo, offset: Int? = 0): SongInfo {
        /*
        if(!checkToken())
            refreshToken()
         */ // one token should be enough for one session, i have to limit API calls somehow

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

        val json = JSONObject(response)
        val track = json.getJSONObject("tracks").getJSONArray("items").getJSONObject(0)

        val artistsArray = track.getJSONArray("artists")
        val artists = StringBuilder()
        for (i in 0 until artistsArray.length()) {
            val currentArtist = artistsArray.getJSONObject(i)
            artists.append(currentArtist.getString("name")).append(",")
        }

        val albumArtURL =
            track.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url")

        val spotifyURL: String = track.getJSONObject("external_urls").getString("spotify")


        return SongInfo(
            track.getString("name"),
            artists.toString().dropLast(1),
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

        val json = JSONObject(response)

        if (json.getBoolean("error"))
            return "No lyrics found."

        val lines = json.getJSONArray("lines")
        val syncedLyrics = StringBuilder()
        for (i in 0 until lines.length()) {
            val currentLine = lines.getJSONObject(i)
            syncedLyrics.append("[${currentLine.getString("timeTag")}").append("]")
                .append(currentLine.getString("words")).append("\n")
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
    fun getContributorsInfo(): List<Map<String, String>> {
        val lambada10 = mapOf(
            "name" to "Lambada10",
            "additionalInfo" to "Lead developer",
            "github" to "https://github.com/Lambada10",
            "telegram" to "https://t.me/Lambada10"
        )
        val bobbyESP = mapOf(
            "name" to "BobbyESP",
            "additionalInfo" to "Contributor",
            "github" to "https://github.com/BobbyESP",
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
