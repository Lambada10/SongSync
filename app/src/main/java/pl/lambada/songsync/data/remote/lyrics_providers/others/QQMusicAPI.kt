package pl.lambada.songsync.data.remote.lyrics_providers.others

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import pl.lambada.songsync.data.remote.PaxMusicHelper
import pl.lambada.songsync.domain.model.SongInfo
import pl.lambada.songsync.domain.model.lyrics_providers.others.PaxQQPayload
import pl.lambada.songsync.domain.model.lyrics_providers.others.QQMusicSearchResponse
import pl.lambada.songsync.util.EmptyQueryException
import pl.lambada.songsync.util.networking.Ktor.client
import pl.lambada.songsync.util.networking.Ktor.json

class QQMusicAPI {
    private val baseURL = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp"
    private val lyricsURL = "https://paxsenix.alwaysdata.net/getQQLyrics.php"

    private val reqHeaders = mapOf(
        "Content-Type" to "application/json",
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
    )

    private val reqParams = mapOf(
        "format" to "json",
        "inCharset" to "utf8",
        "outCharset" to "utf8",
        "platform" to "yqq.json"
    )

    /**
     * Searches for synced lyrics using the song name and artist name.
     * @param query The SongInfo object with songName and artistName fields filled.
     * @return Search result as a SongInfo object.
     */
    suspend fun getSongInfo(query: SongInfo, offset: Int = 0): SongInfo? {
        val search = "${query.songName} ${query.artistName}"

        if (search.isBlank())
            throw EmptyQueryException()

        val response = client.get(baseURL) {
            reqHeaders.forEach {
                header(it.key, it.value)
            }
            reqParams.forEach {
                parameter(it.key, it.value)
            }
            parameter("new_json", 1)
            parameter("w", search)
        }
        val responseBody = response.bodyAsText(Charsets.UTF_8)

        if (response.status.value !in 200..299)
            return null

        val result = json.decodeFromString<QQMusicSearchResponse>(responseBody)

        val song = try {
            result.data.song.list[offset]
        } catch (e: IndexOutOfBoundsException) {
            return null
        }

        val artists = song.singer.joinToString(", ") { it.name }

        return SongInfo(
            songName = song.title,
            artistName = artists,
            qqPayload = json.encodeToString(
                PaxQQPayload.serializer(),
                PaxQQPayload(
                    artist = song.singer.map { it.name },
                    album = song.album.name,
                    id = song.id,
                    title = song.title
                )
            )
        )
    }

    /**
     * Searches for synced lyrics using the song name and artist name.
     * @param payload The payload generated from search results.
     * @return The synced lyrics as a string.
     */
    suspend fun getSyncedLyrics(payload: String, multiPersonWordByWord: Boolean = false): String? {
        val response = client.post(lyricsURL) {
            setBody(payload)
        }
        val responseBody = response.bodyAsText(Charsets.UTF_8)

        return PaxMusicHelper().formatWordByWordLyrics(responseBody, multiPersonWordByWord)
    }
}