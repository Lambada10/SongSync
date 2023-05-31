package pl.lambada.songsync.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.MarqueeAnimationMode
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.data.SongInfo
import pl.lambada.songsync.ui.common.CommonTextField
import pl.lambada.songsync.ui.common.MarqueeText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // uriHandler - used to open links
        val uriHandler = LocalUriHandler.current

        // queryStatus: "Not submitted", "Pending", "Success", "Failed" - used to show different UI
        var queryStatus by rememberSaveable { mutableStateOf("Not submitted") }

        // querySong, queryArtist - used to store user input, offset - for search again
        var querySong by rememberSaveable { mutableStateOf("") }
        var queryArtist by rememberSaveable { mutableStateOf("") }
        var offset by rememberSaveable { mutableStateOf(0) }

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
                    val painter = rememberImagePainter(data = albumArtResult)
                    Row(modifier = Modifier.height(72.dp)) {
                        Image(
                            painter = painter,
                            contentDescription = "Album cover",
                            modifier = Modifier
                                .height(72.dp)
                                .aspectRatio(1f),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.Top) {
                            MarqueeText(text = songResult, fontSize = 18.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            MarqueeText(text = artistResult, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            offset += 1
                            Thread {
                                queryStatus = "Pending"
                                try {
                                    queryResult = viewModel.getSongInfo(queryResult, offset)
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
            }
            "Failed" -> {
                AlertDialog(
                    onDismissRequest = { queryStatus = "Not submitted" },
                    confirmButton = {
                        Button(onClick = { queryStatus = "Not submitted" }) {
                            Text(text = "OK")
                        }
                    },
                    title = { Text(text = "Error") },
                    text = { Text(text = failReason) }
                )
            }
        }

    }
}
