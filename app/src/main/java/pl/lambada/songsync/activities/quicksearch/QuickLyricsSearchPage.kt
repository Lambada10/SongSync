package pl.lambada.songsync.activities.quicksearch

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.lambada.songsync.R
import pl.lambada.songsync.activities.quicksearch.components.ButtonWithIconAndText
import pl.lambada.songsync.activities.quicksearch.components.ErrorCard
import pl.lambada.songsync.activities.quicksearch.components.ExpandableOutlinedCard
import pl.lambada.songsync.activities.quicksearch.components.QuickLyricsSongInfo
import pl.lambada.songsync.activities.quicksearch.components.SyncedLyricsColumn
import pl.lambada.songsync.activities.quicksearch.viewmodel.QuickLyricsSearchViewModel
import pl.lambada.songsync.ui.common.AnimatedCardContentTransformation
import pl.lambada.songsync.util.ResourceState
import pl.lambada.songsync.util.ScreenState

@Composable
fun QuickLyricsSearchPage(
    state: State<QuickLyricsSearchViewModel.QuickSearchViewState>,
    onSendLyrics: (String) -> Unit
) {
    val lyricsState = state.value.lyricsState
    val parsedLyrics = state.value.parsedLyrics
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Crossfade(state.value.screenState) { pageState ->
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.value.song?.let { song ->
                    item {
                        Heading(
                            song = song,
                            lyricsState = lyricsState,
                            onSendLyrics = onSendLyrics
                        )
                    }
                }

                item {
                    HorizontalDivider()
                }

                item {
                    AnimatedContent(
                        modifier = Modifier.fillMaxWidth(),
                        targetState = pageState
                    ) { animatedPageState ->
                        when (animatedPageState) {
                            is ScreenState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }

                            is ScreenState.Success -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    animatedPageState.data?.let {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Cloud,
                                                    contentDescription = null,
                                                )
                                                Text(
                                                    text = stringResource(R.string.cloud_song).uppercase(),
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        letterSpacing = 1.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                )
                                            }

                                            QuickLyricsSongInfo(
                                                songInfo = animatedPageState.data,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        AnimatedContent(
                                            modifier = Modifier.fillMaxWidth(),
                                            transitionSpec = { AnimatedCardContentTransformation },
                                            targetState = state.value.lyricsState
                                        ) { lyricsState ->
                                            when (lyricsState) {
                                                is ResourceState.Loading<*> -> {
                                                    Box(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        LinearProgressIndicator(
                                                            modifier = Modifier.fillMaxWidth(
                                                                0.8f
                                                            )
                                                        )
                                                    }
                                                }

                                                is ResourceState.Success<*> -> {
                                                    lyricsState.data?.let { _ ->
                                                        ExpandableOutlinedCard(
                                                            title = stringResource(R.string.song_lyrics),
                                                            subtitle = stringResource(R.string.lyrics_subtitle),
                                                            icon = Icons.Rounded.Subtitles,
                                                            isExpanded = false,
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            SyncedLyricsColumn(
                                                                lyricsList = parsedLyrics,
                                                                modifier = Modifier
                                                                    .heightIn(
                                                                        min = 200.dp, max = 600.dp
                                                                    )
                                                                    .fillMaxWidth()
                                                                    .padding(8.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                is ResourceState.Error<*> -> {
                                                    ErrorCard(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .heightIn(max = 300.dp),
                                                        stacktrace = lyricsState.message ?: ""
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            is ScreenState.Error -> {
                                ErrorCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 300.dp),
                                    stacktrace = animatedPageState.exception.message
                                        ?: animatedPageState.exception.stackTrace.toString()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Heading(
    song: Pair<String, String>,
    lyricsState: ResourceState<String>,
    onSendLyrics: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.showing_lyrics_for).uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    letterSpacing = 2.sp
                )
            )
            Text(
                text = song.first, style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.by))
                    append(" ")
                    withStyle(MaterialTheme.typography.titleMedium.toSpanStyle()) {
                        append(song.second)
                    }
                },
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            with(lyricsState) {
                ButtonWithIconAndText(
                    icon = Icons.AutoMirrored.Rounded.Send,
                    text = stringResource(R.string.accept),
                    modifier = Modifier.weight(1f),
                    onClick = { onSendLyrics(this.data ?: "") },
                    enabled = this is ResourceState.Success<*>,
                    shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                )
                ButtonWithIconAndText(
                    icon = Icons.Filled.Settings,
                    text = stringResource(R.string.settings),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                )
            }
        }
    }
}