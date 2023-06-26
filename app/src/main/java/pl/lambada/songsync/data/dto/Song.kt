package pl.lambada.songsync.data.dto

import android.net.Uri
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

/**
 * Data class for representing a song.
 * @param title The title of the song.
 * @param artist The artist of the song.
 * @param imgUri The URI of the image.
 * @param filePath The file path of the song.
 */
@Serializable
data class Song(
    val title: String? = "Unknown",
    val artist: String? = "Unknown",
    @Serializable(with = UriSerializer::class) val imgUri: Uri?,
    val filePath: String?
)

/**
 * Serializer for Uri.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Uri::class)
object UriSerializer : KSerializer<Uri> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Uri", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Uri) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Uri {
        return Uri.parse(decoder.decodeString())
    }
}

/**
 * Saver for Song.
 * Used for saving song info with rememberSaveable.
 */
object SongSaver : Saver<Song, String> {
    override fun restore(value: String): Song {
        return Json.decodeFromString(value)
    }

    override fun SaverScope.save(value: Song): String {
        return Json.encodeToString(value)
    }
}

/**
 * Saver for List<Song>.
 * Used for saving song list with rememberSaveable.
 */
object SongListSaver : Saver<List<Song>, String> {
    override fun restore(value: String): List<Song> {
        return Json.decodeFromString(ListSerializer(Song.serializer()), value)
    }

    override fun SaverScope.save(value: List<Song>): String {
        return Json.encodeToString(ListSerializer(Song.serializer()), value)
    }
}
