package pl.lambada.songsync.domain.model.lyrics_providers.others

import kotlinx.serialization.Serializable

@Serializable
data class AppleSearchResponse(
    val id: Long,
    val songName: String,
    val artistName: String,
    val albumName: String,
    val artwork: String,
    val releaseDate: String,
    val duration: Int,
    val isrc: String,
    val url: String,
    val contentRating: String?,
    val albumId: String
)

@Serializable
data class AppleLyricsResponse(
    val type: String,
    val content: List<AppleLyrics>?
)

@Serializable
data class AppleLyrics(
    val text: List<AppleLyricsLineDetails>,
    val timestamp: Int,
    val oppositeTurn: Boolean,
    val background: Boolean,
    val backgroundText: List<AppleLyricsLineDetails>,
    val endtime: Int
)

@Serializable
data class AppleLyricsLineDetails(
    val text: String,
    val part: Boolean,
    val timestamp: Int?,
    val endtime: Int?
)