package pl.lambada.songsync.domain.model.lyrics_providers.others

import kotlinx.serialization.Serializable

@Serializable
data class AppleSearchResponse(
    val id: Long,
    val songName: String,
    val artistName: String,
    val albumName: String,
    val artwork: String,
    val releaseDate: String?,
    val duration: Int,
    val isrc: String,
    val url: String,
    val contentRating: String?,
    val albumId: String
)