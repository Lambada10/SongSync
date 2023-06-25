package pl.lambada.songsync.ui.screens

import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.lambada.songsync.R
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.data.SongInfo
import pl.lambada.songsync.data.SongInfoSaver
import pl.lambada.songsync.ui.components.CommonTextField
import pl.lambada.songsync.ui.components.SongCard
import java.io.File
import java.io.FileNotFoundException
import java.net.UnknownHostException

@Composable
fun BrowseScreen(viewModel: MainViewModel) {

    val scope= rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 8.dp, end = 8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // won't rewrite everything to be different item, it works same way
        item {
            // uriHandler - used to open links
            val uriHandler = LocalUriHandler.current

            // context - for toast
            val context = LocalContext.current

            val generatedUsingString = stringResource(id = R.string.generated_using)

            // queryStatus: "Not submitted", "Pending", "Success", "Failed" - used to show different UI
            var queryStatus by rememberSaveable { mutableStateOf(QueryStatus.NotSubmitted) }

            // querySong, queryArtist - used to store user input, offset - for search again
            var querySong by rememberSaveable { mutableStateOf("") }
            var queryArtist by rememberSaveable { mutableStateOf("") }
            var offset by rememberSaveable { mutableIntStateOf(0) }

            // queryResult - used to store result of query, failReason - used to store error message if error occurs
            var queryResult by rememberSaveable(stateSaver = SongInfoSaver, key = "queryResult") {
                mutableStateOf(
                    SongInfo()
                )
            }
            var failReason by rememberSaveable { mutableStateOf("") }

            when (queryStatus) {
                QueryStatus.NotSubmitted -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    CommonTextField(
                        value = querySong,
                        onValueChange = { querySong = it.toString() },
                        label = stringResource(id = R.string.song_name_no_args),
                        imeAction = ImeAction.Next
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CommonTextField(
                        value = queryArtist,
                        onValueChange = { queryArtist = it.toString() },
                        label = stringResource(R.string.artist_name_no_args)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        val query = SongInfo(
                            songName = querySong, artistName = queryArtist
                        )
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
                                        queryStatus = QueryStatus.Failed
                                        failReason = e.toString()
                                    }
                                }
                            }
                        }
                    }) {
                        Text(text = stringResource(id = R.string.get_lyrics))
                    }
                }

                QueryStatus.Pending -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }

                QueryStatus.Success -> {
                    val songResult by rememberSaveable { mutableStateOf(queryResult.songName.toString()) }
                    val artistResult by rememberSaveable { mutableStateOf(queryResult.artistName.toString()) }
                    val albumArtResult by rememberSaveable { mutableStateOf(queryResult.albumCoverLink.toString()) }
                    Spacer(modifier = Modifier.height(16.dp))
                    SongCard(
                        songName = songResult, artists = artistResult, coverUrl = albumArtResult
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                offset += 1
                                val query = SongInfo(
                                    songName = querySong, artistName = queryArtist
                                )
                                scope.launch(Dispatchers.IO) {
                                    queryStatus = QueryStatus.Pending
                                    try {
                                        queryResult = viewModel.getSongInfo(query, offset)
                                        queryStatus = QueryStatus.Success
                                    } catch (e: Exception) {
                                        queryStatus = QueryStatus.Failed
                                        failReason = e.toString()
                                    }
                                }
                            }) {
                            Text(text = stringResource(id = R.string.try_again))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = { uriHandler.openUri(queryResult.songLink.toString()) }) {
                            Text(text = stringResource(R.string.listen_on_spotify))
                        }
                    }

                    //variable that saves lyrics result
                    var lyricsResult by rememberSaveable(key = "lyricsResultKey") { mutableStateOf("") }
                    var lyricSuccess by rememberSaveable { mutableStateOf(LyricsStatus.NotSubmitted) }

                    LaunchedEffect(true) {
                        launch(Dispatchers.IO) {
                            try {
                                if (queryResult.songLink == null) throw Exception("Song link is empty")
                                if (lyricsResult == "") lyricsResult =
                                    viewModel.getSyncedLyrics(queryResult.songLink!!)
                                lyricSuccess = LyricsStatus.Success
                            } catch (e: Exception) {
                                lyricsResult = e.toString()
                                lyricSuccess = LyricsStatus.Failed
                                if (e is FileNotFoundException) {
                                    lyricsResult = context.getString(R.string.lyrics_not_found)
                                }
                            }
                        }
                    }

                    when (lyricSuccess) {
                        LyricsStatus.NotSubmitted -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            CircularProgressIndicator()
                        }

                        LyricsStatus.Success -> {
                            lyricsResult.dropLast(1) // drop last \n
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedCard(
                                modifier = Modifier.padding(8.dp), shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = lyricsResult, modifier = Modifier.padding(8.dp)
                                )
                            }

                            Button(onClick = {
                                val lrc =
                                    "[ti:${queryResult.songName}]\n" + "[ar:${queryResult.artistName}]\n" + "[by:$generatedUsingString]\n" + lyricsResult
                                val file = File(
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                    "${queryResult.songName} - ${queryResult.artistName}.lrc"
                                )
                                file.writeText(lrc)

                                Toast.makeText(
                                    context,
                                    "File saved to ${file.absolutePath}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }) {
                                Text(text = stringResource(R.string.save_lrc_file))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        LyricsStatus.Failed -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = lyricsResult)
                        }
                    }

                }

                QueryStatus.Failed -> {
                    AlertDialog(onDismissRequest = { queryStatus = QueryStatus.NotSubmitted },
                        confirmButton = {
                            Button(onClick = { queryStatus = QueryStatus.NotSubmitted }) {
                                Text(text = stringResource(id = R.string.ok))
                            }
                        },
                        title = { Text(text = stringResource(id = R.string.error)) },
                        text = {
                            if (failReason.contains("NotFound") || failReason.contains("JSON")) {
                                Text(
                                    text = stringResource(R.string.no_results)
                                )
                            } else {
                                Text(text = stringResource(R.string.error, failReason))
                            }
                        })
                }

                QueryStatus.NoConnection -> {
                    AlertDialog(
                        onDismissRequest = { queryStatus = QueryStatus.NotSubmitted },
                        confirmButton = {
                            Button(onClick = { queryStatus = QueryStatus.NotSubmitted }) {
                                Text(text = stringResource(id = R.string.ok))
                            }
                        },
                        title = { Text(text = stringResource(id = R.string.error)) },
                        text = {
                            Text(text = stringResource(id = R.string.no_internet_server))
                        }
                    )
                }

                else -> {
                    //Nothing
                }
            }
        }
    }
}

enum class LyricsStatus {
    NotSubmitted, Success, Failed
}