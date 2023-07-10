package pl.lambada.songsync.data.dto

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class for representing a song.
 * @param title The title of the song.
 * @param artist The artist of the song.
 * @param imgUri The URI of the image.
 * @param filePath The file path of the song.
 */
@Parcelize
data class Song(
    val title: String?,
    val artist: String?,
    val imgUri: Uri?,
    val filePath: String?
) : Parcelable