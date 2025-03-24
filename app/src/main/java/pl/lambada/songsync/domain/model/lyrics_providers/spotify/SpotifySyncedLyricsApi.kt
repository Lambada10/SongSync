package pl.lambada.songsync.domain.model.lyrics_providers.spotify

import kotlinx.serialization.Serializable

@Serializable
data class SyncedLinesResponse(
    val title: String,
    val artist: String,
    val cover: String,
    val lyrics: String
)
