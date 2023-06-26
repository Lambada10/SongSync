package pl.lambada.songsync.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SyncedLinesResponse(
    val error: Boolean,
    val syncType: String,
    val lines: List<Line>
)

@Serializable
data class Line(
    val timeTag: String,
    val words: String
)
