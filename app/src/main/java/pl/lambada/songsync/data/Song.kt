package pl.lambada.songsync.data

import android.net.Uri

data class Song(
    val id: Long?,
    val title: String? = "Unknown",
    val artist: String? = "Unknown",
    val imgUri: Uri?,
    val filePath: String?,
    val fileName: String?
)