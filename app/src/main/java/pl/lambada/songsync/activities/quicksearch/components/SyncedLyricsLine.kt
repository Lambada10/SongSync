package pl.lambada.songsync.activities.quicksearch.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SyncedLyricsLine(
    time: String,
    lyrics: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        val formattedTime = buildAnnotatedString {
            append(time.substring(0, time.length - 4))
            withStyle(
                style = SpanStyle(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            ) {
                append(time.takeLast(4))
            }
        }
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = lyrics,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SyncedLyricsColumn(
    lyricsList: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        lyricsList.forEach { (time, lyrics) ->
            SyncedLyricsLine(time = time, lyrics = lyrics)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}