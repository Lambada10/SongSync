package pl.lambada.songsync.data.dto

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/*
Data class for storing song information
Used both for local and remote songs,
the only difference is that local songs have songLink set to null
 */
@Serializable
data class SongInfo(
    var songName: String? = null,
    var artistName: String? = null,
    var songLink: String? = null,
    var albumCoverLink: String? = null,
)

//SongInfo saver
//Used for saving song info with rememberSaveable
object SongInfoSaver: Saver<SongInfo, String> {
    override fun restore(value: String): SongInfo {
        return Json.decodeFromString(value)
    }

    override fun SaverScope.save(value: SongInfo): String {
        return Json.encodeToString(value)
    }

}
