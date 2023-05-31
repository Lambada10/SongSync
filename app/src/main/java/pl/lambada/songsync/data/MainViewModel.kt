package pl.lambada.songsync.data

import android.util.Log
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.ViewModel
import org.apache.commons.text.similarity.LevenshteinDistance
import org.json.JSONObject
import pl.lambada.songsync.BuildConfig
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

class MainViewModel: ViewModel() {
    /*
    Spotify API credentials, can be overwritten by user.
    If you want to build this app yourself, you need to create your own Spotify API credentials
    and put them in your local gradle.properties file.
     */
    private var spotifyClientID = BuildConfig.SPOTIFY_CLIENT_ID
    private var spotifyClientSecret = BuildConfig.SPOTIFY_CLIENT_SECRET
    var spotifyToken = ""

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

        val postData = "grant_type=client_credentials&client_id=$spotifyClientID&client_secret=$spotifyClientSecret"
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
        if(!checkToken())
            refreshToken()

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
        val json = JSONObject(response)
        val track = json.getJSONObject("tracks").getJSONArray("items").getJSONObject(0)

        val artistsArray = track.getJSONArray("artists")
        val artists = StringBuilder()
        for (i in 0 until artistsArray.length()) {
            val currentArtist = artistsArray.getJSONObject(i)
            artists.append(currentArtist.getString("name")).append(",")
        }

        val albumArtURL = track.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url")

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
        val distance = levenshteinDistance.apply(string1.lowercase(Locale.getDefault()), string2.lowercase(Locale.getDefault()))
        val maxLength = maxOf(string1.length, string2.length)
        return ((1 - distance.toDouble() / maxLength) * 10000).toInt().toDouble() / 100
    }


}
