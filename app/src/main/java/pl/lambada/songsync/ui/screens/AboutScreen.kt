@file:Suppress("SpellCheckingInspection")

package pl.lambada.songsync.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.lambada.songsync.R
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.data.dto.Release
import pl.lambada.songsync.data.ext.getVersion
import pl.lambada.songsync.ui.components.AboutCard

/**
 * Composable function for AboutScreen component.
 */
@Composable
fun AboutScreen(viewModel: MainViewModel) {
    val uriHandler = LocalUriHandler.current
    val version = LocalContext.current.getVersion()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        item {
            var update by rememberSaveable { mutableStateOf(false) }
            AboutCard(stringResource(R.string.about_songsync)) {
                Text(stringResource(R.string.what_is_songsync))
                Text(stringResource(R.string.extra_what_is_songsync))
                Text("")
                Text(stringResource(R.string.app_version, version))
                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        modifier = Modifier.padding(top = 8.dp),
                        onClick = { update = true }
                    ) {
                        Text(stringResource(R.string.check_for_updates))
                    }
                }
            }

            if (update) {
                CheckForUpdates(
                    onDismiss = { update = false },
                    onDownload = { uriHandler.openUri(it) },
                    context = LocalContext.current,
                    viewModel = viewModel,
                    version = version
                )
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

@Composable
fun CheckForUpdates(
    onDismiss: () -> Unit,
    onDownload: (String) -> Unit,
    context: Context,
    viewModel: MainViewModel,
    version: String
) {
    var updateState by rememberSaveable { mutableStateOf(UpdateState.CHECKING) }
    var latest by rememberSaveable { mutableStateOf(Release()) }
    var isUpdate by rememberSaveable { mutableStateOf(false) }

    when (updateState) {
        UpdateState.CHECKING -> {
            Toast.makeText(
                context,
                stringResource(R.string.checking_for_updates),
                Toast.LENGTH_SHORT
            ).show()

            LaunchedEffect(Unit) {
                launch(Dispatchers.IO) {
                    try {
                        latest = viewModel.getLatestRelease()
                        isUpdate = viewModel.isNewerRelease(context)
                    } catch (e: Exception) {
                        updateState = UpdateState.ERROR
                        return@launch
                    }
                    updateState = if (isUpdate) {
                        UpdateState.UPDATE_AVAILABLE
                    } else {
                        UpdateState.UP_TO_DATE
                    }
                }
            }
        }
        UpdateState.UPDATE_AVAILABLE -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.update_available)) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text("v$version -> ${latest.tagName}")
                        Text(stringResource(R.string.changelog, latest.changelog!!))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDownload(latest.htmlURL!!)
                        }
                    ) {
                        Text(stringResource(R.string.download))
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = onDismiss
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
        UpdateState.UP_TO_DATE -> {
            Toast.makeText(
                context,
                stringResource(R.string.up_to_date),
                Toast.LENGTH_SHORT
            ).show()
            onDismiss()
        }
        UpdateState.ERROR -> {
            Toast.makeText(
                context,
                stringResource(R.string.error_checking_for_updates),
                Toast.LENGTH_SHORT
            ).show()
            onDismiss()
        }
    }
}

@Suppress("SpellCheckingInspection")
enum class Contributor(val devName: String, val contributionLevel: ContributionLevel,
                       val github: String? = null, val telegram: String? = null) {
    LAMBADA10("Lambada10", ContributionLevel.LEAD_DEVELOPER,
        github = "https://github.com/Lambada10", telegram = "https://t.me/Lambada10"),
    NIFT4("Nick", ContributionLevel.DEVELOPER,
        github = "https://github.com/nift4", telegram = "https://t.me/nift4"),
    BOBBYESP("BobbyESP", ContributionLevel.CONTRIBUTOR,
        github = "https://github.com/BobbyESP"),
    AKANETAN("AkaneTan", ContributionLevel.CONTRIBUTOR,
        github = "https://github.com/AkaneTan")
}

/**
 * Defines the contribution level of a contributor.
 */
enum class ContributionLevel(val stringResource: Int) {
    CONTRIBUTOR(R.string.contributor),
    DEVELOPER(R.string.developer),
    LEAD_DEVELOPER(R.string.lead_developer)
}

/**
 * Defines the state of the update check.
 */
enum class UpdateState {
    CHECKING, UP_TO_DATE, UPDATE_AVAILABLE, ERROR
}