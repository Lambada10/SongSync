package pl.lambada.songsync.data.api

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import pl.lambada.songsync.data.EmptyQueryException
import pl.lambada.songsync.data.dto.LRCLibResponse
import pl.lambada.songsync.data.dto.SongInfo
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class LRCLibAPI {
    private val baseURL = "https://lrclib.net/api/"
    private val jsonDec = Json { ignoreUnknownKeys = true }

    /**
     * Searches for synced lyrics using the song name and artist name.
     * @param query The SongInfo object with songName and artistName fields filled.
     * @return Search results as a list of LRCLibResponse objects.
     */
    suspend fun getSongInfo(query: SongInfo): SongInfo? {
        val search = URLEncoder.encode(
            "${query.songName}", // it doesn't work with artist name and song name together
            StandardCharsets.UTF_8.toString()
        )

        if (search == "")
            throw EmptyQueryException()

        val client = HttpClient(CIO)
        val response = client.get(
            baseURL + "search?q=$search"
        )
        val responseBody = response.bodyAsText(Charsets.UTF_8)
        client.close()

        if (responseBody == "[]" || response.status.value !in 200..299)
            return null

        val json = jsonDec.decodeFromString<List<LRCLibResponse>>(responseBody)

        return SongInfo(
            songName = json[0].trackName,
            artistName = json[0].artistName,
            id = json[0].id
        )
    }

    /**
     * Searches for synced lyrics using the song name and artist name.
     * @param id The ID of the song from search results.
     * @return The synced lyrics as a string.
     */
    suspend fun getSyncedLyrics(id: Int): String? {
        val client = HttpClient(CIO)
        val response = client.get(
            baseURL + "get/$id"
        )
        val responseBody = response.bodyAsText(Charsets.UTF_8)
        client.close()

        if (response.status.value !in 200..299 || responseBody == "[]")
            return null

        val json = jsonDec.decodeFromString<LRCLibResponse>(responseBody)
        Log.e("LRCLibAPI", json.syncedLyrics.toString())
        return json.syncedLyrics
    }
}