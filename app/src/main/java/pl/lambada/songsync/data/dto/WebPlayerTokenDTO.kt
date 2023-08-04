package pl.lambada.songsync.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class WebPlayerTokenResponse(
    val clientId: String,
    val accessToken: String,
    val accessTokenExpirationTimestampMs: Long,
    val isAnonymous: Boolean,
)
