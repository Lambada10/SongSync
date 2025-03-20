package pl.lambada.songsync.domain.model.lyrics_providers.spotify

import kotlinx.serialization.Serializable

@Serializable
data class ServerTimeResponse(
    val serverTime: Long,
)

@Serializable
data class WebPlayerTokenResponse(
    val clientId: String,
    val accessToken: String,
    val accessTokenExpirationTimestampMs: Long,
    val isAnonymous: Boolean,
)
