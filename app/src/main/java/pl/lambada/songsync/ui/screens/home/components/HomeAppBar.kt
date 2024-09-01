package pl.lambada.songsync.ui.screens.home.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.lambada.songsync.R
import pl.lambada.songsync.ui.screens.Providers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(
    showing: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
    selectedProvider: Providers,
    onSelectedClearAction: () -> Unit,
    onNavigateToAboutSectionRequest: () -> Unit,
    onProviderSelectRequest: (Providers) -> Unit,
    onBatchDownloadRequest: () -> Unit,
    onSelectAllSongsRequest: () -> Unit,
    onInvertSongSelectionRequest: () -> Unit,
    embedLyrics: Boolean,
    onEmbedLyricsChangeRequest: (Boolean) -> Unit,
    cachedSize: Int,
) {
    MediumTopAppBar(
        navigationIcon = {
            if (showing) {
                IconButton(onClick = onSelectedClearAction) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        },
        title = {
            if (showing) {
                Crossfade(
                    targetState = cachedSize,
                    label = ""
                ) { size ->
                    Text(
                        modifier = Modifier.padding(start = 6.dp),
                        text = stringResource(id = R.string.selected_count, size)
                    )
                }
            } else {
                Text(
                    modifier = Modifier.padding(start = 6.dp),
                    text = stringResource(R.string.app_name)
                )
            }
        },
        actions = {
            if (showing) {
                IconButton(
                    onClick = {
                        onSelectedClearAction()
                        onSelectAllSongsRequest()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.SelectAll,
                        contentDescription = stringResource(R.string.select_all)
                    )
                }
                IconButton(onClick = onInvertSongSelectionRequest) {
                    Icon(
                        imageVector = Icons.Filled.Deselect,
                        contentDescription = stringResource(
                            id = R.string.invert_selection
                        )
                    )
                }
                var expanded by remember { mutableStateOf(false) }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More"
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.batch_download_lyrics),
                                modifier = Modifier.padding(horizontal = 6.dp),
                            )
                        },
                        onClick = {
                            onBatchDownloadRequest()
                            expanded = false
                        }
                    )
                }
            } else {
                HomeTopAppBarDropDown(
                    onNavigateToAboutSectionRequest = onNavigateToAboutSectionRequest,
                    selectedProvider = selectedProvider,
                    onProviderSelectRequest = onProviderSelectRequest,
                    onBatchDownloadRequest = onBatchDownloadRequest,
                    embedLyrics = embedLyrics,
                    onEmbedLyricsChangeRequest = onEmbedLyricsChangeRequest
                )
            }
        },
        colors = if (showing) {
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        } else {
            TopAppBarDefaults.topAppBarColors()
        },
        scrollBehavior = scrollBehavior,
    )
}