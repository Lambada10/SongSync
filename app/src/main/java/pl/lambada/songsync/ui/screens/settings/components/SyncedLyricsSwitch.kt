package pl.lambada.songsync.ui.screens.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import pl.lambada.songsync.R
import pl.lambada.songsync.ui.components.SettingsItem
import pl.lambada.songsync.ui.components.SwitchItem

@Composable
fun SyncedLyricsSwitch(selected: Boolean, onToggle: (Boolean) -> Unit) {
    SwitchItem(
        label = stringResource(id = R.string.synced_lyrics),
        description = stringResource(id = R.string.synced_lyrics_summary),
        selected = selected,
        onClick = { onToggle(!selected) }
    )
}