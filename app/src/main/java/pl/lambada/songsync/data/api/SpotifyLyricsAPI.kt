package pl.lambada.songsync.data.api

import kotlinx.serialization.json.Json
import pl.lambada.songsync.data.dto.SyncedLinesResponse
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class SpotifyLyricsAPI {
    private val baseURL = "https://spotify-lyric-api.herokuapp.com/"
    private val jsonDec = Json { ignoreUnknownKeys = true }

    /**
     * Gets synced lyrics using the song link and returns them as a string formatted as an LRC file.
     * @param songLink The link to the song.
     * @return The synced lyrics as a string.
     */
    fun getSyncedLyrics(songLink: String): String? {
        val url = URL(baseURL + "?url=$songLink&format=lrc")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)

        connection.disconnect()

        val json = jsonDec.decodeFromString<SyncedLinesResponse>(response)

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