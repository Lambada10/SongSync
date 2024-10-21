package pl.lambada.songsync.ui.screens.lyricsFetch.components

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pl.lambada.songsync.R
import pl.lambada.songsync.util.ext.repeatingClickable


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LyricsSuccessContent(
    lyrics: String,
    offset: Int,
    onSetOffset: (Int) -> Unit,
    onSaveLyrics: () -> Unit,
    onEmbedLyrics: () -> Unit,
    onCopyLyrics: () -> Unit
) {


    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Exposure,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = stringResource(R.string.offset),
                modifier = Modifier.padding(start = 6.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(
                onClick = { /* handled by repeatingClickable */ },
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp)) // otherwise square ripple
                    .repeatingClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        enabled = true,
                        maxDelayMillis = 500,
                        onClick = { onSetOffset(offset - 100) }
                    )
            ) {
                Text(text = "-0.1s")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = (if (offset >= 0) "+" else "") +
                    "${offset / 1000.0}s",
            )
            Spacer(modifier = Modifier.width(10.dp))
            OutlinedButton(
                onClick = { /* handled by repeatingClickable */ },
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp)) // otherwise square ripple
                    .repeatingClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        enabled = true,
                        maxDelayMillis = 500,
                        onClick = { onSetOffset(offset + 100) }
                    )
            ) {
                Text(text = "+0.1s")
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

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
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.W500)
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
