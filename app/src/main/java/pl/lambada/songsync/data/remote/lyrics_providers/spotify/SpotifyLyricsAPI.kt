package pl.lambada.songsync.data.remote.lyrics_providers.spotify

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import pl.lambada.songsync.domain.model.lyrics_providers.spotify.SyncedLinesResponse
import pl.lambada.songsync.util.networking.Ktor.client
import pl.lambada.songsync.util.networking.Ktor.json

class SpotifyLyricsAPI {
    private val baseURL = "https://spotify-lyrics-api-cyan.vercel.app/"

    /**
     * Gets synced lyrics using the song link and returns them as a string formatted as an LRC file.
     * @param songLink The link to the song.
     * @return The synced lyrics as a string.
     */
    suspend fun getSyncedLyrics(songLink: String): String? {
        val response = client.get("$baseURL?url=$songLink&format=lrc")
        val responseBody = response.bodyAsText(Charsets.UTF_8)

        if (response.status.value !in 200..299)
            return null

        val json = json.decodeFromString<SyncedLinesResponse>(responseBody)

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