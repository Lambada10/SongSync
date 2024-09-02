package pl.lambada.songsync.ui.screens.lyricsFetch

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyant.taglib.TagLib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.lambada.songsync.R
import pl.lambada.songsync.data.remote.UserSettingsController
import pl.lambada.songsync.data.remote.lyrics_providers.LyricsProviderService
import pl.lambada.songsync.domain.model.SongInfo
import pl.lambada.songsync.ui.LocalSong
import pl.lambada.songsync.util.ext.getVersion
import pl.lambada.songsync.util.ext.toLrcFile
import java.io.File
import java.io.FileNotFoundException
import java.net.UnknownHostException

/**
 * ViewModel class for the main functionality of the app.
 */
class LyricsFetchViewModel(
    val source: LocalSong?,
    val userSettingsController: UserSettingsController,
    private val lyricsProviderService: LyricsProviderService
) : ViewModel() {
    var querySongName by mutableStateOf(source?.songName ?: "")
    var queryArtistName by mutableStateOf(source?.artists ?: "")

    // queryStatus: "Not submitted", "Pending", "Success", "Failed" - used to show different UI
    var queryState by mutableStateOf(
        if (source == null) QueryStatus.NotSubmitted else QueryStatus.Pending
    )
    private var queryOffset by mutableIntStateOf(0)

    var lyricsFetchState by mutableStateOf<LyricsFetchState>(LyricsFetchState.NotSubmitted)

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

    private fun embedLyricsInFile(
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

    private suspend fun getSyncedLyrics(link: String, version: String): String? =
        lyricsProviderService.getSyncedLyrics(link, version, userSettingsController.selectedProvider)

    fun loadSongInfo(context: Context, tryingAgain: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                queryState = QueryStatus.Pending
                lyricsFetchState = LyricsFetchState.NotSubmitted
                if (tryingAgain) queryOffset += 1 else queryOffset = 0

                val result = lyricsProviderService
                    .getSongInfo(
                        query = SongInfo(
                            songName = querySongName,
                            artistName = queryArtistName
                        ),
                        offset = queryOffset,
                        provider = userSettingsController.selectedProvider
                    )
                    ?: error("Error fetching lyrics for the song.")

                queryState = QueryStatus.Success(result)
                loadLyrics(result.songLink, context)
            } catch (e: Exception) {
                queryState = when (e) {
                    is UnknownHostException -> QueryStatus.NoConnection
                    else -> QueryStatus.Failed(e)
                }
            }
        }
    }

    fun saveLyricsToFile(
        lyrics: String,
        song: SongInfo,
        filePath: String?,
        context: Context,
        generatedUsingString: String
    ) {
        val lrc = "[ti:${song.songName}]\n[ar:${song.artistName}]\n[by:$generatedUsingString]\n$lyrics"
        val file = filePath?.toLrcFile() ?: File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "SongSync/${song.songName} - ${song.artistName}.lrc"
        )
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || filePath?.contains("/storage/emulated/0/") == true) {
            file.writeText(lrc)
        } else {
            saveToExternalSDCard(context, filePath, lrc, file.name)
        }
        Toast.makeText(context, context.getString(R.string.file_saved_to, file.absolutePath), Toast.LENGTH_LONG).show()
    }

    private fun loadLyrics(songLink: String?, context: Context) {
        viewModelScope.launch {
            lyricsFetchState = LyricsFetchState.Pending
            try {
                val lyrics = getSyncedLyrics(
                    songLink ?: error("attempted lyrics retrieval with empty url"),
                    context.getVersion()
                )
                if (lyrics == null) throw NullPointerException("lyricsResult is null")
                lyricsFetchState = LyricsFetchState.Success(lyrics)
            } catch (e: Exception) {
                lyricsFetchState = LyricsFetchState.Failed(e)
            }
        }
    }

    fun embedLyricsInFile(
        lyrics: String,
        filePath: String?,
        context: Context,
        generatedUsingString: String,
        song: SongInfo
    ) {
        val lrc = "[ti:${song.songName}]\n[ar:${song.artistName}]\n[by:$generatedUsingString]\n$lyrics"
        kotlin.runCatching {
            embedLyricsInFile(
                context = context,
                filePath = filePath ?: throw NullPointerException("filePath is null"),
                lyrics = lrc
            )
        }.onFailure { exception ->
            val errorMessage = when (exception) {
                is NullPointerException -> context.getString(R.string.embed_non_local_song_error)
                else -> exception.message ?: context.getString(R.string.error)
            }
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }.onSuccess {
            Toast.makeText(context, context.getString(R.string.embedded_lyrics_in_file), Toast.LENGTH_LONG).show()
        }
    }

    private fun saveToExternalSDCard(
        context: Context,
        filePath: String?,
        lrc: String,
        fileName: String
    ) {
        val sd = context.externalCacheDirs[1].absolutePath.substring(0, context.externalCacheDirs[1].absolutePath.indexOf("/Android/data"))
        val path = filePath?.toLrcFile()?.absolutePath?.substringAfter(sd)?.split("/")?.dropLast(1)
        var sdCardFiles = DocumentFile.fromTreeUri(context, Uri.parse(userSettingsController.sdCardPath))
        for (element in path!!) {
            sdCardFiles = sdCardFiles?.listFiles()?.firstOrNull { it.name == element }
        }
        sdCardFiles?.listFiles()?.firstOrNull { it.name == fileName }?.delete()
        sdCardFiles?.createFile("text/lrc", fileName)?.let {
            context.contentResolver.openOutputStream(it.uri)?.use { outputStream ->
                outputStream.write(lrc.toByteArray())
            }
        }
    }

}

sealed interface LyricsFetchState {
    data object NotSubmitted : LyricsFetchState
    data object Pending : LyricsFetchState
    data class Success(val lyrics: String) : LyricsFetchState
    data class Failed(val exception: Exception) : LyricsFetchState
}

sealed interface QueryStatus {
    data object NotSubmitted : QueryStatus
    data object Pending : QueryStatus
    data class Success(val song: SongInfo) : QueryStatus
    data class Failed(val exception: Exception) : QueryStatus
    data object NoConnection : QueryStatus
}