package pl.lambada.songsync.data.remote.lyrics_providers.others

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.lambada.songsync.domain.model.SongInfo
import pl.lambada.songsync.domain.model.lyrics_providers.others.MusixmatchLyricsResponse
import pl.lambada.songsync.domain.model.lyrics_providers.others.MusixmatchSearchResponse
import pl.lambada.songsync.util.EmptyQueryException
import pl.lambada.songsync.util.networking.Ktor.client
import pl.lambada.songsync.util.networking.Ktor.json
import java.net.URLEncoder

class MusixmatchAPI {
    private val baseURL = "https://kerollosy.vercel.app"

    /**
     * Searches for synced lyrics using the song name and artist name.
     * @param query The SongInfo object with songName and artistName fields filled.
     * @return Search result as a SongInfo object.
     */
    suspend fun getSongInfo(query: SongInfo, offset: Int = 0): SongInfo? {
        val search = withContext(Dispatchers.IO) {
            URLEncoder.encode(
                "${query.songName} ${query.artistName}",
                Charsets.UTF_8.toString()
            )
        }

        if (search == " ")
            throw EmptyQueryException()

        val response = client.get(
            "$baseURL/search?q=$search"
        )
        val responseBody = response.bodyAsText(Charsets.UTF_8)

        if (response.status.value !in 200..299 || responseBody == "[]")
            return null

        val json = json.decodeFromString<List<MusixmatchSearchResponse>>(responseBody)

        val result = try {
            json[offset]
        } catch (e: IndexOutOfBoundsException) {
            return null
        }

        return SongInfo(
            songName = result.songName,
            artistName = result.artistName,
            songLink = result.url,
            albumCoverLink = result.artwork,
            musixmatchID = result.id,
        )
    }

    /**
     * Searches for synced lyrics using the song name and artist name.
     * @param id The ID of the song from search results.
     * @return The synced lyrics as a string.
     */
    suspend fun getSyncedLyrics(id: Long): String? {
        val response = client.get(
            "$baseURL/lyrics?id=$id"
        )
        val responseBody = response.bodyAsText(Charsets.UTF_8)

        if (response.status.value !in 200..299)
            return null

        val json = json.decodeFromString<MusixmatchLyricsResponse>(responseBody)
        return json.lyrics
    }
}