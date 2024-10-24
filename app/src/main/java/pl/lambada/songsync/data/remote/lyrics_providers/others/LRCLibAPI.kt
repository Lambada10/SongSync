package pl.lambada.songsync.data.remote.lyrics_providers.others

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.lambada.songsync.domain.model.SongInfo
import pl.lambada.songsync.domain.model.lyrics_providers.others.LRCLibResponse
import pl.lambada.songsync.util.EmptyQueryException
import pl.lambada.songsync.util.networking.Ktor.client
import pl.lambada.songsync.util.networking.Ktor.json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class LRCLibAPI {
    private val baseURL = "https://lrclib.net/api/"

    /**
     * Searches for synced lyrics using the song name and artist name.
     * @param query The SongInfo object with songName and artistName fields filled.
     * @return Search result as a SongInfo object.
     */
    suspend fun getSongInfo(query: SongInfo, offset: Int = 0): SongInfo? {
        val search = withContext(Dispatchers.IO) {
            URLEncoder.encode(
                "${query.songName}", // it doesn't work with artist name and song name together
                StandardCharsets.UTF_8.toString()
            )
        }

        if (search == "")
            throw EmptyQueryException()

        val response = client.get(
            baseURL + "search?q=$search"
        )
        val responseBody = response.bodyAsText(Charsets.UTF_8)

        if (responseBody == "[]" || response.status.value !in 200..299)
            return null

        val json = json.decodeFromString<List<LRCLibResponse>>(responseBody)

        val song = try {
            json[offset]
        } catch (e: IndexOutOfBoundsException) {
            return null
        }

        return SongInfo(
            songName = song.trackName,
            artistName = song.artistName,
            lrcLibID = song.id
        )
    }

    /**
     * Searches for synced lyrics using the song name and artist name.
     * @param id The ID of the song from search results.
     * @return The synced lyrics as a string.
     */
    suspend fun getSyncedLyrics(id: Int): String? {
        val response = client.get(
            baseURL + "get/$id"
        )
        val responseBody = response.bodyAsText(Charsets.UTF_8)

        if (response.status.value !in 200..299 || responseBody == "[]")
            return null

        val json = json.decodeFromString<LRCLibResponse>(responseBody)
        return json.syncedLyrics
    }
}