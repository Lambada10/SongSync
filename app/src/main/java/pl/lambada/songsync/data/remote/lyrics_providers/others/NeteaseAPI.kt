package pl.lambada.songsync.data.remote.lyrics_providers.others

import android.util.Log
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import pl.lambada.songsync.data.EmptyQueryException
import pl.lambada.songsync.data.InternalErrorException
import pl.lambada.songsync.domain.model.SongInfo
import pl.lambada.songsync.domain.model.lyrics_providers.others.NeteaseLyricsResponse
import pl.lambada.songsync.domain.model.lyrics_providers.others.NeteaseResponse
import pl.lambada.songsync.util.networking.Ktor.client
import pl.lambada.songsync.util.networking.Ktor.json
import java.net.URLEncoder

class NeteaseAPI {
    private val baseURL = "http://music.163.com/api/"

    // stolen from github https://github.com/0x7d4/syncedlyrics/blob/ab744c9ebb96d310861364142ef95706c36a6b1a/syncedlyrics/providers/netease.py#L19C19-L19C19
    private val reqHeaders = mapOf(
        "Accept" to "application/json",
        "Accept-Language" to "en-US,en;q=0.9,fa;q=0.8",
        "Cache-Control" to "max-age=0",
        "Sec-ch-ua" to "\".Not/A)Brand\";v=\"99\", \"Google Chrome\";v=\"103\", \"Chromium\";v=\"103\"",
        "Sec-ch-ua-mobile" to "?0",
        "Sec-ch-ua-platform" to "\"Windows\"",
        "Sec-Fetch-Dest" to "document",
        "Sec-Fetch-Mode" to "navigate",
        "Sec-Fetch-Site" to "none",
        "Sec-Fetch-User" to "?1",
        "Upgrade-Insecure-Requests" to "1",
        "Cookie" to "NMTID=00OAVK3xqDG726ITU6jopU6jF2yMk0AAAGCO8l1BA; JSESSIONID-WYYY=8KQo11YK2GZP45RMlz8Kn80vHZ9%2FGvwzRKQXXy0iQoFKycWdBlQjbfT0MJrFa6hwRfmpfBYKeHliUPH287JC3hNW99WQjrh9b9RmKT%2Fg1Exc2VwHZcsqi7ITxQgfEiee50po28x5xTTZXKoP%2FRMctN2jpDeg57kdZrXz%2FD%2FWghb%5C4DuZ%3A1659124633932; _iuqxldmzr_=32; _ntes_nnid=0db6667097883aa9596ecfe7f188c3ec,1659122833973; _ntes_nuid=0db6667097883aa9596ecfe7f188c3ec; WNMCID=xygast.1659122837568.01.0; WEVNSM=1.0.0; WM_NI=CwbjWAFbcIzPX3dsLP%2F52VB%2Bxr572gmqAYwvN9KU5X5f1nRzBYl0SNf%2BV9FTmmYZy%2FoJLADaZS0Q8TrKfNSBNOt0HLB8rRJh9DsvMOT7%2BCGCQLbvlWAcJBJeXb1P8yZ3RHA%3D; WM_NIKE=9ca17ae2e6ffcda170e2e6ee90c65b85ae87b9aa5483ef8ab3d14a939e9a83c459959caeadce47e991fbaee82af0fea7c3b92a81a9ae8bd64b86beadaaf95c9cedac94cf5cedebfeb7c121bcaefbd8b16dafaf8fbaf67e8ee785b6b854f7baff8fd1728287a4d1d246a6f59adac560afb397bbfc25ad9684a2c76b9a8d00b2bb60b295aaafd24a8e91bcd1cb4882e8beb3c964fb9cbd97d04598e9e5a4c6499394ae97ef5d83bd86a3c96f9cbeffb1bb739aed9ea9c437e2a3; WM_TID=AAkRFnl03RdABEBEQFOBWHCPOeMra4IL; playerid=94262567",
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36",
    )


    /**
     * Searches for synced lyrics using the song name and artist name.
     * @param query The SongInfo object with songName and artistName fields filled.
     * @return Search result as a SongInfo object.
     */
    @OptIn(ExperimentalSerializationApi::class)
    suspend fun getSongInfo(query: SongInfo, offset: Int? = 0): SongInfo? {
        val search = "${query.songName} ${query.artistName}"

        if (search == " ")
            throw EmptyQueryException()

        val response = client.get(
            baseURL + "search/pc"
        ) {
            reqHeaders.forEach {
                header(it.key, it.value)
            }
            parameter("limit", 1)
            parameter("type", 1)
            parameter("offset", offset)
            parameter("s", search)
        }
        val responseBody = response.bodyAsText(Charsets.UTF_8)

        if (responseBody == "[]" || response.status.value !in 200..299 || responseBody.contains("\"songCount\":0"))
            return null

        val neteaseResponse: NeteaseResponse
        try {
            neteaseResponse = json.decodeFromString<NeteaseResponse>(responseBody)
        } catch (e: kotlinx.serialization.MissingFieldException) {
            throw InternalErrorException(Log.getStackTraceString(e))
        }

        val artists = neteaseResponse.result.songs[0].artists.joinToString(", ") { it.name }

        return SongInfo(
            songName = neteaseResponse.result.songs[0].name,
            artistName = artists,
            neteaseID = neteaseResponse.result.songs[0].id
        )
    }

    /**
     * Searches for synced lyrics using the song name and artist name.
     * @param id The ID of the song from search results.
     * @return The synced lyrics as a string.
     */
    suspend fun getSyncedLyrics(id: Long): String? {
        val response = client.get(
            baseURL + "song/lyric"
        ) {
            reqHeaders.forEach {
                header(it.key, it.value)
            }
            parameter("id", id)
            parameter("lv", 1)
        }
        val responseBody = response.bodyAsText(Charsets.UTF_8)

        if (response.status.value !in 200..299 || responseBody == "[]")
            return null

        val json = json.decodeFromString<NeteaseLyricsResponse>(responseBody)
        return if (json.lrc.lyric != "") json.lrc.lyric else null
    }
}