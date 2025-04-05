package pl.lambada.songsync.data.remote

import pl.lambada.songsync.domain.model.lyrics_providers.PaxLyrics
import pl.lambada.songsync.domain.model.lyrics_providers.PaxResponse
import pl.lambada.songsync.util.ext.toLrcTimestamp
import pl.lambada.songsync.util.networking.Ktor.json

class PaxMusicHelper {
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

    private fun formatLineLyrics(lyrics: List<PaxLyrics>): String {
        val syncedLyrics = StringBuilder()
        for (line in lyrics) {
            syncedLyrics.append("[${line.timestamp.toLrcTimestamp()}]${line.text[0].text}\n")
        }
        return syncedLyrics.toString()
    }

    fun formatWordByWordLyrics(apiResponse: String, multiPersonWordByWord: Boolean): String? {
        try {
            val data = json.decodeFromString<PaxResponse>(apiResponse)
            if (data.content!!.isEmpty())
                return null

            val lines = data.content

            return when (data.type) {
                "Syllable" -> {
                    formatSyllableLyrics(lines, multiPersonWordByWord).dropLast(1)
                }

                "Line" -> {
                    formatLineLyrics(lines).dropLast(1)
                }

                else -> null
            }
        } catch (e: Exception) {
            val data = json.decodeFromString<List<PaxLyrics>>(apiResponse)
            return formatSyllableLyrics(data, multiPersonWordByWord)
        }
    }
}