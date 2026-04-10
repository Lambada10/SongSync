package pl.lambada.songsync.data.remote.lyrics_providers.spotify

import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import pl.lambada.songsync.domain.model.lyrics_providers.spotify.SyncedLinesResponse
import pl.lambada.songsync.util.networking.Ktor.client
import pl.lambada.songsync.util.networking.Ktor.json

class SpotifyLyricsAPI {
    private val baseURL = "https://lyrics.paxsenix.org/spotify/lyrics"

    /**
     * Gets synced lyrics using the song link and returns them as a string formatted as an LRC file.
     * @param track_url The spotify url to track.
     * @return The synced lyrics as a string.
     */
    suspend fun getSyncedLyrics(track_url: String): String? {
        val trackId = track_url.substringAfterLast("track/").substringBefore("?")
        val response = client.get(baseURL) {
            parameter("id", trackId)
        }
        val responseBody = response.bodyAsText(Charsets.UTF_8)
        if (response.status.value !in 200..299)
            return null

        val json = json.decodeFromString<SyncedLinesResponse>(responseBody)

        if (json.lyrics == "Not Found.")
            return null

        return json.lyrics
    }
}
