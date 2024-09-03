package pl.lambada.songsync.ui.screens.home.components.batchDownload

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import pl.lambada.songsync.R

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