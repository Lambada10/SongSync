package pl.lambada.songsync.data.remote.lyrics_providers.spotify

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.lambada.songsync.data.EmptyQueryException
import pl.lambada.songsync.data.NoTrackFoundException
import pl.lambada.songsync.domain.model.SongInfo
import pl.lambada.songsync.domain.model.lyrics_providers.spotify.TrackSearchResult
import pl.lambada.songsync.domain.model.lyrics_providers.spotify.WebPlayerTokenResponse
import pl.lambada.songsync.util.networking.Ktor.client
import pl.lambada.songsync.util.networking.Ktor.json
import java.io.FileNotFoundException
import java.net.URLEncoder
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets

class SpotifyAPI {
    private val webPlayerURL = "https://open.spotify.com/"
    private val baseURL = "https://api.spotify.com/v1/"

    private var spotifyToken = ""
    private var tokenTime: Long = 0

    /**
     * Refreshes the access token by sending a request to the Spotify API.
     */
    suspend fun refreshToken() {
        val response = client.get(
            webPlayerURL + "get_access_token?reason=transport&productType=web_player"
        ) {
            headers.append(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"
            )
        }
        val responseBody = response.bodyAsText(Charsets.UTF_8)

        val json = json.decodeFromString<WebPlayerTokenResponse>(responseBody)

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
    suspend fun getSongInfo(query: SongInfo, offset: Int? = 0): SongInfo {
        if (System.currentTimeMillis() - tokenTime > 1800000) // 30 minutes
            refreshToken()

        val search = withContext(Dispatchers.IO) {
            URLEncoder.encode(
                "${query.songName} ${query.artistName}",
                StandardCharsets.UTF_8.toString()
            )
        }

        if (search == "+")
            throw EmptyQueryException()

        val response = client.get(
            baseURL + "search?q=$search&type=track&limit=1&offset=$offset"
        ) {
            headers.append("Authorization", "Bearer $spotifyToken")
        }
        val responseBody = response.bodyAsText(Charsets.UTF_8)

        val json = json.decodeFromString<TrackSearchResult>(responseBody)
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