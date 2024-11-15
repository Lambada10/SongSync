package pl.lambada.songsync.activities.quicksearch.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import pl.lambada.songsync.R
import pl.lambada.songsync.activities.quicksearch.QuickLyricsSearchActivity
import pl.lambada.songsync.domain.model.SongInfo

@Composable
fun QuickLyricsSongInfo(
    modifier: Modifier = Modifier,
    songInfo: SongInfo,
    imageLoader: ImageLoader = QuickLyricsSearchActivity.activityImageLoader
) {

    val imageUrl: String? by remember(songInfo.albumCoverLink) {
        mutableStateOf(songInfo.albumCoverLink)
    }

    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors().copy(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 2.dp,
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(72.dp)
                    .clip(MaterialTheme.shapes.small),
                model = imageUrl,
                contentDescription = stringResource(R.string.album_cover),
                imageLoader = imageLoader,
            )
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = songInfo.songName ?: stringResource(R.string.unknown),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Text(
                    text = songInfo.artistName ?: stringResource(R.string.unknown),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun TextWithIcon(
    icon: ImageVector,
    text: String,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null
        )
        Text(text = text, style = textStyle)
    }
}

@Preview
@Composable
private fun TextWithIconPreview() {
    TextWithIcon(
        icon = Icons.Rounded.MusicNote,
        text = "Song Name"
    )
}

@Preview
@Composable
private fun QuickLyricsSongInfoPreview() {
    QuickLyricsSongInfo(
        songInfo = SongInfo(
            songName = "Song Name",
            artistName = "Artist Name",
            albumCoverLink = "https://example.com/image.jpg"
        )
    )
}