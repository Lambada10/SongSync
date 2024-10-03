package pl.lambada.songsync.domain.model.lyrics_providers.others

import kotlinx.serialization.Serializable

@Serializable
data class MusixmatchSearchResponse(
    val id: Long,
    val songName: String,
    val artistName: String,
    val albumName: String,
    val artwork: String,
    val releaseDate: String,
    val duration: Int,
    val url: String,
    val albumId: Long,
    val hasLyrics: Boolean
)

@Serializable
data class MusixmatchLyricsResponse(
    val id: Long,
    val duration: Int,
    val language: String,
    val updatedTime: String,
    val lyrics: String,
)