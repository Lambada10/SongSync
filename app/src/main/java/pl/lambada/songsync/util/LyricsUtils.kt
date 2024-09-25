package pl.lambada.songsync.util

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
import androidx.documentfile.provider.DocumentFile
import com.kyant.taglib.TagLib
import pl.lambada.songsync.R
import pl.lambada.songsync.domain.model.Song
import pl.lambada.songsync.domain.model.SongInfo
import pl.lambada.songsync.ui.screens.home.HomeViewModel
import pl.lambada.songsync.util.ext.getVersion
import pl.lambada.songsync.util.ext.toLrcFile
import java.io.File
import java.io.FileNotFoundException

fun generateLrcContent(
    song: SongInfo,
    lyrics: String,
    generatedUsingString: String
): String {
    return "[ti:${song.songName}]\n[ar:${song.artistName}]\n[by:$generatedUsingString]\n$lyrics"
}

fun newLyricsFilePath(filePath: String?, song: SongInfo): File {
    return filePath?.toLrcFile() ?: File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "SongSync/${song.songName} - ${song.artistName}.lrc"
    )
}

fun writeLyricsToFile(
    file: File?,
    lrcContent: String,
    context: Context,
    song: Song,
    sdCardPath: String?
) {
    try {
        file?.writeText(lrcContent)
    } catch (e: FileNotFoundException) {
        handleFileNotFoundException(context, song, file, lrcContent, sdCardPath)
    }
}

fun handleFileNotFoundException(
    context: Context,
    song: Song,
    file: File?,
    lrc: String,
    sdCardPath: String?
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && !song.filePath!!.contains("/storage/emulated/0")) {
        val sd = context.externalCacheDirs[1].absolutePath.substring(
            0,
            context.externalCacheDirs[1].absolutePath.indexOf("/Android/data")
        )
        val path = file?.absolutePath?.substringAfter(sd)?.split("/")?.dropLast(1)
        var sdCardFiles = DocumentFile.fromTreeUri(context, Uri.parse(sdCardPath))
        for (element in path!!) {
            for (sdCardFile in sdCardFiles!!.listFiles()) {
                if (sdCardFile.name == element) {
                    sdCardFiles = sdCardFile
                }
            }
        }
        sdCardFiles?.listFiles()?.forEach {
            if (it.name == file.name) {
                it.delete()
                return@forEach
            }
        }
        sdCardFiles?.createFile("text/lrc", file.name)?.let {
            val outputStream = context.contentResolver.openOutputStream(it.uri)
            outputStream?.write(lrc.toByteArray())
            outputStream?.close()
        }
    } else {
        error("Unable to handle FileNotFoundException")
    }
}

@SuppressLint("Range")
fun getFileDescriptorFromPath(
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

fun handleSecurityException(
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

/**
 * Defines possible provider choices
 */
enum class Providers(val displayName: String) {
    SPOTIFY("Spotify (via SpotifyLyricsAPI)"),
    LRCLIB("LRCLib"),
    NETEASE("Netease") { val inf = 0},
    APPLE("Apple Music")
}

// only for invoking the task and handling and reporting progress
suspend fun downloadLyrics(
    songs: List<Song>,
    viewModel: HomeViewModel,
    context: Context,
    onProgressUpdate: (successCount: Int, noLyricsCount: Int, failedCount: Int) -> Unit,
    onDownloadComplete: () -> Unit,
    onRateLimitReached: () -> Unit,
) {
    var successCount = 0
    var noLyricsCount = 0
    var failedCount = 0
    var consecutiveNotFound = 0

    songs.forEach { song ->
        downloadLyricsForSong(
            song,
            viewModel,
            context,
            onFailedSongInfoResponse = {
                failedCount++
                consecutiveNotFound++
                if (consecutiveNotFound >= 5) onRateLimitReached()
            },
            onSuccessfulSongInfoResponse = { consecutiveNotFound = 0 },
            onFailedLyricsResponse = {
                if (it is NullPointerException || it is FileNotFoundException)
                    noLyricsCount++
                else {
                    failedCount++
                    consecutiveNotFound++
                }
            },
            onLyricsSaved = { successCount++ }
        )

        onProgressUpdate(successCount, noLyricsCount, failedCount)
    }

    onDownloadComplete()
}



// only for retrieval, processing, and saving data
private suspend fun downloadLyricsForSong(
    song: Song,
    viewModel: HomeViewModel,
    context: Context,
    onFailedSongInfoResponse: (Throwable) -> Unit,
    onSuccessfulSongInfoResponse: () -> Unit,
    onFailedLyricsResponse: (Throwable) -> Unit,
    onLyricsSaved: () -> Unit
) {
    runCatching {
        viewModel
            .getSongInfo(SongInfo(song.title, song.artist))
            ?: throw NullPointerException("Song info result is null")
    }
        .onFailure(onFailedSongInfoResponse)
        .onSuccess { songInfo ->
            onSuccessfulSongInfoResponse()

            runCatching {
                viewModel
                    .getSyncedLyrics(
                        link = songInfo.songLink,
                        version = context.getVersion()
                    )
                    ?: throw NullPointerException("Lyrics result is null")
            }
                .onFailure(onFailedLyricsResponse)
                .onSuccess {
                    formatAndSaveLyricsForSong(
                        song,
                        context,
                        viewModel.userSettingsController.sdCardPath,
                        songInfo,
                        it
                    )

                    onLyricsSaved()
                }
        }
}

private fun formatAndSaveLyricsForSong(
    song: Song,
    context: Context,
    sdCardPath: String?,
    songInfo: SongInfo,
    lyrics: String,
) {
    val targetFile = song.filePath.toLrcFile()

    val lrcContent = generateLrcContent(
        songInfo,
        lyrics,
        context.getString(R.string.generated_using)
    )

    writeLyricsToFile(targetFile, lrcContent, context, song, sdCardPath)
}

fun saveToExternalPath(
    context: Context,
    sourceFilePath: String?,
    lrc: String,
    fileName: String,
    newLyricsFilePath: String?
) {
    val sd = context.externalCacheDirs[1].absolutePath.substringBefore("/Android/data")
    val path = sourceFilePath
        ?.toLrcFile()
        ?.absolutePath
        ?.substringAfter(sd)
        ?.split("/")
        ?.dropLast(1)
        ?: error("path was null when trying to save to sd card")
    var sdCardFiles = DocumentFile.fromTreeUri(context, Uri.parse(newLyricsFilePath))
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