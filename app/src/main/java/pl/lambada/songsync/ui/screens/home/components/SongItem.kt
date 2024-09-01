package pl.lambada.songsync.ui.screens.home.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import pl.lambada.songsync.R
import pl.lambada.songsync.domain.model.Song
import pl.lambada.songsync.ui.ScreenSearch
import pl.lambada.songsync.ui.components.AnimatedText
import pl.lambada.songsync.ui.screens.home.HomeViewModel


@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SongItem(
    id: String,
    selected: Boolean,
    quickSelect: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    navController: NavHostController,
    song: Song,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: HomeViewModel,
) {
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current).data(data = song.imgUri).apply {
            placeholder(R.drawable.ic_song)
            error(R.drawable.ic_song)
        }.build(), imageLoader = LocalContext.current.imageLoader
    )
    val songName = song.title ?: stringResource(id = R.string.unknown)
    val artists = song.artist ?: stringResource(id = R.string.unknown)
    val bgColor = if (selected) MaterialTheme.colorScheme.surfaceVariant
    else MaterialTheme.colorScheme.surface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .background(bgColor)
            .combinedClickable(
                onClick = {
                    if (quickSelect) {
                        onSelectionChanged(!selected)
                    } else {
                        navController.navigate(
                            ScreenSearch(
                                id = id,
                                songName = songName,
                                artists = artists,
                                coverUri = song.imgUri.toString(),
                                filePath = song.filePath,
                            )
                        )
                    }
                },
                onLongClick = { onSelectionChanged(!selected) }
            )
            .padding(vertical = 12.dp, horizontal = 24.dp)
    ) {
        with(sharedTransitionScope) {
            Image(
                painter = painter,
                contentDescription = stringResource(id = R.string.album_cover),
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "cover$id"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        clipInOverlayDuringTransition = sharedTransitionScope.OverlayClip(
                            RoundedCornerShape(20f)
                        )
                    )
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20f))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                AnimatedText(
                    animate = !viewModel.userSettingsController.disableMarquee,
                    text = songName,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.contentColorFor(bgColor),
                    modifier = Modifier.sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "title$id"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                )
                AnimatedText(
                    animate = !viewModel.userSettingsController.disableMarquee,
                    text = artists,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.contentColorFor(bgColor),
                    modifier = Modifier.sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "artist$id"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                )
            }
        }
    }
}