package pl.lambada.songsync.data.remote.lyrics_providers.others

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.lambada.songsync.domain.model.SongInfo
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
    suspend fun getSongInfo(query: SongInfo): SongInfo? {
        val artistName = withContext(Dispatchers.IO) {
            URLEncoder.encode(
                "${query.artistName}",
                Charsets.UTF_8.toString()
            )
        }

        val songName = withContext(Dispatchers.IO) {
            URLEncoder.encode(
                "${query.songName}",
                Charsets.UTF_8.toString()
            )
        }

        if (artistName == "" || songName == "")
            throw EmptyQueryException()

        val response = client.get(
            "$baseURL/full?artist=$artistName&track=$songName"
        )
        val responseBody = response.bodyAsText(Charsets.UTF_8)

        if (response.status.value !in 200..299)
            return null

        val result = json.decodeFromString<MusixmatchSearchResponse>(responseBody)
        
        return SongInfo(
            songName = result.songName,
            artistName = result.artistName,
            songLink = result.url,
            albumCoverLink = result.artwork,
            musixmatchID = result.id,
            hasSyncedLyrics = result.hasSyncedLyrics,
            hasUnsyncedLyrics = result.hasSyncedLyrics,
            syncedLyrics = result.syncedLyrics?.lyrics,
            unsyncedLyrics = result.unsyncedLyrics?.lyrics
        )
    }

    /**
     * Returns the synced lyrics.
     * @param songInfo The SongInfo of the song from search results.
     * @return The synced lyrics as a string or null if the lyrics were not found.
     */
    fun getSyncedLyrics(songInfo: SongInfo?): String? {
        return songInfo?.syncedLyrics
    }
}