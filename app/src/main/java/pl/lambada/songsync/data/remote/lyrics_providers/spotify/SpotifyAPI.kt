package pl.lambada.songsync.data.remote.lyrics_providers.spotify

import android.util.Log
import dev.turingcomplete.kotlinonetimepassword.HmacAlgorithm
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordConfig
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordGenerator
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.lambada.songsync.domain.model.SongInfo
import pl.lambada.songsync.domain.model.lyrics_providers.spotify.ServerTimeResponse
import pl.lambada.songsync.domain.model.lyrics_providers.spotify.TrackSearchResult
import pl.lambada.songsync.domain.model.lyrics_providers.spotify.WebPlayerTokenResponse
import pl.lambada.songsync.util.EmptyQueryException
import pl.lambada.songsync.util.NoTrackFoundException
import pl.lambada.songsync.util.networking.Ktor.client
import pl.lambada.songsync.util.networking.Ktor.json
import java.io.FileNotFoundException
import java.net.URLEncoder
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

@Serializable
data class SecretData(
    val secret: List<Int>,
    val version: Int
)

class SpotifyAPI {
    private val webPlayerURL = "https://open.spotify.com/"
    private val baseURL = "https://api.spotify.com/v1"

    // TOTP variables
    private var totpSecret: ByteArray? = null
    private var totpVer: Int = 0
    private var totpGenerator: TimeBasedOneTimePasswordGenerator? = null

    // Request headers
    private val reqHeaders = mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36",
        "Origin" to "https://open.spotify.com",
        "Referer" to "https://open.spotify.com/",
    )

    // Token data
    private var spotifyToken = ""
    private var tokenTime: Long = 0

    /**
     * Fetches secret data from GitHub and initializes TOTP
     */
    private suspend fun initializeTOTP() {
        if (totpGenerator != null) return

        try {
            val response = client.get("https://github.com/Thereallo1026/spotify-secrets/blob/main/secrets/secretBytes.json?raw=true")
            val responseBody = response.bodyAsText(Charsets.UTF_8)
            val secretDataList = json.decodeFromString<List<SecretData>>(responseBody)
            
            // Get the last element
            val gitu = secretDataList.last()
            
            totpSecret = toSecret(gitu.secret)
            totpVer = gitu.version
            
            totpGenerator = TimeBasedOneTimePasswordGenerator(
                totpSecret!!,
                TimeBasedOneTimePasswordConfig(
                    30L,
                    TimeUnit.SECONDS,
                    6,
                    HmacAlgorithm.SHA1
                )
            )
            
            Log.d("SpotifyAPI", "TOTP initialized with version: $totpVer")
        } catch (e: Exception) {
            Log.e("SpotifyAPI", "Failed to initialize TOTP", e)
            throw e
        }
    }

    /**
     * Converts secret data to ByteArray
     */
    private fun toSecret(data: List<Int>): ByteArray {
        val mappedData = data.mapIndexed { index, value -> 
            value xor ((index % 33) + 9)
        }
        
        val dataString = mappedData.joinToString("")
        val hexData = dataString.toByteArray(StandardCharsets.UTF_8)
            .joinToString("") { "%02x".format(it) }
        
        return hexData.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    /**
     * Gets the server time from the Spotify API.
     * @return The server time in milliseconds.
     */
    private suspend fun getServerTime(): Long {
        val response = client.get(
            webPlayerURL + "api/server-time"
        ) {
            reqHeaders.forEach { (key, value) -> header(key, value) }
        }
        val body = response.bodyAsText(Charsets.UTF_8)
        return json.decodeFromString<ServerTimeResponse>(body).serverTime * 1000
    }

    /**
     * Generates a TOTP code using the server time.
     * @return A Pair containing the timestamp and the TOTP code.
     */
    private suspend fun getTsAndTOTP(): Pair<Long, String> {
        if (totpGenerator == null) {
            initializeTOTP()
        }
        
        val serverTime = getServerTime()
        return Pair(serverTime, totpGenerator!!.generate(serverTime))
    }

    /**
     * Refreshes the access token by sending a request to the Spotify API.
     * @param force If true, forces a token refresh even if the current token is still valid.
     */
    suspend fun refreshToken(force: Boolean = false) {
        if (force || spotifyToken == "") {
            val totp = getTsAndTOTP()
            val response = client.get(
                webPlayerURL + "api/token"
            ) {
                reqHeaders.forEach { (key, value) -> header(key, value) }
                parameter("reason", "init")
                parameter("productType", "mobile-web-player")
                parameter("ts", totp.first)
                parameter("totp", totp.second)
                parameter("totpVer", totpVer) // Use dynamic version
            }
            val responseBody = response.bodyAsText(Charsets.UTF_8)
            val json = json.decodeFromString<WebPlayerTokenResponse>(responseBody)

            this.spotifyToken = json.accessToken
            this.tokenTime = System.currentTimeMillis()
        }
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
            "$baseURL/search?q=$search&type=track&limit=1&offset=$offset"
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
