package pl.lambada.songsync.data.remote.lyrics_providers.others

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.lambada.songsync.domain.model.SongInfo
import pl.lambada.songsync.domain.model.lyrics_providers.others.VocaDBLyricsItem
import pl.lambada.songsync.domain.model.lyrics_providers.others.VocaDBSearchResponse
import pl.lambada.songsync.domain.model.lyrics_providers.others.VocaDBSongWithLyrics
import pl.lambada.songsync.domain.model.lyrics_providers.others.VocaDBArtistSearchResponse
import pl.lambada.songsync.util.EmptyQueryException
import android.util.Log
import pl.lambada.songsync.util.networking.Ktor.client
import pl.lambada.songsync.util.networking.Ktor.json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class VocaDBAPI {
    private val baseURL = "https://vocadb.net/api/"

    suspend fun getSongInfo(query: SongInfo, offset: Int = 0): SongInfo? {
        var offsetVar = offset
        val artistIds = mutableListOf<Int>()

        val artistQuery = withContext(Dispatchers.IO) {
            URLEncoder.encode(query.artistName ?: "", StandardCharsets.UTF_8.toString())
        }
        if (artistQuery.isNotBlank()) {
            val artistURL = baseURL + "artists?query=$artistQuery&allowBaseVoicebanks=true&maxResults=10&preferAccurateMatches=false"
            val artistResponse = client.get(artistURL)
            val artistBody = artistResponse.bodyAsText(Charsets.UTF_8)

            if (artistResponse.status.value !in 200..299 || artistBody.isBlank()) {
                return null
            }

            val artistResp = json.decodeFromString<VocaDBArtistSearchResponse>(artistBody)
            artistResp.items.forEach { artistIds.add(it.id) }
        }

        val songsQuery = withContext(Dispatchers.IO) {
            URLEncoder.encode("${query.songName}", StandardCharsets.UTF_8.toString())
        }
        for (id in artistIds) {
            // i initially thought the artistID[] param was an or filter but instead it was an and filter
            val songsURL = baseURL + "songs?query=$songsQuery&start=$offset&maxResults=10&artistId%5B%5D=$id"
            val songsResponse = client.get(songsURL)
            val songsBody = songsResponse.bodyAsText(Charsets.UTF_8)

            if (songsResponse.status.value !in 200..299 || songsBody.isBlank()) {
                return null
            }

            // this is an interesting way to implement offset value
            // since we do more than one requests, we may not find it in our first request
            // so we just lower it until we find it
            // underflow shouldnt happen as if size is 0 offsetVar won't change and if size is
            // smaller than offsetVar it will still stay positive
            val songsResp = json.decodeFromString<VocaDBSearchResponse>(songsBody)
            if (songsResp.items.size > 0 && songsResp.items.size > offsetVar) {
                val item = songsResp.items[offsetVar]

                return SongInfo(
                    songName = item.name,
                    artistName = item.artistString,
                    vocadbID = item.id
                )
            }
            else {
                offsetVar -= songsResp.items.size as Int
            }
        }

        return null
    }

    suspend fun getSyncedLyrics(id: Int, includeTranslation: Boolean = false, includeRomanization: Boolean = false): String? {
        if (id <= 0) return null
        val lyricsURL = baseURL + "songs/$id?fields=Lyrics"
        val lyricsResponse = client.get(lyricsURL)
        val lyricsBody = lyricsResponse.bodyAsText(Charsets.UTF_8)

        if (lyricsResponse.status.value !in 200..299 || lyricsBody.isBlank()) {
            return null
        }

        val lyricsResp = json.decodeFromString<VocaDBSongWithLyrics>(lyricsBody)
        if (lyricsResp.lyrics.isEmpty()) return null

        val parts = mutableListOf<VocaDBLyricsItem>()

        val original = lyricsResp.lyrics.firstOrNull { it.translationType?.equals("Original", true) == true }
        original?.let { parts.add(it) }

        if (includeRomanization) {
            val romanization = lyricsResp.lyrics.firstOrNull { it.translationType?.contains("Romanized", true) == true }
            romanization?.let { parts.add(it) }
        }

        if (includeTranslation) {
            for (translation in lyricsResp.lyrics.filter { it.translationType?.contains("Translation", true) == true }) {
                translation?.let { parts.add(it) }
            }
        }

        if (parts.isEmpty()) {
            return null
        }

        // i tried separating line by line to merge original and romanization but since they are all from different sources,
        // the line endings are all over the place, which meant it wasn't very reliable, so i just scrapped that idea
        return parts.mapNotNull { "=== ${it.translationType ?: "Lyrics"} (${it.cultureCodes.joinToString(", ")}) ===\n${it.value}" }.joinToString("\n\n\n")
    }
}
