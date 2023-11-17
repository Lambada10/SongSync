package pl.lambada.songsync.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Release(
    @SerialName("html_url")
    val htmlURL: String,
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("body")
    val changelog: String? = null
) : Parcelable
