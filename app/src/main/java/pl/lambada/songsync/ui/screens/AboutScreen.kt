package pl.lambada.songsync.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import pl.lambada.songsync.data.MainViewModel

@Composable
fun AboutScreen(viewModel: MainViewModel) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        val version = viewModel.getVersion(context)
        item {
            AboutCard("About SongSync") {
                Column(
                    modifier = Modifier.padding(
                        start = 8.dp,
                        top = 0.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    )
                ) {
                    Text("SongSync is an app that lets you to download synced lyrics (.lrc files) for your local music library.")
                    Text("You can also search for specific songs to get their lyrics (and save them to downloads).")
                    Text("\nApp version: $version")
                }
            }
        }
        item {
            AboutCard("Spotify API") {
                Column(
                    modifier = Modifier.padding(
                        start = 8.dp,
                        top = 0.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    )
                ) {
                    Text("SongSync uses Spotify API to get song data. " +
                        "You can find more information about Spotify API on their website.")
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            modifier = Modifier.padding(top = 8.dp),
                            onClick = {
                                uriHandler.openUri("https://developer.spotify.com/documentation/web-api")
                            }
                        ) {
                            Text("Spotify for Developers")
                        }
                    }
                }
            }
        }
        item {
            AboutCard("SpotifyLyrics API") {
                Column(
                    modifier = Modifier.padding(
                        start = 8.dp,
                        top = 0.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    )
                ) {
                    Text("SongSync uses SpotifyLyrics API to get lyrics for songs. " +
                            "You can find more information about SpotifyLyrics API on their GitHub page.")
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            modifier = Modifier.padding(top = 8.dp),
                            onClick = {
                                uriHandler.openUri("https://github.com/akashrchandran/spotify-lyrics-api")
                            }
                        ) {
                            Text("View on GitHub")
                        }
                    }
                }
            }
        }
        item {
            AboutCard("Source code") {
                Column(
                    modifier = Modifier.padding(
                        start = 8.dp,
                        top = 0.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    )
                ) {
                    Text("SongSync is an open-source project. You can find the source code on GitHub.")
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                            onClick = {
                                uriHandler.openUri("https://github.com/Lambada10/SongSync")
                            }
                        ) {
                            Text("View on GitHub")
                        }
                    }
                }
            }
        }
        item {
            AboutCard("Contributors") {
                val contributors = viewModel.getContributorsInfo()
                Column(
                    modifier = Modifier.padding(
                        start = 8.dp,
                        top = 0.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    )
                ) {
                    contributors.forEach {
                        var additionalInfo = ""
                        try {
                            additionalInfo = it.getValue("additionalInfo")
                        } catch (e: NoSuchElementException) { // no additional info

                        }
                        Text(
                            text = it.getValue("name") + (if (additionalInfo != "") " ($additionalInfo)" else "")
                        )
                        Row {
                            Spacer(modifier = Modifier.weight(1f))
                            var github = ""
                            try {
                                github = it.getValue("github")
                            } catch (e: NoSuchElementException) {
                                // no github link
                            }
                            if(github != "")
                            Button(
                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                                onClick = {
                                    uriHandler.openUri(github)
                                }
                            ) {
                                Text("GitHub")
                            }
                            var telegram = ""
                            try {
                                telegram = it.getValue("telegram")
                            } catch (e: NoSuchElementException) {
                                // no telegram link
                            }
                            if(telegram != "") {
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                                    onClick = {
                                        uriHandler.openUri(telegram)
                                    }
                                ) {
                                    Text("Telegram")
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            AboutCard(label = "Support") {
                Column(
                    modifier = Modifier.padding(
                        start = 8.dp,
                        top = 0.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    )
                ) {
                    Text("Found a bug? Have a suggestion? Want to contribute? " +
                        "Feel free to contact me on my Telegram group.")
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                            onClick = {
                                uriHandler.openUri("https://t.me/LambadaOT")
                            }
                        ) {
                            Text("Telegram group")
                        }
                    }
                    Text("You can also create an issue on GitHub (link above).")
                }
            }
        }
    }
}

@Composable
fun AboutCard(label: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(8.dp),
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}