@file:Suppress("SpellCheckingInspection")

package pl.lambada.songsync.ui.screens.about

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
import pl.lambada.songsync.ui.screens.about.components.AboutScreenTopBar
import pl.lambada.songsync.ui.screens.about.components.AppInfoSection
import pl.lambada.songsync.ui.screens.about.components.ContributorsSection
import pl.lambada.songsync.ui.screens.about.components.CreditsSection
import pl.lambada.songsync.ui.screens.about.components.ExternalLinkSection
import pl.lambada.songsync.ui.screens.about.components.MarqueeSwitch
import pl.lambada.songsync.ui.screens.about.components.PureBlackThemeSwitch
import pl.lambada.songsync.ui.screens.about.components.SdCardPathSetting
import pl.lambada.songsync.ui.screens.about.components.SupportSection
import pl.lambada.songsync.ui.screens.about.components.TranslationSwitch
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
    val version = context.getVersion()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AboutScreenTopBar(
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
                    selected = viewModel.userSettingsController.pureBlack,
                    onToggle = { viewModel.userSettingsController.updatePureBlack(it) }
                )
            }

            item {
                MarqueeSwitch(
                    selected = viewModel.userSettingsController.disableMarquee,
                    onToggle = { viewModel.userSettingsController.updateDisableMarquee(it) }
                )
            }

            item {
                TranslationSwitch(
                    selected = viewModel.userSettingsController.includeTranslation,
                    onToggle = { viewModel.userSettingsController.updateIncludeTranslation(it) }
                )
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                item {
                    SdCardPathSetting(
                        sdPath = viewModel.userSettingsController.sdCardPath,
                        onClearPath = { viewModel.userSettingsController.updateSdCardPath("") },
                        onUpdatePath = { newPath ->
                            viewModel.userSettingsController.updateSdCardPath(
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