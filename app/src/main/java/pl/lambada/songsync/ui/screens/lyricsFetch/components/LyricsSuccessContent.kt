package pl.lambada.songsync.ui.screens.lyricsFetch.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.lambada.songsync.R


@Composable
fun LyricsSuccessContent(
    lyrics: String,
    onSaveLyrics: (String) -> Unit,
    onEmbedLyrics: (String) -> Unit,
    onCopyLyrics: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = { onSaveLyrics(lyrics) }) {
            Text(text = stringResource(R.string.save_lrc_file))
        }
        Button(onClick = { onEmbedLyrics(lyrics) }) {
            Text(text = stringResource(R.string.embed_lyrics_in_file))
        }
    }

    OutlinedButton(onClick = onCopyLyrics) {
        Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = stringResource(R.string.copy_lyrics_to_clipboard)
        )
    }

    Spacer(modifier = Modifier.height(6.dp))
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    ) {
        SelectionContainer {
            Text(text = lyrics, modifier = Modifier.padding(8.dp))
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}
