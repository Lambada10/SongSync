package pl.lambada.songsync.ui.screens.home.components

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getString
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pl.lambada.songsync.MainActivity
import pl.lambada.songsync.R
import pl.lambada.songsync.domain.model.SongInfo
import pl.lambada.songsync.ui.components.AnimatedText
import pl.lambada.songsync.ui.screens.home.HomeViewModel
import pl.lambada.songsync.util.EmptyQueryException
import pl.lambada.songsync.util.InternalErrorException
import pl.lambada.songsync.util.NoTrackFoundException
import pl.lambada.songsync.util.ext.getVersion
import pl.lambada.songsync.util.ext.toLrcFile
import java.io.FileNotFoundException
import kotlin.math.roundToInt

@SuppressLint("StringFormatMatches")
@Composable
fun BatchDownloadLyrics(viewModel: HomeViewModel, onDone: () -> Unit) {
    val unknownString = stringResource(id = R.string.unknown)
    val generatedUsingString = stringResource(id = R.string.generated_using)
    val songs = viewModel.songsToBatchDownload

    var uiState by rememberSaveable { mutableStateOf(UiState.Warning) }
    var failedCount by rememberSaveable { mutableIntStateOf(0) }
    var noLyricsCount by rememberSaveable { mutableIntStateOf(0) }
    var successCount by rememberSaveable { mutableIntStateOf(0) }
    val count = successCount + failedCount + noLyricsCount
    val total = songs.size
    val isLegacyVersion = Build.VERSION.SDK_INT < Build.VERSION_CODES.R

    val context = LocalContext.current
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelID = getString(context, R.string.batch_download_lyrics)

    val resultIntent = Intent(context, MainActivity::class.java)
    resultIntent.addCategory(Intent.CATEGORY_LAUNCHER)
    resultIntent.setAction(Intent.ACTION_MAIN)
    resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val pendingIntent =
        PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_IMMUTABLE)

    when (uiState) {
        UiState.Cancelled -> {
            notificationManager.cancelAll()
            onDone()
        }

        UiState.Warning -> {
            AlertDialog(
                title = {
                    Text(text = stringResource(id = R.string.batch_download_lyrics))
                },
                text = {
                    Column {
                        Text(
                            text = pluralStringResource(
                                R.plurals.this_will_download_lyrics_for_all_songs,
                                songs.size,
                                songs.size
                            )
                        )
                    }
                },
                onDismissRequest = {
                    uiState = UiState.Cancelled
                },
                confirmButton = {
                    Button(
                        onClick = {
                            uiState = if (isLegacyVersion) {
                                UiState.LegacyPrompt
                            } else {
                                UiState.Pending
                            }
                        },
                    ) {
                        Text(text = stringResource(R.string.yes))
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { uiState = UiState.Cancelled }) {
                        Text(text = stringResource(R.string.no))
                    }
                }
            )
        }

        UiState.LegacyPrompt -> {
            AlertDialog(
                title = {
                    Text(text = stringResource(id = R.string.batch_download_lyrics))
                },
                text = {
                    Column {
                        Text(text = stringResource(R.string.set_sd_path_warn))
                    }
                },
                onDismissRequest = {
                    uiState = UiState.Cancelled
                },
                confirmButton = {
                    Button(
                        onClick = {
                            uiState = UiState.Pending
                        },
                    ) {
                        Text(text = stringResource(R.string.ok))
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            uiState = UiState.Cancelled
                        },
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }
                },
            )
        }

        UiState.Pending -> {
            val percentage = if (total != 0) {
                (count.toFloat() / total.toFloat() * 100).roundToInt()
            } else {
                0 // In other cases = 0
            }

            val notificationBuilder = NotificationCompat.Builder(context, channelID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.downloading_lyrics))
                .setContentText(context.getString(R.string.progress, count, total, percentage))
                .setProgress(100, percentage, false).setTimeoutAfter(2000)
                .setContentIntent(pendingIntent)

            notificationManager.notify(1, notificationBuilder.build())

            AlertDialog(
                title = {
                    Text(text = stringResource(id = R.string.batch_download_lyrics))
                },
                text = {
                    Column {
                        Text(text = stringResource(R.string.downloading_lyrics))
                        AnimatedText(
                            animate = !viewModel.userSettingsController.disableMarquee,
                            text = stringResource(
                                R.string.song,
                                songs.getOrNull((count) % total.coerceAtLeast(1))?.title
                                    ?: unknownString,
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
                onDismissRequest = {
                    /*
                   it's easy to accidentally dismiss the dialog, and since it's a long running task
                   we don't want to accidentally cancel it, so we don't allow dismissing the dialog
                   user can cancel the task by pressing the cancel button
                 */
                },
                confirmButton = {
                    // no button but compose cries when I don't use confirmButton
                },
                dismissButton = {
                    OutlinedButton(onClick = { uiState = UiState.Cancelled }) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            )

            var notFoundInARow by rememberSaveable { mutableIntStateOf(0) }
            var downloadJob by remember { mutableStateOf<Job?>(null) }

            LaunchedEffect(Unit) {
                downloadJob = launch(Dispatchers.IO) {
                    for (i in count until songs.size) {
                        val song = songs[i]
                        if (uiState == UiState.Cancelled) {
                            downloadJob?.cancel()
                            return@launch
                        }

                        if (count >= total) {
                            uiState = UiState.Done
                            downloadJob?.cancel()
                            return@launch
                        }
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
                                        uiState = UiState.RateLimited
                                        return@launch
                                    }
                                    continue
                                }

                                is NoTrackFoundException, is EmptyQueryException, is InternalErrorException -> {
                                    // not increasing notFoundInARow because that is for rate limit
                                    failedCount++
                                }

                                else -> throw e
                            }
                        }
                        notFoundInARow = 0
                        if (queryResult != null) {
                            val lyricsResult: String
                            try {
                                lyricsResult = viewModel.getSyncedLyrics(queryResult.songLink ?: "", context.getVersion())!!
                            } catch (e: Exception) {
                                when (e) {
                                    is NullPointerException, is FileNotFoundException -> {
                                        noLyricsCount++
                                        continue
                                    }

                                    else -> throw e
                                }
                            }
                            val lrc =
                                "[ti:${queryResult.songName}]\n" + "[ar:${queryResult.artistName}]\n" + "[by:$generatedUsingString]\n" + lyricsResult
                            try {
                                file?.writeText(lrc)
                            } catch (e: FileNotFoundException) {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && !song.filePath!!.contains(
                                        "/storage/emulated/0"
                                    )
                                ) {
                                    val sd = context.externalCacheDirs[1].absolutePath.substring(
                                        0,
                                        context.externalCacheDirs[1].absolutePath.indexOf("/Android/data")
                                    )
                                    val path = file?.absolutePath?.substringAfter(sd)?.split("/")
                                        ?.dropLast(1)
                                    var sdCardFiles = DocumentFile.fromTreeUri(
                                        context, Uri.parse(viewModel.userSettingsController.sdCardPath)
                                    )
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
                                    sdCardFiles?.createFile(
                                        "text/lrc", file.name
                                    )?.let {
                                        val outputStream = context.contentResolver.openOutputStream(it.uri)
                                        outputStream?.write(lrc.toByteArray())
                                        outputStream?.close()
                                    }
                                } else {
                                    throw e
                                }
                            }
                            successCount++
                        }
                    }
                    uiState = UiState.Done
                }
            }
        }

        UiState.Done -> {
            notificationManager.cancelAll()

            val notificationBuilder = NotificationCompat.Builder(context, channelID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.download_complete)).setContentText(
                    context.getString(
                        R.string.success_failed, successCount, noLyricsCount, failedCount
                    )
                ).setContentIntent(pendingIntent)

            notificationManager.notify(2, notificationBuilder.build())

            AlertDialog(title = {
                Text(text = stringResource(id = R.string.batch_download_lyrics))
            }, text = {
                Column {
                    Text(text = stringResource(R.string.download_complete))
                    Text(text = stringResource(R.string.success, successCount))
                    Text(text = stringResource(R.string.no_lyrics, noLyricsCount))
                    Text(text = stringResource(R.string.failed, failedCount))
                }
            }, onDismissRequest = {
                uiState = UiState.Cancelled
            }, confirmButton = {
                Button(onClick = { uiState = UiState.Cancelled }) {
                    Text(text = stringResource(id = R.string.ok))
                }
            })
        }

        UiState.RateLimited -> {
            notificationManager.cancelAll()

            AlertDialog(title = {
                Text(text = stringResource(id = R.string.batch_download_lyrics))
            }, text = {
                Column {
                    Text(text = stringResource(R.string.spotify_api_rate_limit_reached))
                    Text(text = stringResource(R.string.please_try_again_later))
                    Text(text = stringResource(R.string.change_api_strategy))
                }
            }, onDismissRequest = {
                uiState = UiState.Cancelled
            }, confirmButton = {
                Button(onClick = { uiState = UiState.Cancelled }) {
                    Text(text = stringResource(id = R.string.ok))
                }
            })
        }
    }
}

enum class UiState {
    Warning, LegacyPrompt, Pending, Done, RateLimited, Cancelled
}