package pl.lambada.songsync.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONException
import pl.lambada.songsync.R
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.data.Song
import pl.lambada.songsync.data.SongInfo
import pl.lambada.songsync.data.SongListSaver
import pl.lambada.songsync.data.SongSaver
import pl.lambada.songsync.data.ext.lowercaseWithLocale
import pl.lambada.songsync.ui.components.MarqueeText
import java.io.File
import java.io.FileNotFoundException
import java.net.UnknownHostException
import kotlin.math.roundToInt

/**
 * Composable function representing the home screen.
 *
 * @param viewModel The [MainViewModel] instance.
 */
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    var uiState by rememberSaveable { mutableStateOf(UiState.Loading) }
    val context = LocalContext.current
    var songs by rememberSaveable(
        stateSaver = SongListSaver,
        init = { mutableStateOf(emptyList()) }
    )

    when (uiState) {
        UiState.Loading -> {
            LoadingScreen(onLoadingComplete = {
                songs = viewModel.getAllSongs(context)
                uiState = UiState.Loaded
            })
        }

        UiState.Loaded -> HomeScreenLoaded(
            viewModel = viewModel,
            songs = songs
        )

        else -> {
            Text(text = stringResource(id = R.string.unreachable_state))
        }
    }
}

/**
 * Composable function representing the loading screen.
 *
 * @param onLoadingComplete Callback invoked when the loading is complete.
 */
