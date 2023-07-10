package pl.lambada.songsync.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubReleaseResponse(
    @SerialName("html_url")
    val htmlURL: String,
    @SerialName("tag_name")
    val tagName: String,
    val body: String
)