package pl.lambada.songsync.domain.model

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
@Suppress("SpellCheckingInspection")
@Parcelize
data class SongInfo(
    var songName: String?,
    var artistName: String? = null,
    var songLink: String? = null,
    var albumCoverLink: String? = null,
    var lrcLibID: Int? = null, // LRCLib-only
    var neteaseID: Long? = null, // Netease-only
    var appleID: Long? = null, // Apple-only
) : Parcelable