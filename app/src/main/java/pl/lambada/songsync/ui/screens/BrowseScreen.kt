package pl.lambada.songsync.ui.screens

import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.data.SongInfo
import pl.lambada.songsync.ui.common.CommonTextField
import pl.lambada.songsync.ui.common.MarqueeText
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
            var queryStatus by rememberSaveable { mutableStateOf("Not submitted") }

            // querySong, queryArtist - used to store user input, offset - for search again
            var querySong by rememberSaveable { mutableStateOf("") }
            var queryArtist by rememberSaveable { mutableStateOf("") }
            var offset by rememberSaveable { mutableIntStateOf(0) }

            // queryResult - used to store result of query, failReason - used to store error message if error occurs
            var queryResult by remember { mutableStateOf(SongInfo()) }
            var failReason by rememberSaveable { mutableStateOf("") }

            when (queryStatus) {
                "Not submitted" -> {
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
                            songName = querySong,
                            artistName = queryArtist
                        )
                        Thread {
                            queryStatus = "Pending"
                            try {
                                queryResult = viewModel.getSongInfo(query, offset)
                                queryStatus = "Success"
                            } catch (e: Exception) {
                                queryStatus = "Failed"
                                failReason = e.toString()
                            }
                        }.start()
                    }) {
                        Text(text = "Get lyrics")
                    }
                }

                "Pending" -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }

                "Success" -> {
                    // fix nulls after rotating device by using rememberSaveable which can't hold complex objects like SongInfo
                    val songResult by rememberSaveable { mutableStateOf(queryResult.songName.toString()) }
                    val artistResult by rememberSaveable { mutableStateOf(queryResult.artistName.toString()) }
                    val albumArtResult by rememberSaveable { mutableStateOf(queryResult.albumCoverLink.toString()) }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedCard(
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        val painter = rememberAsyncImagePainter(model = albumArtResult)
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
                                MarqueeText(text = songResult, fontSize = 18.sp)
                                Spacer(modifier = Modifier.weight(1f))
                                MarqueeText(text = artistResult, fontSize = 14.sp)
                            }
                        }
                    }

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
                                    songName = querySong,
                                    artistName = queryArtist
                                )
                                Thread {
                                    queryStatus = "Pending"
                                    try {
                                        queryResult = viewModel.getSongInfo(query, offset)
                                        queryStatus = "Success"
                                    } catch (e: Exception) {
                                        queryStatus = "Failed"
                                        failReason = e.toString()
                                    }
                                }.start()
                            }
                        ) {
                            Text(text = "Try again")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = { uriHandler.openUri(queryResult.songLink.toString()) }
                        ) {
                            Text(text = "Listen on Spotify")
                        }
                    }

                    // lyrics
                    var lyricsResult by rememberSaveable { mutableStateOf("") }
                    var lyricSuccess by rememberSaveable { mutableStateOf("Not submitted") }
                    Thread {
                        try {
                            lyricsResult = viewModel.getSyncedLyrics(queryResult.songLink.toString())
                            lyricSuccess = "Success"
                        } catch (e: Exception) {
                            lyricsResult = e.toString()
                            lyricSuccess = "Failed"
                            if(e is FileNotFoundException) {
                                lyricsResult = "Lyrics not found"
                            }
                        }
                    }.start()

                    when(lyricSuccess) {
                        "Not submitted" -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            CircularProgressIndicator()
                        }
                        "Success" -> {
                            lyricsResult.dropLast(1) // drop last \n
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedCard(
                                modifier = Modifier.padding(8.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = lyricsResult,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    val lrc =
                                        "[ti:${queryResult.songName}]\n" +
                                        "[ar:${queryResult.artistName}]\n" +
                                        "[by:Generated using SongSync]\n" +
                                        lyricsResult
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
                                }
                            ) {
                                Text(text = "Save .lrc file")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        "Failed" -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = lyricsResult)
                        }
                    }

                }

                "Failed" -> {
                    var showSpotifyResponse by rememberSaveable { mutableStateOf(false) }
                    AlertDialog(
                        onDismissRequest = { queryStatus = "Not submitted" },
                        confirmButton = {
                            Button(onClick = { queryStatus = "Not submitted" }) {
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
            }
        }
    }
}
