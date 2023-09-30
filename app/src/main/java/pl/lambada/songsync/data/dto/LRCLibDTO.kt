package pl.lambada.songsync.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LRCLibResponse(
    val id: Int,
    val name: String,
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val duration: Int,
    val instrumental: Boolean,
    val plainLyrics: String?,
    val syncedLyrics: String?
)