package pl.lambada.songsync.ui.screens.home.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import pl.lambada.songsync.R
import pl.lambada.songsync.util.Providers
import pl.lambada.songsync.util.dataStore
import pl.lambada.songsync.util.set

@Composable
fun HomeTopAppBarDropDown(
    onNavigateToSettingsSectionRequest: () -> Unit,
    selectedProvider: Providers,
    onProviderSelectRequest: (Providers) -> Unit,
    embedLyrics: Boolean,
    onEmbedLyricsChangeRequest: (Boolean) -> Unit,
    onBatchDownloadRequest: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var expandedProviders by remember { mutableStateOf(false) }
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
                    text = stringResource(R.string.provider),
                    modifier = Modifier.padding(horizontal = 6.dp),
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                    contentDescription = "expand dropdown"
                )
            },
            onClick = {
                expanded = false
                expandedProviders = true
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(id = R.string.embed_lyrics_in_file),
                    modifier = Modifier.padding(horizontal = 6.dp),
                )
            },
            trailingIcon = {
                Checkbox(
                    checked = embedLyrics,
                    onCheckedChange = onEmbedLyricsChangeRequest
                )
            },
            onClick = { onEmbedLyricsChangeRequest(!embedLyrics) }
        )
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
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(id = R.string.settings),
                    modifier = Modifier.padding(horizontal = 6.dp),
                )
            },
            onClick = {
                onNavigateToSettingsSectionRequest()
                expanded = false
            }
        )
    }
    val providers = Providers.entries.toTypedArray()
    val dataStore = LocalContext.current.dataStore
    DropdownMenu(
        expanded = expandedProviders,
        onDismissRequest = { expandedProviders = false }
    ) {
        Text(
            text = stringResource(id = R.string.provider),
            modifier = Modifier.padding(start = 18.dp, top = 8.dp),
            fontSize = 12.sp
        )
        providers.forEach {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = it.displayName,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 6.dp)
                        )
                        RadioButton(
                            selected = selectedProvider == it,
                            onClick = {
                                onProviderSelectRequest(it)
                                dataStore.set(
                                    stringPreferencesKey("provider"),
                                    it.displayName
                                )
                                expandedProviders = false
                            }
                        )
                    }
                },
                onClick = {
                    onProviderSelectRequest(it)
                    dataStore.set(
                        stringPreferencesKey("provider"),
                        it.displayName
                    )
                    expandedProviders = false
                }
            )
        }
    }
}