package pl.lambada.songsync.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.lambada.songsync.R
import pl.lambada.songsync.data.ContributorsArgs
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.ui.components.AboutCard

/**
 * Composable function for AboutScreen component.
 *
 * @param viewModel the [MainViewModel] instance.
 */
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
            AboutCard(stringResource(R.string.about_songsync)) {
                Column(
                    modifier = Modifier.padding(
                        start = 8.dp,
                        top = 0.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    )
                ) {
                    Text(stringResource(R.string.what_is_songsync))
                    Text(stringResource(R.string.extra_what_is_songsync))
                    Text(stringResource(R.string.app_version, version))
                }
            }
        }
        item {
            AboutCard(stringResource(R.string.spotify_api)) {
                Column(
                    modifier = Modifier.padding(
                        start = 8.dp,
                        top = 0.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    )
                ) {
                    Text(stringResource(R.string.what_it_uses))
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            modifier = Modifier.padding(top = 8.dp),
                            onClick = {
                                uriHandler.openUri("https://developer.spotify.com/documentation/web-api")
                            }
                        ) {
                            Text(stringResource(R.string.spotify_for_developers))
                        }
                    }
                }
            }
        }
        item {
            AboutCard(stringResource(R.string.spotifylyrics_api)) {
                Column(
                    modifier = Modifier.padding(
                        start = 8.dp,
                        top = 0.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    )
                ) {
                    Text(stringResource(R.string.how_we_get_lyrics))
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            modifier = Modifier.padding(top = 8.dp),
                            onClick = {
                                uriHandler.openUri("https://github.com/akashrchandran/spotify-lyrics-api")
                            }
                        ) {
                            Text(stringResource(R.string.view_on_github))
                        }
                    }
                }
            }
        }
        item {
            AboutCard(stringResource(R.string.source_code)) {
                Column(
                    modifier = Modifier.padding(
                        start = 8.dp,
                        top = 0.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    )
                ) {
                    Text(stringResource(R.string.we_are_open_source))
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                            onClick = {
                                uriHandler.openUri("https://github.com/Lambada10/SongSync")
                            }
                        ) {
                            Text(stringResource(id = R.string.view_on_github))
                        }
                    }
                }
            }
        }
        item {
            AboutCard(stringResource(R.string.contributors)) {
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
                            additionalInfo = it.getValue(ContributorsArgs.ADDITIONAL_INFO)
                        } catch (e: NoSuchElementException) { // no additional info

                        }
                        Text(
                            text = it.getValue(ContributorsArgs.NAME) + (if (additionalInfo != "") " ($additionalInfo)" else "")
                        )
                        Row {
                            Spacer(modifier = Modifier.weight(1f))
                            var github = ""
                            try {
                                github = it.getValue(ContributorsArgs.GITHUB)
                            } catch (e: NoSuchElementException) {
                                // no github link
                            }
                            if (github != "") Button(
                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                                onClick = {
                                    uriHandler.openUri(github)
                                }
                            ) {
                                Text(stringResource(R.string.github))
                            }
                            var telegram = ""
                            try {
                                telegram = it.getValue(ContributorsArgs.TELEGRAM)
                            } catch (e: NoSuchElementException) {
                                // no telegram link
                            }
                            if (telegram != "") {
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                                    onClick = {
                                        uriHandler.openUri(telegram)
                                    }
                                ) {
                                    Text(stringResource(R.string.telegram))
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            AboutCard(stringResource(R.string.support)) {
                Column(
                    modifier = Modifier.padding(
                        start = 8.dp,
                        top = 0.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    )
                ) {
                    Text(stringResource(R.string.bugs_or_suggestions_contact_us))
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                            onClick = {
                                uriHandler.openUri("https://t.me/LambadaOT")
                            }
                        ) {
                            Text(stringResource(R.string.telegram_group))
                        }
                    }
                    Text(stringResource(R.string.create_issue))
                }
            }
        }
    }
}