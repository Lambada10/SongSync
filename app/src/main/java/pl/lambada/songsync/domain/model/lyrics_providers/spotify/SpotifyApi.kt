import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrackSearchResult(
    val data: Data,
    val extensions: Extensions
)

@Serializable
data class Data(
    val searchV2: SearchV2
)

@Serializable
data class SearchV2(
    val query: String,
    val tracksV2: TracksV2
)

@Serializable
data class TracksV2(
    val totalCount: Int,
    val items: List<TrackItem>,
    val pagingInfo: PagingInfo
)

@Serializable
data class TrackItem(
    val matchedFields: List<String>,
    val item: Item
)

@Serializable
data class Item(
    val data: TrackData
)

@Serializable
data class TrackData(
    @SerialName("__typename")
    val typename: String,
    val uri: String,
    val id: String,
    val name: String,
    val albumOfTrack: AlbumOfTrack,
    val artists: Artists,
    val contentRating: ContentRating,
    val duration: Duration,
    val playability: Playability
)

@Serializable
data class AlbumOfTrack(
    val uri: String,
    val name: String,
    val coverArt: CoverArt,
    val id: String
)

@Serializable
data class CoverArt(
    val sources: List<ImageSource>,
    val extractedColors: ExtractedColors
)

@Serializable
data class ImageSource(
    val url: String,
    val width: Int,
    val height: Int
)

@Serializable
data class ExtractedColors(
    val colorDark: ColorDark
)

@Serializable
data class ColorDark(
    val hex: String,
    val isFallback: Boolean
)

@Serializable
data class Artists(
    val items: List<ArtistItem>
)

@Serializable
data class ArtistItem(
    val uri: String,
    val profile: Profile
)

@Serializable
data class Profile(
    val name: String
)

@Serializable
data class ContentRating(
    val label: String
)

@Serializable
data class Duration(
    val totalMilliseconds: Int
)

@Serializable
data class Playability(
    val playable: Boolean
)

@Serializable
data class PagingInfo(
    val nextOffset: Int,
    val limit: Int
)

@Serializable
data class Extensions(
    val requestIds: RequestIds,
    val cacheControl: CacheControl
)

@Serializable
data class RequestIds(
    @SerialName("/searchV2")
    val searchV2: SearchV2RequestId
)

@Serializable
data class SearchV2RequestId(
    @SerialName("search-api")
    val searchApi: String
)

@Serializable
data class CacheControl(
    val version: Int,
    val hints: List<String> = emptyList()
)
