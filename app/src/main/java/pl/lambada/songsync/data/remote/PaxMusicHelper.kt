package pl.lambada.songsync.data.remote

import pl.lambada.songsync.domain.model.lyrics_providers.PaxLyrics
import pl.lambada.songsync.domain.model.lyrics_providers.PaxResponse
import pl.lambada.songsync.util.ext.toLrcTimestamp
import pl.lambada.songsync.util.networking.Ktor.json

class PaxMusicHelper {

    // Regex to match:
    // 1. Voice tags: v1:, v2:, vl: (case-insensitive)
    // 2. Syllable timestamps: <00:00.00>
    // 3. Metadata tags in brackets that are NOT timestamps: [bg:], [ar:], etc.
    private val LYRICS_CLEANUP_REGEX = Regex("""(?i)v[l12]:|<[^>]+>|\[(?!\d{1,3}:\d{1,2}\.\d{1,3})[^\]]+\]""")

    private fun formatSyllableLyricsToString(lyrics: List<PaxLyrics>, multiPersonWordByWord: Boolean): String {
        val syncedLyrics = StringBuilder()
        for (line in lyrics) {
            syncedLyrics.append("[${line.timestamp.toLrcTimestamp()}]")

            if (multiPersonWordByWord) {
                syncedLyrics.append(
                    if (line.oppositeTurn) "v2:" else "v1:"
                )
            }

            for (syllable in line.text) {
                val cleanText = syllable.text.replace(LYRICS_CLEANUP_REGEX, "")
                if (multiPersonWordByWord) {
                    val formatedBeginTimestamp = "<${(syllable.timestamp ?: line.timestamp).toLrcTimestamp()}>"
                    val formatedEndThumbnail = "<${(syllable.endtime ?: (syllable.timestamp ?: line.timestamp)).toLrcTimestamp()}>"
                    
                    if (!syncedLyrics.endsWith(formatedBeginTimestamp))
                        syncedLyrics.append(formatedBeginTimestamp)
                    
                    syncedLyrics.append(cleanText)
                    
                    if (!syllable.part)
                        syncedLyrics.append(" ")
                        
                    syncedLyrics.append(formatedEndThumbnail)
                } else {
                    syncedLyrics.append(cleanText)
                    if (!syllable.part)
                        syncedLyrics.append(" ")
                }
            }

            if (line.background && multiPersonWordByWord) {
                syncedLyrics.append("\n[bg:")
                for (syllable in line.backgroundText) {
                    val formatedBeginTimestamp = "<${(syllable.timestamp ?: line.timestamp).toLrcTimestamp()}>"
                    val formatedEndThumbnail = "<${(syllable.endtime ?: (syllable.timestamp ?: line.timestamp)).toLrcTimestamp()}>"
                    
                    if (!syncedLyrics.endsWith(formatedBeginTimestamp))
                        syncedLyrics.append(formatedBeginTimestamp)
                        
                    syncedLyrics.append(syllable.text.replace(LYRICS_CLEANUP_REGEX, ""))
                    
                    if (!syllable.part)
                        syncedLyrics.append(" ")
                        
                    syncedLyrics.append(formatedEndThumbnail)
                }
                syncedLyrics.append("]")
            }
            syncedLyrics.append("\n")
        }
        
        return syncedLyrics.toString()
            .replace(LYRICS_CLEANUP_REGEX, "") // Final pass to catch anything built during formatting if word-by-word is off
            .replace(Regex(" {2,}"), " ")
            .trim()
    }

    /**
     * Formats line lyrics into LRC format.
     * @param lyrics The list of PaxLyrics objects.
     * @return The formatted lyrics as a string.
     */
    private fun formatLineLyrics(lyrics: List<PaxLyrics>): String {
        val syncedLyrics = StringBuilder()
        val lineTimestampRegex = Regex("""^\[\d+:\d+\.\d+]""")
        for (line in lyrics) {
            val originalText = line.text.firstOrNull()?.text ?: ""
            val cleanText = originalText
                .replace(lineTimestampRegex, "")
                .replace(LYRICS_CLEANUP_REGEX, "")
                .replace(Regex(" {2,}"), " ")
                .trim()
            syncedLyrics.append("[${line.timestamp.toLrcTimestamp()}]$cleanText\n")
        }
        return syncedLyrics.toString()
    }

    /**
     * Formats word-by-word lyrics into LRC format.
     * @param apiResponse The API response containing the lyrics.
     * @param multiPersonWordByWord Flag to format lyrics for multiple persons word by word.
     * @return The formatted lyrics as a string or null if the lyrics were not found.
     */
    fun formatWordByWordLyrics(apiResponse: String, multiPersonWordByWord: Boolean): String? {
        return try {
            val data = json.decodeFromString<PaxResponse>(apiResponse)
            val lines = data.content ?: return null
            if (lines.isEmpty()) return null

            val result = when (data.type) {
                "Syllable" -> formatSyllableLyricsToString(lines, multiPersonWordByWord).trim()
                "Line" -> formatLineLyrics(lines).trim()
                else -> null
            }
            
            result?.replace(Regex("(?m)^ +"), "")?.replace(Regex(" {2,}"), " ")
        } catch (e: Exception) {
            try {
                val data = json.decodeFromString<List<PaxLyrics>>(apiResponse)
                val result = formatSyllableLyricsToString(data, multiPersonWordByWord).trim()
                result.replace(Regex("(?m)^ +"), "")?.replace(Regex(" {2,}"), " ")
            } catch (e2: Exception) {
                null
            }
        }
    }
}