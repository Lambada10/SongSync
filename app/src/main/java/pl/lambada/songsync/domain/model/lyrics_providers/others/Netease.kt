package pl.lambada.songsync.domain.model.lyrics_providers.others

import kotlinx.serialization.Serializable

@Serializable
data class NeteaseResponse(
    val result: NeteaseResult,
    val code: Int
)

@Serializable
data class NeteaseResult(
    val songs: List<NeteaseSong>,
    val songCount: Int
)

@Serializable
data class NeteaseSong(
    val name: String,
    val id: Int,
    val artists: List<NeteaseArtist>,
)

@Serializable
data class NeteaseArtist(
    val name: String
)

@Serializable
data class NeteaseLyricsResponse(
    val lrc: NeteaseLyrics,
    val code: Int
)

@Serializable
data class NeteaseLyrics(
    val lyric: String
)
