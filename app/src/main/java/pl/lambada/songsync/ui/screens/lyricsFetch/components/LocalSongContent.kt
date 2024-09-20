package pl.lambada.songsync.ui.screens.lyricsFetch.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.lambada.songsync.R
import pl.lambada.songsync.ui.LocalSong
import pl.lambada.songsync.ui.components.SongCard

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.LocalSongContent(
    song: LocalSong,
    animatedVisibilityScope: AnimatedVisibilityScope,
    disableMarquee: Boolean
) {
    Row {
        Icon(
            imageVector = Icons.Filled.Downloading,
            contentDescription = null,
            Modifier.padding(end = 5.dp)
        )
        Text(stringResource(R.string.local_song))
    }
    Spacer(modifier = Modifier.height(6.dp))
    SongCard(
        filePath = song.filePath,
        songName = song.songName,
        artists = song.artists,
        coverUrl = song.coverUri,
        animatedVisibilityScope = animatedVisibilityScope,
        animateText = !disableMarquee,
    )
}
