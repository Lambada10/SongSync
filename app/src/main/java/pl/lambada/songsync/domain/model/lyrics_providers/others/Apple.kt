package pl.lambada.songsync.domain.model.lyrics_providers.others

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppleMusicSearchResponse(
    val results: AppleMusicResults,
    val resources: AppleMusicResources? = null
)

@Serializable
data class AppleMusicResults(
    val songs: AppleMusicSongsResult? = null
)

@Serializable
data class AppleMusicSongsResult(
    val data: List<AppleMusicSongData>
)

@Serializable
data class AppleMusicSongData(
    val id: String,
    val type: String,
    val href: String
)

@Serializable
data class AppleMusicResources(
    val songs: Map<String, AppleMusicSongDetail>? = null,
    val artists: Map<String, AppleMusicArtistDetail>? = null
)

@Serializable
data class AppleMusicSongDetail(
    val id: String,
    val type: String,
    val attributes: AppleMusicSongAttributes,
    val relationships: AppleMusicRelationships? = null
)

@Serializable
data class AppleMusicSongAttributes(
    val name: String,
    val artistName: String,
    val albumName: String,
    val artwork: AppleMusicArtwork,
    val url: String,
    val isrc: String? = null,
    val releaseDate: String? = null,
    val durationInMillis: Long? = null,
    val hasTimeSyncedLyrics: Boolean? = null,
    val contentRating: String? = null
)

@Serializable
data class AppleMusicArtwork(
    val url: String,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class AppleMusicRelationships(
    val artists: AppleMusicArtistsRelation? = null
)

@Serializable
data class AppleMusicArtistsRelation(
    val data: List<AppleMusicArtistData>
)

@Serializable
data class AppleMusicArtistData(
    val id: String,
    val type: String
)

@Serializable
data class AppleMusicArtistDetail(
    val id: String,
    val attributes: AppleMusicArtistAttributes
)

@Serializable
data class AppleMusicArtistAttributes(
    val name: String,
    val url: String? = null
)