package pl.lambada.songsync.domain.model.lyrics_providers

import kotlinx.serialization.Serializable

@Serializable
data class PaxResponse(
    val type: String,
    val content: List<PaxLyrics>?
)

@Serializable
data class PaxLyrics(
    val text: List<PaxLyricsLineDetails>,
    val timestamp: Int,
    val oppositeTurn: Boolean,
    val background: Boolean,
    val backgroundText: List<PaxLyricsLineDetails>,
    val endtime: Int
)

@Serializable
data class PaxLyricsLineDetails(
    val text: String,
    val part: Boolean,
    val timestamp: Int?,
    val endtime: Int?
)