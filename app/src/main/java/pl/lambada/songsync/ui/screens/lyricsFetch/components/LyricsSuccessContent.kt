package pl.lambada.songsync.ui.screens.lyricsFetch.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.lambada.songsync.R


@Composable
fun LyricsSuccessContent(
    lyrics: String,
    onSaveLyrics: () -> Unit,
    onEmbedLyrics: () -> Unit,
    onCopyLyrics: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onSaveLyrics) {
                Text(text = stringResource(R.string.save_lrc_file))
            }

            Button(onClick = onEmbedLyrics) {
                Text(text = stringResource(R.string.embed_lyrics_in_file))
            }
        }


        Spacer(modifier = Modifier.height(6.dp))

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column {
                Row(
                    Modifier
                        .clickable(onClick = onCopyLyrics)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    CopyToClipboardElement()
                }
                HorizontalDivider()

                SelectionContainer {
                    Text(text = lyrics, modifier = Modifier.padding(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun CopyToClipboardElement(modifier: Modifier = Modifier) {
    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.outline
    ) {
        Row(
            modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.copy_lyrics_to_clipboard),
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(Modifier.width(4.dp))

            Icon(
                Icons.Outlined.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
