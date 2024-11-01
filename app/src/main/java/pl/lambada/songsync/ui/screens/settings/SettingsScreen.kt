@file:Suppress("SpellCheckingInspection")

package pl.lambada.songsync.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import pl.lambada.songsync.R
import pl.lambada.songsync.data.remote.UpdateState
import pl.lambada.songsync.data.remote.UserSettingsController
import pl.lambada.songsync.ui.screens.settings.components.SettingsScreenTopBar
import pl.lambada.songsync.ui.screens.settings.components.AppInfoSection
import pl.lambada.songsync.ui.screens.settings.components.ContributorsSection
import pl.lambada.songsync.ui.screens.settings.components.CreditsSection
import pl.lambada.songsync.ui.screens.settings.components.ExternalLinkSection
import pl.lambada.songsync.ui.screens.settings.components.MarqueeSwitch
import pl.lambada.songsync.ui.screens.settings.components.MultiPersonSwitch
import pl.lambada.songsync.ui.screens.settings.components.PureBlackThemeSwitch
import pl.lambada.songsync.ui.screens.settings.components.SdCardPathSetting
import pl.lambada.songsync.ui.screens.settings.components.ShowPathSwitch
import pl.lambada.songsync.ui.screens.settings.components.SupportSection
import pl.lambada.songsync.ui.screens.settings.components.SyncedLyricsSwitch
import pl.lambada.songsync.ui.screens.settings.components.TranslationSwitch
import pl.lambada.songsync.ui.screens.settings.components.UpdateAvailableDialog
import pl.lambada.songsync.util.ext.getVersion

/**
 * Composable function for AboutScreen component.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    userSettingsController: UserSettingsController,
    navController: NavController
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val version = context.getVersion()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SettingsScreenTopBar(
                navController = navController,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = paddingValues
        ) {
            item {
                if (isSystemInDarkTheme()) PureBlackThemeSwitch(
                    selected = userSettingsController.pureBlack,
                    onToggle = { userSettingsController.updatePureBlack(it) }
                )
            }

            item {
                MarqueeSwitch(
                    selected = userSettingsController.disableMarquee,
                    onToggle = { userSettingsController.updateDisableMarquee(it) }
                )
            }

            item {
                ShowPathSwitch(
                    selected = userSettingsController.showPath,
                    onToggle = { userSettingsController.updateShowPath(it) }
                )
            }

            item {
                TranslationSwitch(
                    selected = userSettingsController.includeTranslation,
                    onToggle = { userSettingsController.updateIncludeTranslation(it) }
                )
            }

            item {
                MultiPersonSwitch(
                    selected = userSettingsController.multiPersonWordByWord,
                    onToggle = { userSettingsController.updateMultiPersonWordByWord(it) }
                )
            }

            item {
                SyncedLyricsSwitch(
                    selected = userSettingsController.syncedMusixmatch,
                    onToggle = { userSettingsController.updateSyncedMusixmatch(it) }
                )
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                item {
                    SdCardPathSetting(
                        sdPath = userSettingsController.sdCardPath,
                        onClearPath = { userSettingsController.updateSdCardPath("") },
                        onUpdatePath = { newPath ->
                            userSettingsController.updateSdCardPath(
                                newPath
                            )
                        }
                    )
                }
            }

            item {
                AppInfoSection(
                    version = version,
                    onCheckForUpdates = { viewModel.checkForUpdates(context) }
                )
            }

            item {
                ExternalLinkSection(
                    label = stringResource(R.string.source_code),
                    description = stringResource(R.string.view_on_github),
                    url = "https://github.com/Lambada10/SongSync",
                    uriHandler = uriHandler
                )
            }

            item { SupportSection(uriHandler = uriHandler) }

            item { ContributorsSection(uriHandler = uriHandler) }

            item { CreditsSection(uriHandler = uriHandler) }
        }
    }

    val updateState = viewModel.updateState
    if (updateState is UpdateState.UpdateAvailable) {
        UpdateAvailableDialog(
            onDismiss = viewModel::dismissUpdate,
            onDownloadRequest = { uriHandler.openUri(updateState.release.htmlURL) },
            latestVersion = updateState.release.tagName,
            currentVersion = version,
            changelog = updateState.release.changelog ?: ""
        )
    }
}