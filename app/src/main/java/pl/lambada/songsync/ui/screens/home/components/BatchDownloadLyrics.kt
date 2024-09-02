package pl.lambada.songsync.ui.screens.home.components

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.documentfile.provider.DocumentFile
import pl.lambada.songsync.R
import pl.lambada.songsync.domain.model.Song
import pl.lambada.songsync.domain.model.SongInfo
import pl.lambada.songsync.ui.components.AnimatedText
import pl.lambada.songsync.ui.screens.home.HomeViewModel
import pl.lambada.songsync.util.EmptyQueryException
import pl.lambada.songsync.util.InternalErrorException
import pl.lambada.songsync.util.NoTrackFoundException
import pl.lambada.songsync.util.ext.getVersion
import pl.lambada.songsync.util.ext.toLrcFile
import java.io.File
import java.io.FileNotFoundException
import kotlin.math.roundToInt

@SuppressLint("StringFormatMatches")
@Composable
fun BatchDownloadLyrics(viewModel: HomeViewModel, onDone: () -> Unit) {
    val songs = viewModel.songsToBatchDownload
    var uiState by rememberSaveable { mutableStateOf(UiState.Warning) }
    var successCount by rememberSaveable { mutableIntStateOf(0) }
    var noLyricsCount by rememberSaveable { mutableIntStateOf(0) }
    var failedCount by rememberSaveable { mutableIntStateOf(0) }
    val count = successCount + failedCount + noLyricsCount
    val total = songs.size
    val context = LocalContext.current

    when (uiState) {
        UiState.Cancelled -> onDone()
        UiState.Warning -> BatchDownloadWarningDialog(
            songsCount = songs.size,
            onConfirm = {
                uiState = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    UiState.LegacyPrompt
                } else {
                    UiState.Pending
                }
            },
            onDismiss = { uiState = UiState.Cancelled }
        )

        UiState.LegacyPrompt -> LegacyPromptDialog(
            onConfirm = { uiState = UiState.Pending },
            onDismiss = { uiState = UiState.Cancelled }
        )

        UiState.Pending -> {
            val percentage =
                if (total != 0) (count.toFloat() / total.toFloat() * 100).roundToInt() else 0
            LaunchedEffect(Unit) {
                downloadLyrics(
                    songs = songs,
                    viewModel = viewModel,
                    context = context,
                    onProgressUpdate = { s, n, f ->
                        successCount = s
                        noLyricsCount = n
                        failedCount = f
                    },
                    onDownloadComplete = { uiState = UiState.Done },
                    onRateLimitReached = { uiState = UiState.RateLimited }
                )
            }
            DownloadProgressDialog(
                currentSongTitle = songs.getOrNull(count % total)?.title,
                count = count,
                total = total,
                percentage = percentage,
                successCount = successCount,
                noLyricsCount = noLyricsCount,
                failedCount = failedCount,
                onCancel = { uiState = UiState.Cancelled },
                disableMarquee = viewModel.userSettingsController.disableMarquee
            )
        }

        UiState.Done -> DownloadCompleteDialog(
            successCount = successCount,
            noLyricsCount = noLyricsCount,
            failedCount = failedCount,
            onDismiss = { uiState = UiState.Cancelled }
        )

        UiState.RateLimited -> RateLimitedDialog(onDismiss = { uiState = UiState.Cancelled })
    }
}

@Composable
fun BatchDownloadWarningDialog(songsCount: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        title = {
            Text(text = stringResource(id = R.string.batch_download_lyrics))
        },
        text = {
            Column {
                Text(
                    text = pluralStringResource(
                        R.plurals.this_will_download_lyrics_for_all_songs,
                        songsCount,
                        songsCount
                    )
                )
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(R.string.yes))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.no))
            }
        }
    )
}

