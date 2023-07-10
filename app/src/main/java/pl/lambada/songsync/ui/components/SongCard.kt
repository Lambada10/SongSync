package pl.lambada.songsync.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.CombinedModifier
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import pl.lambada.songsync.R

@Composable
fun SongCard(
    modifier: Modifier = Modifier,
    songName: String,
    artists: String,
    coverUrl: String?,
) {
    OutlinedCard(
        shape = RoundedCornerShape(10.dp),
        modifier = CombinedModifier(
            outer = Modifier.
                fillMaxWidth().
                padding(8.dp),
            inner = modifier
        )
    ) {
        Row(modifier = Modifier.height(72.dp)) {
            if (coverUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = coverUrl),
                    contentDescription = stringResource(R.string.album_cover),
                    modifier = Modifier
                        .height(72.dp)
                        .aspectRatio(1f),
                )
            }
            Spacer(modifier = Modifier.width(2.dp))
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.Top
            ) {
                MarqueeText(text = songName, fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                MarqueeText(text = artists, fontSize = 14.sp)
            }
        }
    }
}