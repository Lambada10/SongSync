package pl.lambada.songsync.ui.screens.home.components.batchDownload

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import pl.lambada.songsync.R

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