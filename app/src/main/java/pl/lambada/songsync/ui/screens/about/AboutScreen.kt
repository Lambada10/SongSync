@file:Suppress("SpellCheckingInspection")

package pl.lambada.songsync.ui.screens.about

import android.net.Uri
import android.os.Build
import android.os.Environment
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import pl.lambada.songsync.R
import pl.lambada.songsync.ui.components.AboutItem
import pl.lambada.songsync.ui.components.SwitchItem
import pl.lambada.songsync.ui.screens.about.components.Contributor
import pl.lambada.songsync.ui.screens.about.components.UpdateAvailableDialog
import pl.lambada.songsync.ui.screens.about.components.UpdateState
import pl.lambada.songsync.util.ext.getVersion

/**
 * Composable function for AboutScreen component.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    viewModel: AboutViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val version = LocalContext.current.getVersion()

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
                        val selected =  viewModel.userSettingsController.pureBlack

                        SwitchItem(
                            label = stringResource(R.string.pure_black_theme),
                            selected = selected
                        ) {
                            viewModel.userSettingsController.updatePureBlack(!selected)
                        }
                    }
                }
            }

            item {
                AboutItem(label = stringResource(R.string.disable_marquee)) {
                    val selected = viewModel.userSettingsController.disableMarquee

                    SwitchItem(
                        label = stringResource(R.string.disable_marquee_summary),
                        selected = selected
                    ) {
                        viewModel.userSettingsController.updateDisableMarquee(!selected)
                    }
                }
            }

            item {
                AboutItem(label = stringResource(id = R.string.include_translation)) {
                    val selected = viewModel.userSettingsController.includeTranslation

                    SwitchItem(
                        label = stringResource(id = R.string.include_translation_summary),
                        selected = selected
                    ) {
                        viewModel.userSettingsController.updateIncludeTranslation(!selected)
                    }
                }
            }

            item {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    var picker by remember { mutableStateOf(false) }
                    val sdPath = viewModel.userSettingsController.sdCardPath
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
                                    viewModel.userSettingsController.updateSdCardPath("")
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
                                    viewModel.userSettingsController.updateSdCardPath(it.toString())
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
                            onClick = { viewModel.checkForUpdates(context) }
                        ) {
                            Text(stringResource(R.string.check_for_updates))
                        }
                    }
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
                        val additionalInfo =
                            stringResource(id = it.contributionLevel.stringResource)
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

    val updateState = viewModel.updateState
    if (updateState is UpdateState.UpdateAvailable) UpdateAvailableDialog(
        onDismiss = viewModel::dismissUpdate,
        onDownloadRequest = { uriHandler.openUri(updateState.release.htmlURL) },
        latestVersion = updateState.release.tagName,
        currentVersion = version,
        changelog = updateState.release.changelog ?: ""
    )
}