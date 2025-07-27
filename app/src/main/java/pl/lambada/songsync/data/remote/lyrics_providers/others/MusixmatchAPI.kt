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
    suspend fun getSongInfo(query: SongInfo, offset: Int = 0): SongInfo? {
        val artistName = withContext(Dispatchers.IO) {
            URLEncoder.encode(
                query.artistName,
                Charsets.UTF_8.toString()
            )
        }

        val songName = withContext(Dispatchers.IO) {
            URLEncoder.encode(
                query.songName,
                Charsets.UTF_8.toString()
            )
        }

        if (artistName.isBlank() || songName.isBlank())
            throw EmptyQueryException()

        val response = client.get(
            "$baseURL/v2/full?artist=$artistName&track=$songName"
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
            unsyncedLyrics = result.unsyncedLyrics?.lyrics,
            availableLanguages = result.availableLanguages,
            originalLanguage = result.originalLanguage,
            currentLanguage = result.originalLanguage ?: "en"
        )
    }

    /**
     * Gets lyrics in a specific language for a song.
     * @param songId The Musixmatch song ID.
     * @param language The language code (e.g., "es", "fr", "de").
     * @param preferSynced Whether to prefer synced lyrics over unsynced.
     * @return The lyrics in the requested language or null if not available.
     */
    suspend fun getLyricsInLanguage(
        songId: Long,
        language: String,
        preferSynced: Boolean = true
    ): String? {
        val response = client.get(
            "$baseURL/v2/full?id=$songId&lang=$language"
        )
        val responseBody = response.bodyAsText(Charsets.UTF_8)

        if (response.status.value !in 200..299)
            return null

        val result = json.decodeFromString<MusixmatchSearchResponse>(responseBody)

        return if (preferSynced && result.syncedLyrics != null) {
            result.syncedLyrics.lyrics
        } else {
            result.unsyncedLyrics?.lyrics
        }
    }

    /**
     * Returns the lyrics.
     * @param songInfo The SongInfo of the song from search results.
     * @param preferUnsynced Flag to prefer unsynced lyrics when synced lyrics are not available.
     * @return The lyrics as a string or null if the lyrics were not found.
     */
    fun getLyrics(songInfo: SongInfo?, preferUnsynced: Boolean = true): String? {
        return songInfo?.syncedLyrics ?: if (preferUnsynced) songInfo?.unsyncedLyrics else null
    }

    /**
     * Gets the display name for a language code.
     * @param languageCode The ISO language code.
     * @return The display name of the language.
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            "en" -> "English"
            "es" -> "Español"
            "fr" -> "Français"
            "de" -> "Deutsch"
            "it" -> "Italiano"
            "pt" -> "Português"
            "ru" -> "Русский"
            "ja" -> "日本語"
            "ko" -> "한국어"
            "zh" -> "中文"
            "ar" -> "العربية"
            "hi" -> "हिन्दी"
            "tr" -> "Türkçe"
            "pl" -> "Polski"
            "nl" -> "Nederlands"
            "sv" -> "Svenska"
            "da" -> "Dansk"
            "no" -> "Norsk"
            "cs" -> "Čeština"
            "sk" -> "Slovenčina"
            "hu" -> "Magyar"
            "ro" -> "Română"
            "bg" -> "Български"
            "hr" -> "Hrvatski"
            "sr" -> "Српски"
            "sl" -> "Slovenščina"
            "et" -> "Eesti"
            "lv" -> "Latviešu"
            "lt" -> "Lietuvių"
            "uk" -> "Українська"
            "he" -> "עברית"
            "th" -> "ไทย"
            "vi" -> "Tiếng Việt"
            "id" -> "Bahasa Indonesia"
            "ms" -> "Bahasa Melayu"
            "ta" -> "தமிழ்"
            "bn" -> "বাংলা"
            "fa" -> "فارسی"
            "uz" -> "O'zbek"
            "ky" -> "Кыргызча"
            "mn" -> "Монгол"
            "ka" -> "ქართული"
            "az" -> "Azərbaycan"
            "af" -> "Afrikaans"
            "is" -> "Íslenska"
            "mk" -> "Македонски"
            "bs" -> "Bosanski"
            "fi" -> "Finnish"
            "nb" -> "Norsk Bokmål"
            else -> languageCode.uppercase()
        }
    }
}