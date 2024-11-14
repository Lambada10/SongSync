package pl.lambada.songsync.activities.quicksearch

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.lambada.songsync.activities.quicksearch.viewmodel.QuickLyricsSearchViewModel
import pl.lambada.songsync.util.ResourceState
import pl.lambada.songsync.util.ScreenState

@Composable
fun QuickLyricsSearchPage(
    state: State<QuickLyricsSearchViewModel.QuickSearchViewState>,
    onSendLyrics: (String) -> Unit
) {
    Crossfade(state.value.screenState) { pageState ->
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Search query: ${state.value.song}")
            when(pageState) {
                is ScreenState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ScreenState.Success -> {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Success: ${pageState.data}")
                        when(state.value.lyricsState) {
                            is ResourceState.Loading<*> -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                            is ResourceState.Success<*> -> {
                                state.value.lyricsState.data?.let { obtainedLyrics -> //This crunches the animation lol
                                    Button(onClick = { onSendLyrics(obtainedLyrics) }) {
                                        Text("Send lyrics back")
                                    }
                                    Text("Lyrics: $obtainedLyrics")
                                }
                            }
                            is ResourceState.Error<*> -> {
                                Text("Error: ${state.value.lyricsState.message}")
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