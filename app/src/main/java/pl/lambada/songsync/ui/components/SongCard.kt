package pl.lambada.songsync.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import pl.lambada.songsync.R

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.SongCard(
    id: String,
    animateText: Boolean,
    songName: String,
    artists: String,
    coverUrl: String?,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    OutlinedCard(
        shape = RoundedCornerShape(10.dp),
        modifier = CombinedModifier(
            outer = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            inner = modifier
        )
    ) {
        Row(modifier = Modifier.height(72.dp)) {
            if (coverUrl != null) {
                val painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current).data(data = coverUrl)
                        .apply {
                            placeholder(R.drawable.ic_song)
                            error(R.drawable.ic_song)
                        }.build(),
                    imageLoader = LocalContext.current.imageLoader
                )
                Image(
                    painter = painter,
                    contentDescription = stringResource(R.string.album_cover),
                    modifier = Modifier
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "cover$id"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            clipInOverlayDuringTransition = sharedTransitionScope.OverlayClip(
                                RoundedCornerShape(
                                    topStart = 30f,
                                    bottomStart = 30f,
                                    topEnd = 0f,
                                    bottomEnd = 0f
                                )
                            )
                        )
                        .height(72.dp)
                        .aspectRatio(1f)
                        .clip(
                            RoundedCornerShape(
                                topStart = 30f,
                                bottomStart = 30f,
                                topEnd = 0f,
                                bottomEnd = 0f
                            )
                        )
                )
            }
            Spacer(modifier = Modifier.width(2.dp))
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.Top
            ) {
                AnimatedText(
                    text = songName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    animate = animateText,
                    modifier = Modifier.sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "title$id"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                AnimatedText(
                    text = artists,
                    fontSize = 14.sp,
                    animate = animateText,
                    modifier = Modifier.sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "artist$id"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                )
            }
        }
    }
}