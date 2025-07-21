package pl.lambada.songsync.domain.model.lyrics_providers.spotify

import kotlinx.serialization.Serializable

@Serializable
data class SyncedLinesResponse(
    val lyrics: String,
    val isError: Boolean
)
