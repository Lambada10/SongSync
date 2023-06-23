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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import coil.imageLoader
import org.json.JSONException
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.data.Song
import pl.lambada.songsync.data.SongInfo
import pl.lambada.songsync.ui.common.MarqueeText
import java.io.File
import java.io.FileNotFoundException
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.roundToInt


@Composable
fun HomeScreen(viewModel: MainViewModel) {
    var uiState by rememberSaveable { mutableStateOf("Loading") }
    val context = LocalContext.current
    var songs: List<Song> = viewModel.getAllSongs(context)
    when(uiState) {
        "Loading" ->
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Thread {
                    songs = viewModel.getAllSongs(context)
                    uiState = "Loaded"
                }.start()
            }
        "Loaded" ->
            HomeScreenLoaded(viewModel = viewModel, songs = songs)
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
            Column (
                modifier = Modifier
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
            ) {
                Row {
                    Button(onClick = { isBatchDownload = true }) {
                        Text(text = "Batch download lyrics")
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

                if (isBatchDownload)
                    BatchDownloadLyrics(
                        songs = songs,
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp),
                        content = {}
                    )
                }
            }
        }
        
        songs.forEach { song ->
            item {
                if(song.title!!.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault()))
                    || song.artist!!.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault()))) {
                    SongItem(song = song, viewModel = viewModel)
                }
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
    OutlinedCard(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                fetch = true
            }
    ) {
        val painter = rememberImagePainter(
            data = song.imgUri,
            imageLoader = LocalContext.current.imageLoader,
            builder = {
                placeholder(0)
            }
        )
        // I hate this
        val songName by rememberSaveable {
            mutableStateOf(
                song.title ?: ""
            )
        }
        val artistName by rememberSaveable {
            mutableStateOf(
                song.artist ?: ""
            )
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
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.Top
            ) {
                MarqueeText(text = songName, fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                MarqueeText(text = artistName, fontSize = 14.sp)
            }
        }
    }
    if (fetch) {
        // queryStatus: "Not submitted", "Pending", "Success", "Failed" - used to show different UI
        var queryStatus by rememberSaveable { mutableStateOf("Not submitted") }

        // query and offset for API
        val query = SongInfo(
            songName = song.title ?: "",
            artistName = song.artist ?: ""
        )
        var offset by rememberSaveable { mutableIntStateOf(0) }

        // queryResult - used to store result of query, failReason - used to store error message if error occurs
        var queryResult by remember { mutableStateOf(SongInfo()) }
        var failReason by rememberSaveable { mutableStateOf("") }

        // lyrics
        var lyricsResult by rememberSaveable { mutableStateOf("") }

        when (queryStatus) {
            "Cancelled" -> {
                fetch = false
            }
            "Not submitted" -> {
                AlertDialog(
                    title = {
                        Text(text = "Get lyrics")
                    },
                    text = {
                        Text(text = "Selected song: ${song.title} by ${song.artist}")
                    },
                    onDismissRequest = {
                        queryStatus = "Cancelled"
                    },
                    confirmButton = {
                        Button(onClick = {
                            Thread {
                                queryStatus = "Pending"
                                try {
                                    queryResult = viewModel.getSongInfo(query, offset)
                                    queryStatus = "Success"
                                } catch (e: Exception) {
                                    failReason = e.toString()
                                    queryStatus = "Failed"
                                }
                            }.start()
                        }
                        ) {
                            Text(text = "Get Song Info")
                        }
                    }
                )
            }
            "Pending" -> {
                AlertDialog(
                    title = {
                        Text(text = "Fetching song info")
                    },
                    text = {
                        Text(text = "Fetching song info for ${song.title} by ${song.artist}")
                    },
                    onDismissRequest = {
                        queryStatus = "Cancelled"
                    },
                    confirmButton = {
                        // no button but compose cries when i don't use confirmButton
                    }
                )
            }
            "Success" -> {
                val songNameResult by rememberSaveable { mutableStateOf(queryResult.songName) }
                val artistNameResult by rememberSaveable { mutableStateOf(queryResult.artistName) }
                AlertDialog(
                    title = {
                        Text(text = "Song info fetched")
                    },
                    text = {
                        Column {
                            Text(text = "Song name: ${songNameResult}")
                            Text(text = "Artist name: ${artistNameResult}")
                            Text(text = "Is that correct?")
                        }
                    },
                    onDismissRequest = {
                        queryStatus = "Cancelled"
                    },
                    confirmButton = {
                        Button(onClick = {
                            Thread {
                                queryStatus = "LyricsPending"
                                try {
                                    lyricsResult = viewModel.getSyncedLyrics(queryResult.songLink.toString())
                                    queryStatus = "LyricsSuccess"
                                } catch (e: Exception) {
                                    if (e is FileNotFoundException)
                                        lyricsResult = "No lyrics found"
                                    else
                                        failReason = e.toString()
                                    queryStatus = "LyricsFailed"
                                }
                            }.start()
                        }
                        ) {
                            Text(text = "Get lyrics")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = {
                            offset += 1
                            Thread {
                                queryStatus = "Pending"
                                try {
                                    queryResult = viewModel.getSongInfo(query, offset)
                                    queryStatus = "Success"
                                } catch (e: Exception) {
                                    failReason = e.toString()
                                    queryStatus = "Failed"
                                }
                            }.start()
                        }
                        ) {
                            Text(text = "Try again")
                        }
                    }
                )
            }
            "LyricsPending" -> {
                AlertDialog(
                    title = {
                        Text(text = "Fetching lyrics")
                    },
                    text = {
                        Text(text = "Fetching lyrics for ${queryResult.songName} by ${queryResult.artistName}")
                    },
                    onDismissRequest = {
                        queryStatus = "Cancelled"
                    },
                    confirmButton = {
                        // no button but compose cries when i don't use confirmButton
                    }
                )
            }
            "LyricsSuccess" -> {
                AlertDialog(
                    title = {
                        Text(text = "Lyrics fetched")
                    },
                    text = {
                        Column {
                            Text(text = "First line of lyrics: ")
                            Text(text = lyricsResult.split("\n")[0])
                            Text(text = "Is that correct?")
                        }
                    },
                    onDismissRequest = {
                        queryStatus = "Cancelled"
                    },
                    confirmButton = {
                        Button(onClick = {
                            val lrc =
                                "[ti:${queryResult.songName}]\n" +
                                "[ar:${queryResult.artistName}]\n" +
                                "[by:Generated using SongSync]\n" +
                                lyricsResult
                            val file = File(
                                song.filePath?.dropLast(4) + ".lrc"
                            )
                            file.writeText(lrc)

                            Toast.makeText(
                                context,
                                "Lyrics saved to ${file.path}",
                                Toast.LENGTH_LONG
                            ).show()

                            queryStatus = "Cancelled"
                        }
                        ) {
                            Text(text = "Save lyrics")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = {
                            queryStatus = "Cancelled"
                        }
                        ) {
                            Text(text = "Cancel")
                        }
                    }
                )
            }
            "LyricsFailed" -> {
                AlertDialog(
                    title = {
                        Text(text = "Error")
                    },
                    text = {
                        Text(text = "This track has no lyrics")
                    },
                    onDismissRequest = {
                        queryStatus = "Cancelled"
                    },
                    confirmButton = {
                        Button(onClick = { queryStatus = "Cancelled" }) {
                            Text(text = "OK")
                        }
                    }
                )
            }
            "Failed" -> {
                var showSpotifyResponse by rememberSaveable { mutableStateOf(false) }
                AlertDialog(
                    onDismissRequest = { queryStatus = "Cancelled" },
                    confirmButton = {
                        Button(onClick = { queryStatus = "Cancelled" }) {
                            Text(text = "OK")
                        }
                    },
                    dismissButton = {
                        if(!failReason.contains("FileNotFoundException")) {
                            if (showSpotifyResponse)
                                OutlinedButton(onClick = { showSpotifyResponse = false }) {
                                    Text(text = "Hide response")
                                }
                            else
                                OutlinedButton(onClick = { showSpotifyResponse = true }) {
                                    Text(text = "Show response")
                                }
                        }
                    },
                    title = { Text(text = "Error") },
                    text = {
                        val response by rememberSaveable { mutableStateOf(viewModel.spotifyResponse) }
                        if(!showSpotifyResponse)
                            Text(text = failReason)
                        else
                            Text(text = response)
                    }
                )
            }
            else -> fetch = false
        }
    }
}

