package pl.lambada.songsync.ui.screens.lyricsFetch

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.RecoverableSecurityException
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

    private suspend fun getSyncedLyrics(link: String, version: String): String? =
        lyricsProviderService.getSyncedLyrics(
            link,
            version,
            userSettingsController.selectedProvider
        )

    fun loadSongInfo(context: Context, tryingAgain: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                queryState = QueryStatus.Pending
                lyricsFetchState = LyricsFetchState.NotSubmitted
                queryOffset = if (tryingAgain) queryOffset + 1 else 0

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
        val lrcContent = generateLrcContent(song, lyrics, generatedUsingString)
        val file = newLyricsFilePath(filePath, song)

        if (isLegacyFileAccessRequired(filePath)) {
            file.writeText(lrcContent)
        } else {
            saveToExternalSDCard(context, filePath, lrcContent, file.name, userSettingsController.sdCardPath)
        }

        showToast(context, R.string.file_saved_to, file.absolutePath)
    }

    private fun loadLyrics(songLink: String?, context: Context) {
        viewModelScope.launch {
            lyricsFetchState = LyricsFetchState.Pending
            try {
                val lyrics = getSyncedLyrics(
                    songLink ?: throw IllegalStateException("Attempted lyrics retrieval with empty URL"),
                    context.getVersion()
                ) ?: throw NullPointerException("Lyrics result is null")
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
        val lrcContent = generateLrcContent(song, lyrics, generatedUsingString)

        runCatching {
            embedLyricsInFile(
                context,
                filePath ?: throw NullPointerException("File path is null"),
                lrcContent
            )
        }.onFailure { exception ->
            showToast(context, resolveEmbedErrorMessage(context, exception))
        }.onSuccess {
            showToast(context, R.string.embedded_lyrics_in_file)
        }
    }
}

private fun generateLrcContent(
    song: SongInfo,
    lyrics: String,
    generatedUsingString: String
): String {
    return "[ti:${song.songName}]\n[ar:${song.artistName}]\n[by:$generatedUsingString]\n$lyrics"
}

private fun newLyricsFilePath(filePath: String?, song: SongInfo): File {
    return filePath?.toLrcFile() ?: File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "SongSync/${song.songName} - ${song.artistName}.lrc"
    )
}

private fun isLegacyFileAccessRequired(filePath: String?): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.R
            || filePath?.contains("/storage/emulated/0/") == true
}

private fun resolveEmbedErrorMessage(context: Context, exception: Throwable): String {
    return when (exception) {
        is NullPointerException -> context.getString(R.string.embed_non_local_song_error)
        else -> exception.message ?: context.getString(R.string.error)
    }
}

private fun showToast(context: Context, messageResId: Int, vararg args: Any) {
    Toast.makeText(context, context.getString(messageResId, *args), Toast.LENGTH_LONG).show()
}
private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

@SuppressLint("Range")
private fun getFileDescriptorFromPath(
    context: Context, filePath: String, mode: String = "r"
): ParcelFileDescriptor? {
    val resolver = context.contentResolver
    val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    val projection = arrayOf(MediaStore.Files.FileColumns._ID)
    val selection = "${MediaStore.Files.FileColumns.DATA}=?"
    val selectionArgs = arrayOf(filePath)

    return resolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val fileId = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID))
            if (fileId != -1) {
                val fileUri = Uri.withAppendedPath(uri, fileId.toString())
                try {
                    resolver.openFileDescriptor(fileUri, mode)
                } catch (e: FileNotFoundException) {
                    Log.e("LyricsFetchViewModel", "File not found: ${e.message}")
                    null
                }
            } else null
        } else null
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
        val metadata = TagLib.getMetadata(fileDescriptor, false) ?: error("Metadata is null")

        TagLib.savePropertyMap(
            fd.dup().detachFd(),
            propertyMap = metadata.propertyMap.apply { put("LYRICS", arrayOf(lyrics)) }
        )

        true
    } catch (securityException: SecurityException) {
        handleSecurityException(securityException, securityExceptionHandler)
        false
    } catch (e: Exception) {
        Log.e("LyricsFetchViewModel", "Error embedding lyrics: ${e.message}")
        false
    }
}

private fun saveToExternalSDCard(
    context: Context,
    filePath: String?,
    lrc: String,
    fileName: String,
    sdCardPath: String?
) {
    val sd = context.externalCacheDirs[1].absolutePath.substringBefore("/Android/data")
    val path = filePath
        ?.toLrcFile()
        ?.absolutePath
        ?.substringAfter(sd)
        ?.split("/")
        ?.dropLast(1)
        ?: error("path was null when trying to save to sd card")
    var sdCardFiles = DocumentFile.fromTreeUri(context, Uri.parse(sdCardPath))
    path.forEach { element ->
        sdCardFiles = sdCardFiles?.listFiles()?.firstOrNull { it.name == element }
    }
    sdCardFiles?.listFiles()?.firstOrNull { it.name == fileName }?.delete()
    sdCardFiles?.createFile("text/lrc", fileName)?.let {
        context.contentResolver.openOutputStream(it.uri)?.use { outputStream ->
            outputStream.write(lrc.toByteArray())
        }
    }
}

private fun handleSecurityException(
    securityException: SecurityException,
    intentPassthrough: (PendingIntent) -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val recoverableSecurityException =
            securityException as? RecoverableSecurityException
                ?: throw RuntimeException(securityException.message, securityException)

        intentPassthrough(recoverableSecurityException.userAction.actionIntent)
    } else {
        throw RuntimeException(securityException.message, securityException)
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