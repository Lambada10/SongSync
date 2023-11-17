package pl.lambada.songsync.data.remote.lyrics_providers.spotify

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import pl.lambada.songsync.domain.model.lyrics_providers.spotify.SyncedLinesResponse

class SpotifyLyricsAPI {
    private val baseURL = "https://spotify-lyric-api-984e7b4face0.herokuapp.com/"
    private val jsonDec = Json { ignoreUnknownKeys = true }

    /**
     * Gets synced lyrics using the song link and returns them as a string formatted as an LRC file.
     * @param songLink The link to the song.
     * @return The synced lyrics as a string.
     */
    suspend fun getSyncedLyrics(songLink: String): String? {
        val client = HttpClient(CIO)
        val response = client.get(baseURL + "?url=$songLink&format=lrc")
        val responseBody = response.bodyAsText(Charsets.UTF_8)
        client.close()

        if (response.status.value !in 200..299)
            return null

        val json = jsonDec.decodeFromString<SyncedLinesResponse>(responseBody)

        if (json.error)
            return null

        val lines = json.lines
        val syncedLyrics = StringBuilder()

        for (line in lines) {
            syncedLyrics.append("[${line.timeTag}]${line.words}\n")
        }

        return syncedLyrics.toString().dropLast(1)
    }
}