package pl.lambada.songsync.data.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Release(
    val htmlURL: String? = null,
    val tagName: String? = null,
    val changelog: String? = null
) : Parcelable
