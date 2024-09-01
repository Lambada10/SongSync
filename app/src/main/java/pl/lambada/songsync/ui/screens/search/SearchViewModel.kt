package pl.lambada.songsync.ui.screens.search

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import com.kyant.taglib.TagLib
import pl.lambada.songsync.data.remote.UserSettingsController
import pl.lambada.songsync.data.remote.lyrics_providers.others.AppleAPI
import pl.lambada.songsync.data.remote.lyrics_providers.others.LRCLibAPI
import pl.lambada.songsync.data.remote.lyrics_providers.others.NeteaseAPI
import pl.lambada.songsync.data.remote.lyrics_providers.spotify.SpotifyAPI
import pl.lambada.songsync.data.remote.lyrics_providers.spotify.SpotifyLyricsAPI
import pl.lambada.songsync.domain.model.SongInfo
import pl.lambada.songsync.ui.screens.Providers
import pl.lambada.songsync.ui.screens.home.EmptyQueryException
import pl.lambada.songsync.ui.screens.home.InternalErrorException
import pl.lambada.songsync.ui.screens.home.NoTrackFoundException
import java.io.FileNotFoundException
import java.net.UnknownHostException

/**
 * ViewModel class for the main functionality of the app.
 */
class SearchViewModel(val userSettingsController: UserSettingsController) : ViewModel() {
    // Spotify API token
    private val spotifyAPI = SpotifyAPI()

    // LRCLib Track ID
    private var lrcLibID = 0

    // Netease Track ID and stuff
    private var neteaseID = 0L

    // Apple Track ID
    private var appleID = 0L
    // TODO: Use values from SongInfo object returned by search instead of storing them here

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
            when (userSettingsController.selectedProvider) {
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
            when (userSettingsController.selectedProvider) {
                Providers.SPOTIFY -> SpotifyLyricsAPI().getSyncedLyrics(songLink, version)
                Providers.LRCLIB -> LRCLibAPI().getSyncedLyrics(this.lrcLibID)
                Providers.NETEASE -> NeteaseAPI().getSyncedLyrics(this.neteaseID, userSettingsController.includeTranslation)
                Providers.APPLE -> AppleAPI().getSyncedLyrics(this.appleID)
            }
        } catch (e: Exception) {
            null
        }
    }

    @SuppressLint("Range")
    private fun getFileDescriptorFromPath(
        context: Context, filePath: String, mode: String = "r"
    ): ParcelFileDescriptor? {
        val resolver: ContentResolver = context.contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(MediaStore.Files.FileColumns._ID)
        val selection = "${MediaStore.Files.FileColumns.DATA}=?"
        val selectionArgs = arrayOf(filePath)

        resolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val fileId: Int =
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID))
                if (fileId == -1) {
                    return null
                } else {
                    val fileUri: Uri = Uri.withAppendedPath(uri, fileId.toString())
                    try {
                        return resolver.openFileDescriptor(fileUri, mode)
                    } catch (e: FileNotFoundException) {
                        Log.e("MainViewModel", "File not found: ${e.message}")
                    }
                }
            }
        }

        return null
    }

    private fun handleSecurityException(
        securityException: SecurityException, intentPassthrough: (PendingIntent) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val recoverableSecurityException =
                securityException as? RecoverableSecurityException ?: throw RuntimeException(
                    securityException.message, securityException
                )

            intentPassthrough(recoverableSecurityException.userAction.actionIntent)
        } else {
            throw RuntimeException(securityException.message, securityException)
        }
    }

    fun embedLyricsInFile(
        context: Context,
        filePath: String,
        lyrics: String,
        securityExceptionHandler: (PendingIntent) -> Unit = {}
    ): Boolean {
        return try {
            val fd = getFileDescriptorFromPath(context, filePath, mode = "w")
                ?: throw IllegalStateException("File descriptor is null")

            val fileDescriptor = fd.dup().detachFd()

            val metadata = TagLib.getMetadata(fileDescriptor, false) ?: throw IllegalStateException(
                "Metadata is null"
            )

            fd.dup().detachFd().let {
                TagLib.savePropertyMap(
                    it, propertyMap = metadata.propertyMap.apply {
                        put("LYRICS", arrayOf(lyrics))
                    }
                )
            }

            true
        } catch (securityException: SecurityException) {
            handleSecurityException(securityException, securityExceptionHandler)
            false
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error embedding lyrics: ${e.message}")
            false
        }
    }
}