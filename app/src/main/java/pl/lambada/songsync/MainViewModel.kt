package pl.lambada.songsync

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import pl.lambada.songsync.data.remote.github.GithubAPI
import pl.lambada.songsync.data.remote.lyrics_providers.others.AppleAPI
import pl.lambada.songsync.data.remote.lyrics_providers.others.LRCLibAPI
import pl.lambada.songsync.data.remote.lyrics_providers.others.NeteaseAPI
import pl.lambada.songsync.data.remote.lyrics_providers.spotify.SpotifyAPI
import pl.lambada.songsync.data.remote.lyrics_providers.spotify.SpotifyLyricsAPI
import pl.lambada.songsync.domain.model.Release
import pl.lambada.songsync.domain.model.SongInfo
import pl.lambada.songsync.ui.screens.Providers
import pl.lambada.songsync.util.ext.getVersion
import java.io.FileNotFoundException
import java.net.UnknownHostException

/**
 * ViewModel class for the main functionality of the app.
 */
class MainViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    var blacklistedFolders = mutableListOf<String>()
    var hideLyrics by mutableStateOf(false)
    // Spotify API token
    private val spotifyAPI = SpotifyAPI()

    // other settings
    var pureBlack: MutableState<Boolean> = mutableStateOf(false)
    var disableMarquee: MutableState<Boolean> = mutableStateOf(false)
    var sdCardPath = ""

    // selected provider
    var selectedProvider by mutableStateOf(Providers.SPOTIFY)

    // LRCLib Track ID
    private var lrcLibID = 0

    // Netease Track ID and stuff
    private var neteaseID = 0L
    var includeTranslation = false

    // Apple Track ID
    private var appleID = 0L
    // TODO: Use values from SongInfo object returned by search instead of storing them here

    /**
     * Refreshes the access token by sending a request to the Spotify API.
     */
    suspend fun refreshToken() {
        spotifyAPI.refreshToken()
    }

    /**
     * Gets song information from the Spotify API.
     * @param query The SongInfo object with songName and artistName fields filled.
     * @param offset (optional) The offset used for trying to find a better match or searching again.
     * @return The SongInfo object containing the song information.
     */
    @Throws(
        UnknownHostException::class, FileNotFoundException::class, NoTrackFoundException::class,
        EmptyQueryException::class, InternalErrorException::class
    )
    suspend fun getSongInfo(query: SongInfo, offset: Int? = 0): SongInfo {
        return try {
            when (this.selectedProvider) {
                Providers.SPOTIFY -> spotifyAPI.getSongInfo(query, offset)
                Providers.LRCLIB -> LRCLibAPI().getSongInfo(query).also {
                    this.lrcLibID = it?.lrcLibID ?: 0
                } ?: throw NoTrackFoundException()

                Providers.NETEASE -> NeteaseAPI().getSongInfo(query, offset).also {
                    this.neteaseID = it?.neteaseID ?: 0
                } ?: throw NoTrackFoundException()

                Providers.APPLE -> AppleAPI().getSongInfo(query).also {
                    this.appleID = it?.appleID ?: 0
                } ?: throw NoTrackFoundException()
            }
        } catch (e: InternalErrorException) {
            throw e
        } catch (e: NoTrackFoundException) {
            throw e
        } catch (e: EmptyQueryException) {
            throw e
        } catch (e: Exception) {
            throw InternalErrorException(Log.getStackTraceString(e))
        }
    }

    /**
     * Gets synced lyrics using the song link and returns them as a string formatted as an LRC file.
     * @param songLink The link to the song.
     * @return The synced lyrics as a string.
     */
    suspend fun getSyncedLyrics(songLink: String, version: String): String? {
        return try {
            when (this.selectedProvider) {
                Providers.SPOTIFY -> SpotifyLyricsAPI().getSyncedLyrics(songLink, version)
                Providers.LRCLIB -> LRCLibAPI().getSyncedLyrics(this.lrcLibID)
                Providers.NETEASE -> NeteaseAPI().getSyncedLyrics(this.neteaseID, includeTranslation)
                Providers.APPLE -> AppleAPI().getSyncedLyrics(this.appleID)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Gets latest GitHub release information.
     * @return The latest release version.
     */
    suspend fun getLatestRelease(): Release {
        return GithubAPI.getLatestRelease()
    }

    /**
     * Checks if the latest release is newer than the current version.
     */
    suspend fun isNewerRelease(context: Context): Boolean {
        val currentVersion = context.getVersion().replace(".", "").toInt()
        val latestVersion = getLatestRelease().tagName.replace(".", "").replace("v", "").toInt()

        return latestVersion > currentVersion
    }
}

class NoTrackFoundException : Exception()

class InternalErrorException(msg: String) : Exception(msg)

class EmptyQueryException : Exception()