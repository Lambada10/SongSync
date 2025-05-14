package pl.lambada.songsync.data.remote

import pl.lambada.songsync.domain.model.lyrics_providers.PaxLyrics
import pl.lambada.songsync.domain.model.lyrics_providers.PaxResponse
import pl.lambada.songsync.util.ext.toLrcTimestamp
import pl.lambada.songsync.util.networking.Ktor.json

class PaxMusicHelper {

    /**
     * Formats syllable lyrics into LRC format.
     * @param lyrics The list of PaxLyrics objects.
     * @param multiPersonWordByWord Flag to format lyrics for multiple persons word by word.
     * @return The formatted lyrics as a string.
     */
    private fun formatSyllableLyrics(lyrics: List<PaxLyrics>, multiPersonWordByWord: Boolean): String {
        val syncedLyrics = StringBuilder()
        for (line in lyrics) {
            syncedLyrics.append("[${line.timestamp.toLrcTimestamp()}]")

            if (multiPersonWordByWord) {
                syncedLyrics.append(
                    if (line.oppositeTurn) "v2:" else "v1:"
                )
            }

            for (syllable in line.text) {
                val formatedBeginTimestamp = "<${syllable.timestamp!!.toLrcTimestamp()}>"
                val formatedEndTimestamp = "<${syllable.endtime?.toLrcTimestamp()}>"
                if (!syncedLyrics.endsWith(formatedBeginTimestamp))
                    syncedLyrics.append(formatedBeginTimestamp)
                syncedLyrics.append(syllable.text)
                if (!syllable.part)
                    syncedLyrics.append(" ")
                syncedLyrics.append(formatedEndTimestamp)
            }

            if (line.background && multiPersonWordByWord) {
                syncedLyrics.append("\n[bg:")
                for (syllable in line.backgroundText) {
                    val formatedBeginTimestamp = "<${syllable.timestamp!!.toLrcTimestamp()}>"
                    val formatedEndTimestamp = "<${syllable.endtime?.toLrcTimestamp()}>"
                    if (!syncedLyrics.endsWith(formatedBeginTimestamp))
                        syncedLyrics.append(formatedBeginTimestamp)
                    syncedLyrics.append(syllable.text)
                    if (!syllable.part)
                        syncedLyrics.append(" ")
                    syncedLyrics.append(formatedEndTimestamp)
                }
                syncedLyrics.append("]")
            }
            syncedLyrics.append("\n")
        }
        return syncedLyrics.toString()
    }

    /**
     * Formats line lyrics into LRC format.
     * @param lyrics The list of PaxLyrics objects.
     * @return The formatted lyrics as a string.
     */
    private fun formatLineLyrics(lyrics: List<PaxLyrics>): String {
        val syncedLyrics = StringBuilder()
        for (line in lyrics) {
            syncedLyrics.append("[${line.timestamp.toLrcTimestamp()}]${line.text[0].text}\n")
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
            if (data.content!!.isEmpty())
                return null

            val lines = data.content

            when (data.type) {
                "Syllable" -> formatSyllableLyrics(lines, multiPersonWordByWord).dropLast(1)
                "Line" -> formatLineLyrics(lines).dropLast(1)
                else -> null
            }
        } catch (e: Exception) {
            val data = json.decodeFromString<List<PaxLyrics>>(apiResponse)
            formatSyllableLyrics(data, multiPersonWordByWord)
        }
    }
}