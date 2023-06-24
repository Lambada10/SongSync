package pl.lambada.songsync.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import org.json.JSONException
import pl.lambada.songsync.R
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.data.Song
import pl.lambada.songsync.data.SongInfo
import pl.lambada.songsync.data.SongSaver
import pl.lambada.songsync.data.ext.lowercaseWithLocale
import pl.lambada.songsync.ui.common.MarqueeText
import java.io.File
import java.io.FileNotFoundException
import java.net.UnknownHostException
import java.util.concurrent.Executors
import kotlin.math.roundToInt


@Composable
fun HomeScreen(viewModel: MainViewModel) {
    var uiState by rememberSaveable { mutableStateOf(UiState.Loading) }
    val context = LocalContext.current
    var songs: List<Song> = viewModel.getAllSongs(context)
    when (uiState) {
        UiState.Loading -> Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Thread {
                songs = viewModel.getAllSongs(context)
                uiState = UiState.Loaded
            }.start()
        }

        UiState.Loaded -> HomeScreenLoaded(viewModel = viewModel, songs = songs)

        else -> {
            Text(text = stringResource(id = R.string.unreachable_state))
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
                        dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow
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

                if (isBatchDownload) BatchDownloadLyrics(songs = songs,
                    viewModel = viewModel,
                    onDone = { isBatchDownload = false })

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
            val songTitleLowercase = song.title!!.lowercaseWithLocale()
            val songArtistLowercase = song.artist!!.lowercaseWithLocale()
            val queryLowercase = query.lowercaseWithLocale()


            if (songTitleLowercase.contains(queryLowercase) || songArtistLowercase.lowercaseWithLocale()
                    .contains(queryLowercase)
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
    var fetch by rememberSaveable { mutableStateOf(false) }
    OutlinedCard(shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                fetch = true
            }) {
        val painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current).data(data = song.imgUri)
                .apply(block = fun ImageRequest.Builder.() {
                    placeholder(0)
                }).build(), imageLoader = LocalContext.current.imageLoader
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
            Column(
                modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.Top
            ) {
                MarqueeText(text = savedSong.title!!, fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                MarqueeText(text = savedSong.artist!!, fontSize = 14.sp)
            }
        }
    }
    if (fetch) {
        var queryStatus by rememberSaveable {
            mutableStateOf(
                QueryStatus.NotSubmitted
            )
        }

        // query and offset for API
        val query = SongInfo(
            songName = song.title ?: "", artistName = song.artist ?: ""
        )
        var offset by rememberSaveable { mutableIntStateOf(0) }

        // queryResult - used to store result of query, failReason - used to store error message if error occurs
        var queryResult by remember { mutableStateOf(SongInfo()) }
        var failReason by rememberSaveable { mutableStateOf("") }

        // lyrics
        var lyricsResult by rememberSaveable { mutableStateOf("") }

        when (queryStatus) {
            QueryStatus.Cancelled -> {
                fetch = false
            }

            QueryStatus.NotSubmitted -> {
                AlertDialog(title = {
                    Text(text = stringResource(R.string.get_lyrics))
                }, text = {
                    Text(
                        text = stringResource(
                            R.string.selected_song_by,
                            song.title ?: "Unknown",
                            song.artist ?: "Unknown"
                        )
                    )
                }, onDismissRequest = {
                    queryStatus = QueryStatus.Cancelled
                }, confirmButton = {
                    Button(onClick = {
                        Thread {
                            queryStatus = QueryStatus.Pending
                            try {
                                queryResult = viewModel.getSongInfo(query, offset)
                                queryStatus = QueryStatus.Success
                            } catch (e: Exception) {
                                when(e){
                                    is UnknownHostException -> {
                                        queryStatus = QueryStatus.NoConnection
                                    }
                                    else -> {
                                        failReason = e.toString()
                                        queryStatus = QueryStatus.Failed
                                    }
                                }
                            }
                        }.start()
                    }) {
                        Text(text = stringResource(R.string.get_song_info))
                    }
                })
            }

            QueryStatus.Pending -> {
                AlertDialog(title = {
                    Text(text = stringResource(R.string.fetching_song_info))
                }, text = {
                    Text(
                        text = stringResource(
                            R.string.fetching_song_info_for_by,
                            song.title ?: "Unknown",
                            song.artist ?: "Unknown"
                        )
                    )
                }, onDismissRequest = {
                    queryStatus = QueryStatus.Cancelled
                }, confirmButton = {
                    // no button but compose cries when i don't use confirmButton
                })
            }

            QueryStatus.Success -> {
                val songNameResult by rememberSaveable { mutableStateOf(queryResult.songName) }
                val artistNameResult by rememberSaveable { mutableStateOf(queryResult.artistName) }
                AlertDialog(title = {
                    Text(text = stringResource(R.string.song_info_fetched))
                }, text = {
                    Column {
                        Text(
                            text = stringResource(
                                R.string.song_name, songNameResult ?: "Unknown"
                            )
                        )
                        Text(
                            text = stringResource(
                                R.string.artist_name, artistNameResult ?: "Unknown"
                            )
                        )
                        Text(text = stringResource(R.string.is_that_correct))
                    }
                }, onDismissRequest = {
                    queryStatus = QueryStatus.Cancelled
                }, confirmButton = {
                    Button(onClick = {
                        Thread {
                            queryStatus = QueryStatus.LyricsPending
                            try {
                                lyricsResult =
                                    viewModel.getSyncedLyrics(queryResult.songLink.toString())
                                queryStatus = QueryStatus.LyricsSuccess
                            } catch (e: Exception) {
                                if (e is FileNotFoundException) lyricsResult = "No lyrics found"
                                else failReason = e.toString()
                                queryStatus = QueryStatus.LyricsFailed
                            }
                        }.start()
                    }) {
                        Text(text = stringResource(id = R.string.get_lyrics))
                    }
                }, dismissButton = {
                    OutlinedButton(onClick = {
                        offset += 1
                        Thread {
                            queryStatus = QueryStatus.Pending
                            try {
                                queryResult = viewModel.getSongInfo(query, offset)
                                queryStatus = QueryStatus.Success
                            } catch (e: Exception) {
                                failReason = e.toString()
                                queryStatus = QueryStatus.Failed
                            }
                        }.start()
                    }) {
                        Text(text = stringResource(R.string.try_again))
                    }
                })
            }

            QueryStatus.LyricsPending -> {
                AlertDialog(title = {
                    Text(text = stringResource(R.string.fetching_lyrics))
                }, text = {
                    Text(
                        text = stringResource(
                            R.string.fetching_lyrics_for_by,
                            queryResult.songName ?: "Unknown",
                            queryResult.artistName ?: "Unknown"
                        )
                    )
                }, onDismissRequest = {
                    queryStatus = QueryStatus.Cancelled
                }, confirmButton = {
                    // no button but compose cries when i don't use confirmButton
                })
            }

            QueryStatus.LyricsSuccess -> {
                AlertDialog(title = {
                    Text(text = stringResource(R.string.lyrics_fetched))
                }, text = {
                    Column {
                        Text(text = stringResource(R.string.first_line_of_lyrics))
                        Text(text = lyricsResult.split("\n")[0])
                        Text(text = stringResource(id = R.string.is_that_correct))
                    }
                }, onDismissRequest = {
                    queryStatus = QueryStatus.Cancelled
                }, confirmButton = {
                    Button(onClick = {
                        val lrc =
                            "[ti:${queryResult.songName}]\n" + "[ar:${queryResult.artistName}]\n" + "[by:Generated using SongSync]\n" + lyricsResult
                        val file = File(
                            song.filePath?.dropLast(4) + ".lrc"
                        )
                        file.writeText(lrc)

                        Toast.makeText(
                            context, "Lyrics saved to ${file.path}", Toast.LENGTH_LONG
                        ).show()

                        queryStatus = QueryStatus.Cancelled
                    }) {
                        Text(text = stringResource(R.string.save_lyrics))
                    }
                }, dismissButton = {
                    OutlinedButton(onClick = {
                        queryStatus = QueryStatus.Cancelled
                    }) {
                        Text(text = stringResource(R.string.cancel))
                    }
                })
            }

            QueryStatus.LyricsFailed -> {
                AlertDialog(title = {
                    Text(text = stringResource(R.string.error))
                }, text = {
                    Text(text = stringResource(R.string.this_track_has_no_lyrics))
                }, onDismissRequest = {
                    queryStatus = QueryStatus.Cancelled
                }, confirmButton = {
                    Button(onClick = { queryStatus = QueryStatus.Cancelled }) {
                        Text(text = stringResource(R.string.ok))
                    }
                })
            }

            QueryStatus.Failed -> {
                AlertDialog(onDismissRequest = { queryStatus = QueryStatus.Cancelled },
                    confirmButton = {
                        Button(onClick = { queryStatus = QueryStatus.Cancelled }) {
                            Text(text = stringResource(id = R.string.ok))
                        }
                    },
                    title = { Text(text = stringResource(id = R.string.error)) },
                    text = {
                        if (failReason.contains("NotFound") || failReason.contains("JSON")) Text(
                            text = "No results"
                        )
                        else Text(text = stringResource(R.string.an_error_occurred, failReason))
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
    var uiState by rememberSaveable { mutableStateOf(UiState.Warning) }

    var failedCount by rememberSaveable { mutableIntStateOf(0) }
    var successCount by rememberSaveable { mutableIntStateOf(0) }
    val total = songs.size

    when (uiState) {
        UiState.Cancelled -> {
            onDone()
        }

        UiState.Warning -> {
            AlertDialog(title = {
                Text(text = stringResource(id = R.string.batch_download_lyrics))
            }, text = {
                Column {
                    Text(text = stringResource(R.string.this_will_download_lyrics_for_all_songs))
                    Text(text = stringResource(R.string.existing_lyrics_for_songs_overwrite))
                    Text(text = stringResource(R.string.less_accurate))
                    Text(text = stringResource(R.string.sure_to_continue))
                }
            }, onDismissRequest = {
                uiState = UiState.Cancelled
            }, confirmButton = {
                Button(onClick = { uiState = UiState.Pending }) {
                    Text(text = stringResource(R.string.yes))
                }
            }, dismissButton = {
                OutlinedButton(onClick = { uiState = UiState.Cancelled }) {
                    Text(text = stringResource(R.string.no))
                }
            })
        }

        UiState.Pending -> {
            AlertDialog(title = {
                Text(text = stringResource(id = R.string.batch_download_lyrics))
            }, text = {
                Column {
                    Text(text = stringResource(R.string.downloading_lyrics))
                    MarqueeText(
                        stringResource(
                            R.string.song,
                            songs[(successCount + failedCount) % total].title ?: "Unknown",
                        )
                    ) // marquee cuz long
                    Text(
                        text = stringResource(
                            R.string.progress,
                            successCount + failedCount,
                            total,
                            ((successCount + failedCount) / total.toFloat() * 100).roundToInt()
                        )
                    )
                    Text(
                        text = stringResource(
                            R.string.success_failed, successCount, failedCount
                        )
                    )
                    Text(text = stringResource(R.string.please_do_not_close_the_app_this_may_take_a_while))
                }
            }, onDismissRequest = {
                uiState = UiState.Cancelled
            }, confirmButton = {
                // no button but compose cries when i don't use confirmButton
            }, dismissButton = {
                OutlinedButton(onClick = { uiState = UiState.Cancelled }) {
                    Text(text = "Cancel")
                }
            })
            val executor = Executors.newSingleThreadExecutor() // blame spotify for single thread
            var notFoundInARow = 0 // detect rate limit
            executor.execute {
                for (song in songs) {
                    if (uiState == UiState.Cancelled) {
                        executor.shutdown()
                        return@execute
                    }

                    if (successCount + failedCount >= total) {
                        uiState = UiState.Done
                        executor.shutdown()
                        return@execute
                    }

                    val query = SongInfo(song.title, song.artist)
                    var queryResult: SongInfo? = null
                    try {
                        queryResult = viewModel.getSongInfo(query)
                    } catch (e: Exception) {
                        if (e is FileNotFoundException) { // no such song OR rate limited
                            notFoundInARow++
                            failedCount++
                            if (notFoundInARow >= 5) {
                                uiState = UiState.RateLimited
                                return@execute
                            }
                            continue
                        }
                        if (e is JSONException) { // no such song
                            failedCount++
                            continue
                        }
                    } finally {
                        notFoundInARow = 0
                    }
                    val lyricsResult: String
                    try {
                        lyricsResult = viewModel.getSyncedLyrics(queryResult?.songLink!!)
                    } catch (e: FileNotFoundException) { // no lyrics
                        failedCount++
                        continue
                    }
                    val lrc =
                        "[ti:${queryResult.songName}]\n" + "[ar:${queryResult.artistName}]\n" + "[by:Generated using SongSync]\n" + lyricsResult
                    val file = File(
                        song.filePath!!.dropLast(4) + ".lrc"
                    )
                    file.writeText(lrc)
                    successCount++
                }
                uiState = UiState.Done
            }
        }

        UiState.Done -> {
            AlertDialog(title = {
                Text(text = stringResource(id = R.string.batch_download_lyrics))
            }, text = {
                Column {
                    Text(text = stringResource(R.string.download_complete))
                    Text(text = stringResource(R.string.success, successCount))
                    Text(text = stringResource(R.string.failed, failedCount))
                }
            }, onDismissRequest = {
                uiState = UiState.Cancelled
            }, confirmButton = {
                Button(onClick = { uiState = UiState.Cancelled }) {
                    Text(text = "OK")
                }
            })
        }

        UiState.RateLimited -> {
            AlertDialog(title = {
                Text(text = stringResource(id = R.string.batch_download_lyrics))
            }, text = {
                Column {
                    Text(text = stringResource(R.string.spotify_api_rate_limit_reached))
                    Text(text = stringResource(R.string.please_try_again_later))
                }
            }, onDismissRequest = {
                uiState = UiState.Cancelled
            }, confirmButton = {
                Button(onClick = { uiState = UiState.Cancelled }) {
                    Text(text = stringResource(id = R.string.ok))
                }
            })
        }

        else -> {
            // nothing
        }
    }
}

// queryStatus: "Not submitted", "Pending", "Success", "Failed" - used to show different UI
enum class QueryStatus {
    NotSubmitted, Pending, Success, Failed, Cancelled, LyricsPending, LyricsSuccess, LyricsFailed, NoConnection
}

enum class UiState {
    Warning, Pending, Done, RateLimited, Cancelled, Loading, Loaded
}