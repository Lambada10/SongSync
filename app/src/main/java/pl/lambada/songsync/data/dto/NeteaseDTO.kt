package pl.lambada.songsync.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class NeteaseResponse(
    val result: Result,
    val code: Int
)

@Serializable
data class Result(
    val songs: List<NetEaseSong>,
    val songCount: Int
)

@Serializable
data class NetEaseSong(
    val name: String,
    val id: Int,
    val artists: List<NetEaseArtist>,
)

@Serializable
data class NetEaseArtist(
    val name: String
)

@Serializable
data class NeteaseLyricsResponse(
    val lrc: NetaaseLyrics,
    val code: Int
)

@Serializable
data class NetaaseLyrics(
    val lyric: String
)
