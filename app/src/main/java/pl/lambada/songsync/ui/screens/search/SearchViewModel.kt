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
import pl.lambada.songsync.data.remote.lyrics_providers.LyricsProviderService
import pl.lambada.songsync.domain.model.SongInfo
import java.io.FileNotFoundException

/**
 * ViewModel class for the main functionality of the app.
 */
class SearchViewModel(
    val userSettingsController: UserSettingsController,
    private val lyricsProviderService: LyricsProviderService
) : ViewModel() {
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

    suspend fun getSongInfo(queryResult: SongInfo, offset: Int): SongInfo? =
        lyricsProviderService.getSongInfo(queryResult, offset, userSettingsController.selectedProvider)

    suspend fun getSyncedLyrics(s: String, version: String): String? =
        lyricsProviderService.getSyncedLyrics(s, version, userSettingsController.selectedProvider)
}