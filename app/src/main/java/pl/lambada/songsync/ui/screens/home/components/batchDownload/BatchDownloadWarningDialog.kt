package pl.lambada.songsync.ui.screens.home.components.batchDownload

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.lambada.songsync.R
import pl.lambada.songsync.ui.components.SwitchItem

@Composable
fun BatchDownloadWarningDialog(
    songsCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    embedLyrics: Boolean,
    onEmbedLyricsChangeRequest: (Boolean) -> Unit,
) {
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
                Spacer(modifier = Modifier.height(16.dp))
                SwitchItem(
                    label = stringResource(R.string.embed_lyrics_in_file),
                    selected = embedLyrics,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50f))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    innerPaddingValues = PaddingValues(
                        top = 8.dp,
                        start = 18.dp,
                        end = 10.dp,
                        bottom = 8.dp
                    )
                ) { onEmbedLyricsChangeRequest(!embedLyrics) }
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