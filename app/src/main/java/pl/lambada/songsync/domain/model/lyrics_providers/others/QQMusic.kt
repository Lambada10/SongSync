package pl.lambada.songsync.domain.model.lyrics_providers.others

import kotlinx.serialization.Serializable

@Serializable
data class QQMusicSearchResponse(
    val data: QQMusicData
)

@Serializable
data class QQMusicData(
    val song: QQMusicSong
)

@Serializable
data class QQMusicSong(
    val list: List<QQMusicSongInfo>
)

@Serializable
data class QQMusicSongInfo(
    val title: String,
    val singer: List<QQMusicSinger>,
    val album: QQMusicAlbum,
    val id: Long
)

@Serializable
data class QQMusicSinger(
    val name: String
)

@Serializable
data class QQMusicAlbum(
    val name: String
)

@Serializable
data class PaxQQPayload(
    val artist: List<String>,
    val album: String,
    val id: Long,
    val title: String
)