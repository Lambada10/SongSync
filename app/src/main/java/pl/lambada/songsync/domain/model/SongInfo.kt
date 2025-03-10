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
 * @param lrcLibID The ID for LRCLib.
 * @param qqPayload The payload for QQMusic.
 * @param neteaseID The ID for Netease.
 * @param appleID The ID for Apple Music.
 * @param musixmatchID The ID for Musixmatch.
 * @param hasSyncedLyrics Flag indicating if the song has synced lyrics (Musixmatch-only).
 * @param hasUnsyncedLyrics Flag indicating if the song has unsynced lyrics (Musixmatch-only).
 * @param syncedLyrics The synced lyrics (Musixmatch-only).
 * @param unsyncedLyrics The unsynced lyrics (Musixmatch-only).
 */
@Suppress("SpellCheckingInspection")
@Parcelize
data class SongInfo(
    var songName: String?,
    var artistName: String? = null,
    var songLink: String? = null,
    var albumCoverLink: String? = null,
    var lrcLibID: Int? = null, // LRCLib-only
    var qqPayload: String? = null, // QQMusic-only
    var neteaseID: Long? = null, // Netease-only
    var appleID: Long? = null, // Apple-only
    var musixmatchID: Long? = null, // Musixmatch-only
    var hasSyncedLyrics: Boolean? = null, // Musixmatch-only
    var hasUnsyncedLyrics: Boolean? = null, // Musixmatch-only
    var syncedLyrics: String? = null, // Musixmatch-only
    var unsyncedLyrics: String? = null, // Musixmatch-only
) : Parcelable