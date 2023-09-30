package pl.lambada.songsync.data.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class for storing song information.
 * Used for both local and remote songs.
 * The only difference is that local songs have songLink set to null.
 * @param songName The name of the song.
 * @param artistName The name of the artist.
 * @param songLink The link to the song.
 * @param albumCoverLink The link to the album cover.
 */
@Parcelize
data class SongInfo(
    var songName: String?,
    var artistName: String? = null,
    var songLink: String? = null, // Spotify-only
    var albumCoverLink: String? = null, // Spotify-only
    var id: Int? = null, // LRCLib-only
) : Parcelable