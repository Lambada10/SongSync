package pl.lambada.songsync.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AccessTokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int
)
