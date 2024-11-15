package pl.lambada.songsync.activities.quicksearch

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import pl.lambada.songsync.activities.quicksearch.components.ExpandableOutlinedCard
import pl.lambada.songsync.activities.quicksearch.components.QuickLyricsSongInfo
import pl.lambada.songsync.activities.quicksearch.components.SyncedLyricsColumn
import pl.lambada.songsync.activities.quicksearch.viewmodel.QuickLyricsSearchViewModel
import pl.lambada.songsync.util.ResourceState
import pl.lambada.songsync.util.ScreenState

@Composable
fun QuickLyricsSearchPage(
    state: State<QuickLyricsSearchViewModel.QuickSearchViewState>,
    onSendLyrics: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Crossfade(state.value.screenState) { pageState ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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

                        state.value.song?.let { song ->
                            Text(
                                text = song.first,
                                style = MaterialTheme.typography.headlineSmall
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
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ButtonWithIconAndText(
                            icon = Icons.AutoMirrored.Rounded.Send,
                            text = stringResource(R.string.accept),
                            modifier = Modifier
                                .weight(1f),
                            onClick = { onSendLyrics(state.value.lyricsState.data!!) },
                            enabled = state.value.lyricsState is ResourceState.Success,
                            shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                        )
                        ButtonWithIconAndText(
                            icon = Icons.Filled.Settings,
                            text = stringResource(R.string.settings),
                            modifier = Modifier
                                .weight(1f),
                            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                        )
                    }
                }

                HorizontalDivider()

                when (pageState) {
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
                            pageState.data?.let {
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
                                        songInfo = pageState.data,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                when (state.value.lyricsState) {
                                    is ResourceState.Loading<*> -> {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }

                                    is ResourceState.Success<*> -> {
                                        state.value.lyricsState.data?.let { _ -> //This crunches the animation lol
                                            ExpandableOutlinedCard(
                                                title = stringResource(R.string.song_lyrics),
                                                subtitle = stringResource(R.string.lyrics_subtitle),
                                                icon = Icons.Rounded.Subtitles,
                                                isExpanded = false,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                SyncedLyricsColumn(
                                                    lyricsList = state.value.parsedLyrics,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(8.dp)
                                                )
                                            }
                                        }
                                    }

                                    is ResourceState.Error<*> -> {
                                        Text("Error: ${state.value.lyricsState.message}")
                                    }
                                }
                            }

                        }
                    }

                    is ScreenState.Error -> {
                        Text("Error: ${pageState.exception.message}")
                    }
                }
            }
        }
    }
}