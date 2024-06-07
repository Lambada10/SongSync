@file:Suppress("SpellCheckingInspection")

package pl.lambada.songsync.ui.screens

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.lambada.songsync.R
import pl.lambada.songsync.data.MainViewModel
import pl.lambada.songsync.domain.model.Release
import pl.lambada.songsync.ui.components.AboutItem
import pl.lambada.songsync.ui.components.SwitchItem
import pl.lambada.songsync.util.dataStore
import pl.lambada.songsync.util.ext.getVersion
import pl.lambada.songsync.util.set

/**
 * Composable function for AboutScreen component.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val uriHandler = LocalUriHandler.current
    val version = LocalContext.current.getVersion()
    val context = LocalContext.current

    val dataStore = context.dataStore

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack(
                                navController.graph.startDestinationId,
                                false
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                title = {
                    Text(
                        modifier = Modifier.padding(start = 6.dp),
                        text = stringResource(id = R.string.about)
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = paddingValues
        ) {
            item {
                if (isSystemInDarkTheme()) {
                    AboutItem(label = stringResource(R.string.theme)) {
                        val pureBlack = viewModel.pureBlack
                        var selected by remember { mutableStateOf(pureBlack.value) }
                        SwitchItem(
                            label = stringResource(R.string.pure_black_theme),
                            selected = selected
                        ) {
                            viewModel.pureBlack.value = !selected
                            selected = !selected
                            dataStore.set(key = booleanPreferencesKey("pure_black"), value = selected)
                        }
                    }
                }
            }

            item {
                AboutItem(label = stringResource(R.string.disable_marquee)) {
                    val disableMarquee = viewModel.disableMarquee
                    var selected by remember { mutableStateOf(disableMarquee.value) }
                    SwitchItem(
                        label = stringResource(R.string.disable_marquee_summary),
                        selected = selected
                    ) {
                        viewModel.disableMarquee.value = !selected
                        selected = !selected
                        dataStore.set(key = booleanPreferencesKey("marquee_disable"), value = selected)
                    }
                }
            }

            item {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    var picker by remember { mutableStateOf(false) }
                    val sdCardPath = viewModel.sdCardPath
                    var sdPath by rememberSaveable { mutableStateOf(sdCardPath) }
                    AboutItem(
                        label = stringResource(R.string.sd_card),
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)
                    ) {
                        Text(stringResource(R.string.set_sd_path))
                        if (sdPath == "") {
                            Text(
                                text = stringResource(R.string.no_sd_card_path_set),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.sd_card_path_set_successfully),
                                color = MaterialTheme.colorScheme.tertiary,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row {
                            Spacer(modifier = Modifier.weight(1f))
                            OutlinedButton(
                                onClick = {
                                    sdPath = ""
                                    viewModel.sdCardPath = ""
                                    dataStore.set(
                                        key = stringPreferencesKey("sd_card_path"),
                                        value = sdPath
                                    )
                                }
                            ) {
                                Text(stringResource(R.string.clear_sd_card_path))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(onClick = { picker = true }) {
                                Text(stringResource(R.string.set_sd_card_path))
                            }
                        }

                        if (picker) {
                            val sdCardPicker =
                                rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
                                    if (it == null) {
                                        picker = false
                                        return@rememberLauncherForActivityResult
                                    }
                                    sdPath = it.toString()
                                    viewModel.sdCardPath = it.toString()
                                    dataStore.set(
                                        key = stringPreferencesKey("sd_card_path"),
                                        value = sdPath
                                    )
                                    picker = false
                                }
                            LaunchedEffect(Unit) {
                                sdCardPicker.launch(Uri.parse(Environment.getExternalStorageDirectory().absolutePath))
                            }
                        }
                    }
                }
            }

            item {
                var update by rememberSaveable { mutableStateOf(false) }
                AboutItem(
                    label = stringResource(R.string.about_songsync),
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)
                ) {
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
                AboutItem(
                    stringResource(R.string.source_code),
                    modifier = Modifier
                        .clickable { uriHandler.openUri("https://github.com/Lambada10/SongSync") }
                        .padding(horizontal = 22.dp, vertical = 16.dp)
                ) {
                    Text(stringResource(R.string.we_are_open_source))
                    Text(
                        text = stringResource(id = R.string.view_on_github),
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 12.sp
                    )
                }
            }

            item {
                AboutItem(
                    stringResource(R.string.support),
                ) {
                    Column(
                        modifier = Modifier
                            .clickable { uriHandler.openUri("https://t.me/LambadaOT") }
                            .padding(horizontal = 22.dp, vertical = 16.dp)
                    ) {
                        Text(
                            stringResource(R.string.bugs_or_suggestions_contact_us),
                        )
                        Text(
                            text = stringResource(R.string.telegram_group),
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        stringResource(R.string.create_issue),
                        modifier = Modifier.padding(horizontal = 22.dp)
                    )
                }
            }

            item {
                AboutItem(stringResource(R.string.contributors)) {
                    Contributor.entries.forEach {
                        val additionalInfo = stringResource(id = it.contributionLevel.stringResource)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { it.github?.let { it1 -> uriHandler.openUri(it1) } }
                                .padding(horizontal = 22.dp, vertical = 16.dp)
                        ) {
                            Text(text = it.devName)
                            Text(
                                text = additionalInfo,
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            item {
                AboutItem(label = stringResource(id = R.string.thanks_to)) {
                    val credits = mapOf(
                        stringResource(R.string.spotify_api) to "https://developer.spotify.com/documentation/web-api",
                        stringResource(R.string.spotifylyrics_api) to "https://github.com/akashrchandran/spotify-lyrics-api",
                        stringResource(R.string.syncedlyrics_py) to "https://github.com/0x7d4/syncedlyrics",
                        stringResource(R.string.statusbar_lyrics_ext) to "https://github.com/cjybyjk/StatusBarLyricExt"
                    )
                    credits.forEach { credit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { uriHandler.openUri(credit.value) }
                                .padding(22.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = stringResource(id = R.string.open_website)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = credit.key)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
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
    var latest: Release? by rememberSaveable { mutableStateOf(null) }
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
                        Text("v$version -> ${latest?.tagName}")
                        Text(stringResource(R.string.changelog, latest?.changelog!!))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDownload(latest?.htmlURL!!)
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
enum class Contributor(
    val devName: String, val contributionLevel: ContributionLevel,
    val github: String? = null, val telegram: String? = null
) {
    LAMBADA10(
        "Lambada10", ContributionLevel.LEAD_DEVELOPER,
        github = "https://github.com/Lambada10", telegram = "https://t.me/Lambada10"
    ),
    NIFT4(
        "Nick", ContributionLevel.DEVELOPER,
        github = "https://github.com/nift4", telegram = "https://t.me/nift4"
    ),
    BOBBYESP(
        "BobbyESP", ContributionLevel.CONTRIBUTOR,
        github = "https://github.com/BobbyESP"
    ),
    AKANETAN(
        "AkaneTan", ContributionLevel.CONTRIBUTOR,
        github = "https://github.com/AkaneTan"
    )
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

/**
 * Defines possible provider choices
 */
enum class Providers(val displayName: String) {
    SPOTIFY("Spotify (via SpotifyLyricsAPI)"),
    LRCLIB("LRCLib"),
    NETEASE("Netease")
}