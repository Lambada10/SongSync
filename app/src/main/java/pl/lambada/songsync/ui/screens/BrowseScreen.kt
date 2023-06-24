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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import pl.lambada.songsync.R
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.data.SongInfo
import pl.lambada.songsync.data.SongInfoSaver
import pl.lambada.songsync.ui.common.CommonTextField
import pl.lambada.songsync.ui.components.SongCard
import java.io.File
import java.io.FileNotFoundException

@Composable
fun BrowseScreen(viewModel: MainViewModel) {
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

            // queryStatus: "Not submitted", "Pending", "Success", "Failed" - used to show different UI
            var queryStatus by rememberSaveable { mutableStateOf(QueryStatus.NotSubmitted) }

            // querySong, queryArtist - used to store user input, offset - for search again
            var querySong by rememberSaveable { mutableStateOf("") }
            var queryArtist by rememberSaveable { mutableStateOf("") }
            var offset by rememberSaveable { mutableIntStateOf(0) }

            // queryResult - used to store result of query, failReason - used to store error message if error occurs
            var queryResult by rememberSaveable(stateSaver = SongInfoSaver) {
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
                        label = "Song name",
                        imeAction = ImeAction.Next
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CommonTextField(
                        value = queryArtist,
                        onValueChange = { queryArtist = it.toString() },
                        label = "Artist name"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        val query = SongInfo(
                            songName = querySong, artistName = queryArtist
                        )
                        Thread {
                            queryStatus = QueryStatus.Pending
                            try {
                                queryResult = viewModel.getSongInfo(query, offset)
                                queryStatus = QueryStatus.Success
                            } catch (e: Exception) {
                                queryStatus = QueryStatus.Failed
                                failReason = e.toString()
                            }
                        }.start()
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
                                Thread {
                                    queryStatus = QueryStatus.Pending
                                    try {
                                        queryResult = viewModel.getSongInfo(query, offset)
                                        queryStatus = QueryStatus.Success
                                    } catch (e: Exception) {
                                        queryStatus = QueryStatus.Failed
                                        failReason = e.toString()
                                    }
                                }.start()
                            }) {
                            Text(text = stringResource(id = R.string.try_again))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = { uriHandler.openUri(queryResult.songLink.toString()) }) {
                            Text(text = stringResource(R.string.listen_on_spotify))
                        }
                    }

                    // lyrics
                    var lyricsResult by rememberSaveable { mutableStateOf("") }
                    var lyricSuccess by rememberSaveable { mutableStateOf("Not submitted") }
                    Thread {
                        try {
                            if (queryResult.songLink == null) throw Exception("Song link is empty")
                            lyricsResult =
                                viewModel.getSyncedLyrics(queryResult.songLink!!)
                            lyricSuccess = "Success"
                        } catch (e: Exception) {
                            lyricsResult = e.toString()
                            lyricSuccess = "Failed"
                            if (e is FileNotFoundException) {
                                lyricsResult = "Lyrics not found"
                            }
                        }
                    }.start()

                    when (lyricSuccess) {
                        "Not submitted" -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            CircularProgressIndicator()
                        }

                        "Success" -> {
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
                                    "[ti:${queryResult.songName}]\n" + "[ar:${queryResult.artistName}]\n" + "[by:Generated using SongSync]\n" + lyricsResult
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

                        "Failed" -> {
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

                else -> {
                    //Nothing
                }
            }
        }
    }
}
