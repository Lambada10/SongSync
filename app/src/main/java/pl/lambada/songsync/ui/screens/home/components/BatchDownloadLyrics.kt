package pl.lambada.songsync.ui.screens.home.components

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import pl.lambada.songsync.R
import pl.lambada.songsync.domain.model.Song
import pl.lambada.songsync.domain.model.SongInfo
import pl.lambada.songsync.ui.screens.home.HomeViewModel
import pl.lambada.songsync.ui.screens.home.components.batchDownload.BatchDownloadWarningDialog
import pl.lambada.songsync.ui.screens.home.components.batchDownload.DownloadCompleteDialog
import pl.lambada.songsync.ui.screens.home.components.batchDownload.DownloadProgressDialog
import pl.lambada.songsync.ui.screens.home.components.batchDownload.LegacyPromptDialog
import pl.lambada.songsync.ui.screens.home.components.batchDownload.RateLimitedDialog
import pl.lambada.songsync.util.ext.getVersion
import pl.lambada.songsync.util.ext.toLrcFile
import pl.lambada.songsync.util.generateLrcContent
import pl.lambada.songsync.util.writeLyricsToFile
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
                    onProgressUpdate = { newSuccessCount, newNoLyricsCount, newFailedCount ->
                        successCount = newSuccessCount
                        noLyricsCount = newNoLyricsCount
                        failedCount = newFailedCount
                    },
                    onDownloadComplete = { uiState = UiState.Done },
                    onRateLimitReached = { uiState = UiState.RateLimited },
                    sdCardPath = viewModel.userSettingsController.sdCardPath
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

suspend fun downloadLyrics(
    songs: List<Song>,
    viewModel: HomeViewModel,
    context: Context,
    onProgressUpdate: (successCount: Int, noLyricsCount: Int, failedCount: Int) -> Unit,
    onDownloadComplete: () -> Unit,
    onRateLimitReached: () -> Unit,
    sdCardPath: String?
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
            sdCardPath,
            onSaved = { successCount++ },
            onNoLyrics = { noLyricsCount++ },
            onFailed = {
                failedCount++
                consecutiveNotFound++
                if (consecutiveNotFound >= 5) onRateLimitReached()
            },
            onProcessingQueryResult = { consecutiveNotFound = 0 }
        )

        onProgressUpdate(successCount, noLyricsCount, failedCount)
    }

    onDownloadComplete()
}

private suspend fun downloadLyricsForSong(
    song: Song,
    viewModel: HomeViewModel,
    context: Context,
    sdCardPath: String?,
    onSaved: () -> Unit,
    onNoLyrics: () -> Unit,
    onFailed: (Throwable) -> Unit,
    onProcessingQueryResult: () -> Unit
) {
    val file = song.filePath.toLrcFile()

    runCatching {
        viewModel.getSongInfo(SongInfo(song.title, song.artist))
            ?: throw NullPointerException("Song info result is null")
    }
        .onFailure(onFailed)
        .onSuccess { queryResult ->
            onProcessingQueryResult()

            runCatching {
                viewModel.getSyncedLyrics(
                    link = queryResult.songLink ?: error("attempted to get lyrics without link"),
                    version = context.getVersion()
                ) ?: throw NullPointerException("Lyrics result is null")
            }
                .onFailure {
                    if (it is NullPointerException || it is FileNotFoundException)
                        onNoLyrics()
                    else
                        onFailed(it)
                }
                .onSuccess { lyricsResult ->
                    val lrcContent = generateLrcContent(
                        queryResult,
                        lyricsResult,
                        context.getString(R.string.generated_using)
                    )

                    writeLyricsToFile(file, lrcContent, context, song, sdCardPath)

                    onSaved()
                }
        }
}

enum class UiState {
    Warning, LegacyPrompt, Pending, Done, RateLimited, Cancelled
}