@Composable
fun BatchDownloadLyrics(songs: List<Song>, viewModel: MainViewModel, onDone: () -> Unit) {
    var uiState by rememberSaveable { mutableStateOf("Warning") }

    var failedCount by rememberSaveable { mutableIntStateOf(0) }
    var successCount by rememberSaveable { mutableIntStateOf(0) }
    val total = songs.size

    when (uiState) {
        "Cancelled" -> {
            onDone()
        }
        "Warning" -> {
            AlertDialog(
                title = {
                    Text(text = "Batch download lyrics")
                },
                text = {
                    Column {
                        Text(text = "This will download lyrics for all songs")
                        Text(text = "Existing lyrics for songs will be overwritten with new ones")
                        Text(text = "This may be less accurate than downloading lyrics one by one")
                        Text(text = "Are you sure you want to continue?")
                    }
                },
                onDismissRequest = {
                    uiState = "Cancelled"
                },
                confirmButton = {
                    Button(
                        onClick = { uiState = "Pending" }
                    ) {
                        Text(text = "Yes")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { uiState = "Cancelled" }
                    ) {
                        Text(text = "No")
                    }
                }
            )
        }
        "Pending" -> {
            AlertDialog(
                title = {
                    Text(text = "Batch download lyrics")
                },
                text = {
                    Column {
                        Text(text = "Downloading lyrics")
                        MarqueeText("Song: ${songs[(successCount + failedCount) % total].title}") // marquee cuz long
                        Text(text = "Progress: ${successCount + failedCount}/$total " +
                            "(${((successCount + failedCount) / total.toFloat() * 100).roundToInt()}%)")
                        Text(text = "Success: $successCount, Failed: $failedCount")
                        Text(text = "Please do not close the app, this may take a while")
                    }
                },
                onDismissRequest = {
                    uiState = "Cancelled"
                },
                confirmButton = {
                    // no button but compose cries when i don't use confirmButton
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { uiState = "Cancelled" }
                    ) {
                        Text(text = "Cancel")
                    }
                }
            )
            val executor = Executors.newSingleThreadExecutor() // blame spotify for single thread
            var notFoundInARow = 0 // detect rate limit
            executor.execute {
                for (song in songs) {
                    if (uiState == "Cancelled") {
                        executor.shutdown()
                        return@execute
                    }

                    if (successCount + failedCount >= total) {
                        uiState = "Done"
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
                                uiState = "RateLimited"
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
                        "[ti:${queryResult.songName}]\n" +
                        "[ar:${queryResult.artistName}]\n" +
                        "[by:Generated using SongSync]\n" +
                        lyricsResult
                    val file = File(
                        song.filePath!!.dropLast(4) + ".lrc"
                    )
                    file.writeText(lrc)
                    successCount++
                }
                uiState = "Done"
            }
        }
        "Done" -> {
            AlertDialog(
                title = {
                    Text(text = "Batch download lyrics")
                },
                text = {
                    Column {
                        Text(text = "Download complete")
                        Text(text = "Success: $successCount")
                        Text(text = "Failed: $failedCount")
                    }
                },
                onDismissRequest = {
                    uiState = "Cancelled"
                },
                confirmButton = {
                    Button(
                        onClick = { uiState = "Cancelled" }
                    ) {
                        Text(text = "OK")
                    }
                }
            )
        }
        "RateLimited" -> {
            AlertDialog(
                title = {
                    Text(text = "Batch download lyrics")
                },
                text = {
                    Column {
                        Text(text = "Spotify API rate limit reached")
                        Text(text = "Please try again later")
                    }
                },
                onDismissRequest = {
                    uiState = "Cancelled"
                },
                confirmButton = {
                    Button(
                        onClick = { uiState = "Cancelled" }
                    ) {
                        Text(text = "OK")
                    }
                }
            )
        }
    }
}