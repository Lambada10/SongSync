package pl.lambada.songsync.domain.model.lyrics_providers.others

import kotlinx.serialization.Serializable

@Serializable
data class VocaDBSearchResponse(
    val items: List<VocaDBSongItem> = emptyList(),
    val term: String? = null,
    val totalCount: Int = 0
)

@Serializable
data class VocaDBSongItem(
    val id: Int,
    val name: String,
    val defaultName: String? = null,
    val defaultNameLanguage: String? = null,
    val artistString: String? = null,
    val lengthSeconds: Int? = null
)

@Serializable
data class VocaDBSongWithLyrics(
    val id: Int,
    val name: String,
    val artistString: String? = null,
    val publishDate: String? = null,
    val lyrics: List<VocaDBLyricsItem> = emptyList()
)

@Serializable
data class VocaDBLyricsItem(
    val cultureCodes: List<String> = emptyList(),
    val id: Int,
    val source: String? = null,
    val translationType: String? = null,
    val url: String? = null,
    val value: String? = null
)


@Serializable
data class VocaDBArtistSearchResponse(
    val items: List<VocaDBArtistItem> = emptyList(),
    val term: String? = null,
    val totalCount: Int = 0
)

@Serializable
data class VocaDBArtistItem(
    val id: Int,
    val name: String,
    val defaultName: String? = null,
    val defaultNameLanguage: String? = null,
    val artistType: String? = null
)
