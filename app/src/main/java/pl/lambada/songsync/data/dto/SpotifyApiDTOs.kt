package pl.lambada.songsync.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrackSearchResult(
    val tracks: Tracks
)

@Serializable
data class Tracks(
    val href: String,
    val items: List<Track>,
    val limit: Int,
    val next: String?,
    val offset: Int,
    val previous: String?,
    val total: Int
)

@Serializable
data class Track(
    val album: Album,
    val artists: List<Artist>,
    @SerialName("available_markets")
    val availableMarkets: List<String>,
    @SerialName("disc_number")
    val discNumber: Int,
    @SerialName("duration_ms")
    val durationMs: Int,
    val explicit: Boolean,
    @SerialName("external_ids")
    val externalIds: ExternalIds,
    @SerialName("external_urls")
    val externalUrls: ExternalUrls,
    val href: String,
    val id: String,
    @SerialName("is_local")
    val isLocal: Boolean,
    val name: String,
    val popularity: Int,
    @SerialName("preview_url")
    val previewUrl: String?,
    @SerialName("track_number")
    val trackNumber: Int,
    val type: String,
    val uri: String
)

@Serializable
data class Album(
    @SerialName("album_type")
    val albumType: String,
    val artists: List<Artist>,
    @SerialName("available_markets")
    val availableMarkets: List<String>,
    @SerialName("external_urls")
    val externalUrls: ExternalUrls,
    val href: String,
    val id: String,
    val images: List<Image>,
    val name: String,
    @SerialName("release_date")
    val releaseDate: String,
    @SerialName("release_date_precision")
    val releaseDatePrecision: String,
    @SerialName("total_tracks")
    val totalTracks: Int,
    val type: String,
    val uri: String
)

@Serializable
data class Artist(
    val externalUrls: ExternalUrls? = null,
    val href: String,
    val id: String,
    val name: String,
    val type: String,
    val uri: String
)

@Suppress("SpellCheckingInspection")
@Serializable
data class ExternalIds(
    val isrc: String
)

@Serializable
data class ExternalUrls(
    val spotify: String
)

@Serializable
data class Image(
    val height: Int,
    val url: String,
    val width: Int
)