@Composable
fun LoadingScreen(onLoadingComplete: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        LaunchedEffect(Unit) {
            onLoadingComplete()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun HomeScreenLoaded(viewModel: MainViewModel, songs: List<Song>) {
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }

    var isBatchDownload by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 8.dp, end = 8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
            ) {
                Row {
                    Button(onClick = { isBatchDownload = true }) {
                        Text(text = stringResource(R.string.batch_download_lyrics))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Divider()

                if (isBatchDownload) {
                    BatchDownloadLyrics(
                        songs = songs,
                        viewModel = viewModel,
                        onDone = { isBatchDownload = false }
                    )
                }

                if (showSearch) {
                    val keyboardController = LocalSoftwareKeyboardController.current
                    SearchBar(
                        query = query,
                        onQueryChange = { query = it },
                        onSearch = { keyboardController?.hide() },
                        active = false,
                        onActiveChange = {},
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        content = {}
                    )
                }
            }
        }

        items(songs.size) {
            val song = songs[it]
            val songTitleLowercase = song.title?.lowercaseWithLocale()
            val songArtistLowercase = song.artist?.lowercaseWithLocale()
            val queryLowercase = query.lowercaseWithLocale()

            if (songTitleLowercase?.contains(queryLowercase) == true || songArtistLowercase?.contains(
                    queryLowercase
                ) == true
            ) {
                SongItem(song = song, viewModel = viewModel)
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SongItem(song: Song, viewModel: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val unknownString = stringResource(id = R.string.unknown)
    val noLyricsString = stringResource(id = R.string.lyrics_not_found)

    var fetch by rememberSaveable { mutableStateOf(false) }

    OutlinedCard(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                fetch = true
            }
    ) {
        val painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current).data(data = song.imgUri)
                .apply {
                    placeholder(0)
                }.build(),
            imageLoader = LocalContext.current.imageLoader
        )

        val savedSong by rememberSaveable(stateSaver = SongSaver) {
            mutableStateOf(song)
        }

        Row(modifier = Modifier.height(72.dp)) {
            Image(
                painter = painter,
                contentDescription = "Album cover",
                modifier = Modifier
                    .height(72.dp)
                    .aspectRatio(1f),
            )
            Spacer(modifier = Modifier.width(2.dp))
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.Top) {
                MarqueeText(text = savedSong.title ?: unknownString, fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                MarqueeText(text = savedSong.artist ?: unknownString, fontSize = 14.sp)
            }
        }
    }

    if (fetch) {
        var queryStatus by rememberSaveable { mutableStateOf(QueryStatus.NotSubmitted) }
        val query = SongInfo(songName = song.title ?: "", artistName = song.artist ?: "")
        var offset by rememberSaveable { mutableIntStateOf(0) }
        var queryResult by remember { mutableStateOf(SongInfo()) }
        var failReason by rememberSaveable { mutableStateOf("") }
        var lyricsResult by rememberSaveable { mutableStateOf("") }

        when (queryStatus) {
            QueryStatus.Cancelled -> {
                fetch = false
            }

            QueryStatus.NotSubmitted -> {
                AlertDialog(
                    title = {
                        Text(text = stringResource(R.string.get_lyrics))
                    },
                    text = {
                        Text(
                            text = stringResource(
                                R.string.selected_song_by,
                                song.title ?: unknownString,
                                song.artist ?: unknownString
                            )
                        )
                    },
                    onDismissRequest = {
                        queryStatus = QueryStatus.Cancelled
                    },
                    confirmButton = {
                        Button(onClick = {
                            scope.launch(Dispatchers.IO) {
                                queryStatus = QueryStatus.Pending
                                try {
                                    queryResult = viewModel.getSongInfo(query, offset)
                                    queryStatus = QueryStatus.Success
                                } catch (e: Exception) {
                                    when (e) {
                                        is UnknownHostException -> {
                                            queryStatus = QueryStatus.NoConnection
                                        }
                                        else -> {
                                            failReason = e.toString()
                                            queryStatus = QueryStatus.Failed
                                        }
                                    }
                                }
                            }
                        }) {
                            Text(text = stringResource(R.string.get_song_info))
                        }
                    }
                )
            }

            QueryStatus.Pending -> {
                AlertDialog(
                    title = {
                        Text(text = stringResource(R.string.fetching_song_info))
                    },
                    text = {
                        Text(
                            text = stringResource(
                                R.string.fetching_song_info_for_by,
                                song.title ?: unknownString,
                                song.artist ?: unknownString
                            )
                        )
                    },
                    onDismissRequest = {
                        queryStatus = QueryStatus.Cancelled
                    },
                    confirmButton = {
                        // no button but compose cries when I don't use confirmButton
                    }
                )
            }

            QueryStatus.Success -> {
                val songNameResult by rememberSaveable { mutableStateOf(queryResult.songName) }
                val artistNameResult by rememberSaveable { mutableStateOf(queryResult.artistName) }

                AlertDialog(
                    title = {
                        Text(text = stringResource(R.string.song_info_fetched))
                    },
                    text = {
                        Column {
                            Text(text = stringResource(R.string.song_name, songNameResult ?: unknownString))
                            Text(text = stringResource(R.string.artist_name, artistNameResult ?: unknownString))
                            Text(text = stringResource(R.string.is_that_correct))
                        }
                    },
                    onDismissRequest = {
                        queryStatus = QueryStatus.Cancelled
                    },
                    confirmButton = {
                        Button(onClick = {
                            scope.launch(Dispatchers.Default) {
                                queryStatus = QueryStatus.LyricsPending
                                try {
                                    lyricsResult = viewModel.getSyncedLyrics(queryResult.songLink.toString())
                                    queryStatus = QueryStatus.LyricsSuccess
                                } catch (e: Exception) {
                                    if (e is FileNotFoundException) {
                                        lyricsResult = noLyricsString
                                    } else {
                                        failReason = e.toString()
                                        queryStatus = QueryStatus.LyricsFailed
                                    }
                                }
                            }
                        }) {
                            Text(text = stringResource(id = R.string.get_lyrics))
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = {
                            offset += 1
                            scope.launch(Dispatchers.IO) {
                                queryStatus = QueryStatus.Pending
                                try {
                                    queryResult = viewModel.getSongInfo(query, offset)
                                    queryStatus = QueryStatus.Success
                                } catch (e: Exception) {
                                    failReason = e.toString()
                                    queryStatus = QueryStatus.Failed
                                }
                            }
                        }) {
                            Text(text = stringResource(R.string.try_again))
                        }
                    }
                )
            }

            QueryStatus.LyricsPending -> {
                AlertDialog(
                    title = {
                        Text(text = stringResource(R.string.fetching_lyrics))
                    },
                    text = {
                        Text(
                            text = stringResource(
                                R.string.fetching_lyrics_for_by,
                                queryResult.songName ?: unknownString,
                                queryResult.artistName ?: unknownString
                            )
                        )
                    },
                    onDismissRequest = {
                        queryStatus = QueryStatus.Cancelled
                    },
                    confirmButton = {
                        // no button but compose cries when I don't use confirmButton
                    }
                )
            }

            QueryStatus.LyricsSuccess -> {
                AlertDialog(
                    title = {
                        Text(text = stringResource(R.string.lyrics_fetched))
                    },
                    text = {
                        Column {
                            Text(text = stringResource(R.string.first_line_of_lyrics))
                            Text(text = lyricsResult.split("\n")[0])
                            Text(text = stringResource(id = R.string.is_that_correct))
                        }
                    },
                    onDismissRequest = {
                        queryStatus = QueryStatus.Cancelled
                    },
                    confirmButton = {
                        Button(onClick = {
                            val lrc =
                                "[ti:${queryResult.songName}]\n" +
                                        "[ar:${queryResult.artistName}]\n" +
                                        "[by:${context.getString(R.string.generated_using)}]\n" +
                                        lyricsResult

                            val file = File(song.filePath?.dropLast(4) + ".lrc")
                            file.writeText(lrc)

                            Toast.makeText(
                                context,
                                "${context.getString(R.string.lyrics_saved_to)} ${file.path}",
                                Toast.LENGTH_LONG
                            ).show()

                            queryStatus = QueryStatus.Cancelled
                        }) {
                            Text(text = stringResource(R.string.save_lyrics))
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = {
                            queryStatus = QueryStatus.Cancelled
                        }) {
                            Text(text = stringResource(R.string.cancel))
                        }
                    }
                )
            }

            QueryStatus.LyricsFailed -> {
                AlertDialog(
                    title = {
                        Text(text = stringResource(R.string.error))
                    },
                    text = {
                        Text(text = stringResource(R.string.this_track_has_no_lyrics))
                    },
                    onDismissRequest = {
                        queryStatus = QueryStatus.Cancelled
                    },
                    confirmButton = {
                        Button(onClick = {
                            queryStatus = QueryStatus.Cancelled
                        }) {
                            Text(text = stringResource(R.string.ok))
                        }
                    }
                )
            }

            QueryStatus.Failed -> {
                AlertDialog(
                    onDismissRequest = { queryStatus = QueryStatus.Cancelled },
                    confirmButton = {
                        Button(onClick = { queryStatus = QueryStatus.Cancelled }) {
                            Text(text = stringResource(id = R.string.ok))
                        }
                    },
                    title = { Text(text = stringResource(id = R.string.error)) },
                    text = {
                        if (failReason.contains("NotFound") || failReason.contains("JSON")) {
                            Text(text = stringResource(id = R.string.no_results))
                        } else {
                            Text(text = stringResource(R.string.an_error_occurred, failReason))
                        }
                    }
                )
            }

            QueryStatus.NoConnection -> {
                AlertDialog(
                    onDismissRequest = { queryStatus = QueryStatus.Cancelled },
                    confirmButton = {
                        Button(onClick = { queryStatus = QueryStatus.Cancelled }) {
                            Text(text = stringResource(id = R.string.ok))
                        }
                    },
                    title = { Text(text = stringResource(id = R.string.error)) },
                    text = {
                        Text(text = stringResource(R.string.no_internet_server))
                    }
                )
            }
        }
    }
}

@Composable
fun BatchDownloadLyrics(songs: List<Song>, viewModel: MainViewModel, onDone: () -> Unit) {
    val unknownString = stringResource(id = R.string.unknown)
    val generatedUsingString = stringResource(id = R.string.generated_using)

    var uiState by rememberSaveable { mutableStateOf(UiState.Warning) }
    var failedCount by rememberSaveable { mutableIntStateOf(0) }
    var successCount by rememberSaveable { mutableIntStateOf(0) }
    val total = songs.size

    when (uiState) {
        UiState.Cancelled -> {
            onDone()
        }

        UiState.Warning -> {
            AlertDialog(
                title = {
                    Text(text = stringResource(id = R.string.batch_download_lyrics))
                },
                text = {
                    Column {
                        Text(text = stringResource(R.string.this_will_download_lyrics_for_all_songs))
                        Text(text = stringResource(R.string.existing_lyrics_for_songs_overwrite))
                        Text(text = stringResource(R.string.less_accurate))
                        Text(text = stringResource(R.string.sure_to_continue))
                    }
                },
                onDismissRequest = {
                    uiState = UiState.Cancelled
                },
                confirmButton = {
                    Button(onClick = { uiState = UiState.Pending }) {
                        Text(text = stringResource(R.string.yes))
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { uiState = UiState.Cancelled }) {
                        Text(text = stringResource(R.string.no))
                    }
                }
            )
        }

        UiState.Pending -> {
            val totalCount = successCount + failedCount
            val percentage = if (total != 0) {
                (totalCount.toFloat() / total.toFloat() * 100).roundToInt()
            } else {
                0 // In other cases = 0
            }

            AlertDialog(
                title = {
                    Text(text = stringResource(id = R.string.batch_download_lyrics))
                },
                text = {
                    Column {
                        Text(text = stringResource(R.string.downloading_lyrics))
                        MarqueeText(
                            stringResource(
                                R.string.song,
                                songs[(successCount + failedCount) % total].title ?: unknownString,
                            )
                        )
                        Text(text = stringResource(R.string.progress, totalCount, total, percentage))
                        Text(text = stringResource(R.string.success_failed, successCount, failedCount))
                        Text(text = stringResource(R.string.please_do_not_close_the_app_this_may_take_a_while))
                    }
                },
                onDismissRequest = {
                    uiState = UiState.Cancelled
                },
                confirmButton = {
                    // no button but compose cries when I don't use confirmButton
                },
                dismissButton = {
                    OutlinedButton(onClick = { uiState = UiState.Cancelled }) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            )

            var notFoundInARow by remember { mutableIntStateOf(0) }
            var downloadJob by remember { mutableStateOf<Job?>(null) }

            LaunchedEffect(true) {
                downloadJob = launch(Dispatchers.IO) {
                    for (song in songs) {
                        if (uiState == UiState.Cancelled) {
                            downloadJob?.cancel()
                            return@launch
                        }

                        if (successCount + failedCount >= total) {
                            uiState = UiState.Done
                            downloadJob?.cancel()
                            return@launch
                        }

                        val query = SongInfo(song.title, song.artist)
                        var queryResult: SongInfo? = null
                        try {
                            queryResult = viewModel.getSongInfo(query)
                        } catch (e: Exception) {
                            if (e is FileNotFoundException) {
                                notFoundInARow++
                                failedCount++
                                if (notFoundInARow >= 5) {
                                    uiState = UiState.RateLimited
                                    return@launch
                                }
                                continue
                            }
                            if (e is JSONException) {
                                failedCount++
                                continue
                            }
                        } finally {
                            notFoundInARow = 0
                        }
                        val lyricsResult: String
                        try {
                            lyricsResult = viewModel.getSyncedLyrics(queryResult?.songLink!!)
                        } catch (e: FileNotFoundException) {
                            failedCount++
                            continue
                        }
                        val lrc =
                            "[ti:${queryResult.songName}]\n" +
                                    "[ar:${queryResult.artistName}]\n" +
                                    "[by:$generatedUsingString]\n" +
                                    lyricsResult

                        val file = File(song.filePath!!.dropLast(4) + ".lrc")
                        file.writeText(lrc)

                        successCount++
                    }
                    uiState = UiState.Done
                }
            }
        }

        UiState.Done -> {
            AlertDialog(
                title = {
                    Text(text = stringResource(id = R.string.batch_download_lyrics))
                },
                text = {
                    Column {
                        Text(text = stringResource(R.string.download_complete))
                        Text(text = stringResource(R.string.success, successCount))
                        Text(text = stringResource(R.string.failed, failedCount))
                    }
                },
                onDismissRequest = {
                    uiState = UiState.Cancelled
                },
                confirmButton = {
                    Button(onClick = { uiState = UiState.Cancelled }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            )
        }

        UiState.RateLimited -> {
            AlertDialog(
                title = {
                    Text(text = stringResource(id = R.string.batch_download_lyrics))
                },
                text = {
                    Column {
                        Text(text = stringResource(R.string.spotify_api_rate_limit_reached))
                        Text(text = stringResource(R.string.please_try_again_later))
                    }
                },
                onDismissRequest = {
                    uiState = UiState.Cancelled
                },
                confirmButton = {
                    Button(onClick = { uiState = UiState.Cancelled }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            )
        }

        else -> {
            // nothing because we do not use the other states
        }
    }
}

enum class QueryStatus {
    NotSubmitted, Pending, Success, Failed, Cancelled, LyricsPending, LyricsSuccess, LyricsFailed, NoConnection
}

enum class UiState {
    Warning, Pending, Done, RateLimited, Cancelled, Loading, Loaded
}