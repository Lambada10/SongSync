package pl.lambada.songsync.ui.screens

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
import pl.lambada.songsync.data.ext.getVersion
import pl.lambada.songsync.ui.components.AboutCard

/**
 * Composable function for AboutScreen component.
 */
@Composable
fun AboutScreen() {
    val uriHandler = LocalUriHandler.current
    val version = LocalContext.current.getVersion()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        item {
            AboutCard(stringResource(R.string.about_songsync)) {
                Text(stringResource(R.string.what_is_songsync))
                Text(stringResource(R.string.extra_what_is_songsync))
                Text(stringResource(R.string.app_version, version))
            }
        }
        item {
            AboutCard(stringResource(R.string.spotify_api)) {
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
        item {
            AboutCard(stringResource(R.string.spotifylyrics_api)) {
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
        item {
            AboutCard(stringResource(R.string.source_code)) {
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
        item {
            AboutCard(stringResource(R.string.contributors)) {
                Contributor.values().forEach {
                    val additionalInfo = stringResource(id = it.contributionLevel.stringResource)
                    Text(text = "${it.devName} ($additionalInfo)")
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        if (it.github != null) {
                            Button(
                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                                onClick = {
                                    uriHandler.openUri(it.github)
                                }
                            ) {
                                Text(stringResource(R.string.github))
                            }
                        }
                        if (it.telegram != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                                onClick = {
                                    uriHandler.openUri(it.telegram)
                                }
                            ) {
                                Text(stringResource(R.string.telegram))
                            }
                        }
                    }
                }
            }
        }
        item {
            AboutCard(stringResource(R.string.support)) {
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

enum class Contributor(val devName: String, val contributionLevel: ContributionLevel,
                       val github: String? = null, val telegram: String? = null) {
    LAMBADA10("Lambada10", ContributionLevel.LEAD_DEVELOPER,
        github = "https://github.com/Lambada10", telegram = "https://t.me/Lambada10"),
    BOBBYESP("BobbyESP", ContributionLevel.CONTRIBUTOR,
        github = "https://github.com/BobbyESP"),
    AKANETAN("AkaneTan", ContributionLevel.CONTRIBUTOR,
        github = "https://github.com/AkaneTan"),
    NIFT4("Nick", ContributionLevel.CONTRIBUTOR,
        github = "https://github.com/nift4")
}

/**
 * Defines the contribution level of a contributor.
 */
enum class ContributionLevel(val stringResource: Int) {
    CONTRIBUTOR(R.string.contributor),
    DEVELOPER(R.string.developer),
    LEAD_DEVELOPER(R.string.lead_developer)
}