@Composable
fun LegacyPromptDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        title = {
            Text(text = stringResource(id = R.string.batch_download_lyrics))
        },
        text = {
            Column {
                Text(text = stringResource(R.string.set_sd_path_warn))
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(R.string.ok))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
fun DownloadProgressDialog(
    currentSongTitle: String?,
    count: Int,
    total: Int,
    percentage: Int,
    successCount: Int,
    noLyricsCount: Int,
    failedCount: Int,
    onCancel: () -> Unit,
    disableMarquee: Boolean,
) {
    AlertDialog(
        title = {
            Text(text = stringResource(id = R.string.batch_download_lyrics))
        },
        text = {
            Column {
                Text(text = stringResource(R.string.downloading_lyrics))
                AnimatedText(
                    animate = !disableMarquee,
                    text = stringResource(
                        R.string.song,
                        currentSongTitle ?: stringResource(id = R.string.unknown)
                    )
                )
                Text(text = stringResource(R.string.progress, count, total, percentage))
                Text(
                    text = stringResource(
                        R.string.success_failed, successCount, noLyricsCount, failedCount
                    )
                )
                Text(text = stringResource(R.string.please_do_not_close_the_app_this_may_take_a_while))
            }
        },
        onDismissRequest = { /* Prevent accidental dismiss */ },
        confirmButton = { /* Empty but required */ },
        dismissButton = {
            OutlinedButton(onClick = onCancel) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun DownloadCompleteDialog(
    successCount: Int,
    noLyricsCount: Int,
    failedCount: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        title = {
            Text(text = stringResource(id = R.string.batch_download_lyrics))
        },
        text = {
            Column {
                Text(text = stringResource(R.string.download_complete))
                Text(text = stringResource(R.string.success, successCount))
                Text(text = stringResource(R.string.no_lyrics, noLyricsCount))
                Text(text = stringResource(R.string.failed, failedCount))
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    )
}


@Composable
fun RateLimitedDialog(onDismiss: () -> Unit) {
    AlertDialog(
        title = {
            Text(text = stringResource(id = R.string.batch_download_lyrics))
        },
        text = {
            Column {
                Text(text = stringResource(R.string.spotify_api_rate_limit_reached))
                Text(text = stringResource(R.string.please_try_again_later))
                Text(text = stringResource(R.string.change_api_strategy))
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    )
}

suspend fun downloadLyrics(
    songs: List<Song>,
    viewModel: HomeViewModel,
    context: Context,
    onProgressUpdate: (successCount: Int, noLyricsCount: Int, failedCount: Int) -> Unit,
    onDownloadComplete: () -> Unit,
    onRateLimitReached: () -> Unit
) {
    var successCount = 0
    var noLyricsCount = 0
    var failedCount = 0
    var notFoundInARow = 0

    for (i in songs.indices) {
        val song = songs[i]

        val file = song.filePath.toLrcFile()
        val query = SongInfo(song.title, song.artist)
        var queryResult: SongInfo? = null
        try {
            queryResult = viewModel.getSongInfo(query)
        } catch (e: Exception) {
            when (e) {
                is FileNotFoundException -> {
                    notFoundInARow++
                    failedCount++
                    if (notFoundInARow >= 5) {
                        onRateLimitReached()
                        return
                    }
                    continue
                }

                is NoTrackFoundException, is EmptyQueryException, is InternalErrorException -> {
                    failedCount++
                }

                else -> throw e
            }
        }

        notFoundInARow = 0
        if (queryResult != null) {
            val lyricsResult: String
            try {
                lyricsResult =
                    viewModel.getSyncedLyrics(queryResult.songLink ?: "", context.getVersion())!!
            } catch (e: Exception) {
                when (e) {
                    is NullPointerException, is FileNotFoundException -> {
                        noLyricsCount++
                        continue
                    }

                    else -> throw e
                }
            }
            val lrc = "[ti:${queryResult.songName}]\n" +
                    "[ar:${queryResult.artistName}]\n" +
                    "[by:${context.getString(R.string.generated_using)}]\n" +
                    lyricsResult
            try {
                file?.writeText(lrc)
            } catch (e: FileNotFoundException) {
                handleFileNotFoundException(
                    context,
                    song,
                    file,
                    lrc,
                    viewModel.userSettingsController.sdCardPath
                )
            }
            successCount++
        }

        onProgressUpdate(successCount, noLyricsCount, failedCount)
    }
    onDownloadComplete()
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


enum class UiState {
    Warning, LegacyPrompt, Pending, Done, RateLimited, Cancelled
}