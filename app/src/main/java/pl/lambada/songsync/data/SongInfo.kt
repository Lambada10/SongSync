package pl.lambada.songsync.data

/*
Data class for storing song information
Used both for local and remote songs,
the only difference is that local songs have songLink set to null
 */
data class SongInfo(
    var songName: String? = null,
    var artistName: String? = null,
    var songLink: String? = null,
    var albumCoverLink: String? = null,
)
