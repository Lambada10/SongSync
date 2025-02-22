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
                syncedLyrics.append("<${syllable.timestamp!!.toLrcTimestamp()}>${syllable.text}")
                if (!syllable.part) {
                    syncedLyrics.append(" ")
                }
                syncedLyrics.append("<${syllable.endtime?.toLrcTimestamp()}>")
            }

            if (line.background && multiPersonWordByWord) {
                syncedLyrics.append("<${line.text.last().endtime?.toLrcTimestamp()}>\n")

                syncedLyrics.append("[bg:")

                for (syllable in line.backgroundText) {
                    syncedLyrics.append("<${syllable.timestamp!!.toLrcTimestamp()}>${syllable.text}")
                    if (!syllable.part) {
                        syncedLyrics.append(" ")
                    }
                    syncedLyrics.append("<${syllable.endtime?.toLrcTimestamp()}>")
                }

                syncedLyrics.append("<${line.endtime.toLrcTimestamp()}>]\n")
            } else {
                syncedLyrics.append("<${line.endtime.toLrcTimestamp()}>\n")
            }
        }
        return syncedLyrics.toString().dropLast(1)
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