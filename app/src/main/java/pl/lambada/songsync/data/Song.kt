package pl.lambada.songsync.data

import android.net.Uri
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Song(
    val id: Long?,
    val title: String? = "Unknown",
    val artist: String? = "Unknown",
    @Serializable(UriSerializer::class) val imgUri: Uri?,
    val filePath: String?,
    val fileName: String?
)

//Uri serializer
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