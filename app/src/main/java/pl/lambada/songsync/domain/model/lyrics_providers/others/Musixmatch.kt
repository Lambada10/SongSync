package pl.lambada.songsync.domain.model.lyrics_providers.others

import kotlinx.serialization.Serializable

@Serializable
data class MusixmatchSearchResponse(
    val commontrack_id: Long,
    val track_name: String,
    val artist_name: String,
    val album_name: String,
    val album_cover: String,
    val release_date: String,
    val track_length: Int,
    val track_share_url: String,
    val album_id: Long,
    val has_lyrics: Boolean
)

@Serializable
data class MusixmatchLyricsResponse(
    val subtitle_id: Long,
    val subtitle_length: Int,
    val subtitle_language: String,
    val updated_time: String,
    val subtitle_body: String,
)