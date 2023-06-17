package pl.lambada.songsync.data

import android.net.Uri

data class Song(
    val id: Long?,
    val title: String?,
    val artist: String?,
    val imgUri: Uri?,
    val filePath: String?,
    val fileName: String?
)