package pl.lambada.songsync.data

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Data class for storing song information.
 * Used for both local and remote songs.
 * The only difference is that local songs have songLink set to null.
 * @param songName The name of the song.
 * @param artistName The name of the artist.
 * @param songLink The link to the song.
 * @param albumCoverLink The link to the album cover.
 */
@Serializable
data class SongInfo(
    var songName: String? = null,
    var artistName: String? = null,
    var songLink: String? = null,
    var albumCoverLink: String? = null,
)

/**
 * Saver for SongInfo.
 * Used for saving song info with rememberSaveable.
 */
object SongInfoSaver : Saver<SongInfo, String> {
    override fun restore(value: String): SongInfo {
        return Json.decodeFromString(value)
    }

    override fun SaverScope.save(value: SongInfo): String {
        return Json.encodeToString(value)
    }
}
