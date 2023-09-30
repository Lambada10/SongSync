package pl.lambada.songsync.data.api

import kotlinx.serialization.json.Json
import pl.lambada.songsync.data.EmptyQueryException
import pl.lambada.songsync.data.NoTrackFoundException
import pl.lambada.songsync.data.dto.SongInfo
import pl.lambada.songsync.data.dto.TrackSearchResult
import pl.lambada.songsync.data.dto.WebPlayerTokenResponse
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets

class SpotifyAPI {
    private val webPlayerURL = "https://open.spotify.com/"
    private val baseURL = "https://api.spotify.com/v1/"
    private val jsonDec = Json { ignoreUnknownKeys = true }

    private var spotifyToken = ""
    private var tokenTime: Long = 0

    /**
     * Refreshes the access token by sending a request to the Spotify API.
     */
    fun refreshToken() {
        if (System.currentTimeMillis() - tokenTime < 1800000) { // 30 minutes
            return
        }

        val url = URL(webPlayerURL + "get_access_token?reason=transport&productType=web_player")
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "GET"
        connection.setRequestProperty("Content-Type", "application/json")

        val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
        connection.disconnect()
        val json = jsonDec.decodeFromString<WebPlayerTokenResponse>(response)

        this.spotifyToken = json.accessToken
        this.tokenTime = System.currentTimeMillis()
    }

    /**
     * Gets song information from the Spotify API.
     * @param query The SongInfo object with songName and artistName fields filled.
     * @param offset (optional) The offset used for trying to find a better match or searching again.
     * @return The SongInfo object containing the song information.
     */
    @Throws(UnknownHostException::class, FileNotFoundException::class, NoTrackFoundException::class)
    fun getSongInfo(query: SongInfo, offset: Int? = 0): SongInfo {
        refreshToken()

        val search = URLEncoder.encode(
            "${query.songName} ${query.artistName}",
            StandardCharsets.UTF_8.toString()
        )

        if (search == "+")
            throw EmptyQueryException()

        val url = URL(baseURL + "search?q=$search&type=track&limit=1&offset=$offset")
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "GET"
        connection.setRequestProperty("Authorization", "Bearer $spotifyToken")

        val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)

        connection.disconnect()

        val json = jsonDec.decodeFromString<TrackSearchResult>(response)
        if (json.tracks.items.isEmpty())
            throw NoTrackFoundException()
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
}