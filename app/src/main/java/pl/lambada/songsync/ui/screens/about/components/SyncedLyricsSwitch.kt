package pl.lambada.songsync.ui.screens.about.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import pl.lambada.songsync.R
import pl.lambada.songsync.ui.components.AboutItem
import pl.lambada.songsync.ui.components.SwitchItem

@Composable
fun SyncedLyricsSwitch(selected: Boolean, onToggle: (Boolean) -> Unit) {
    AboutItem(label = stringResource(id = R.string.synced_lyrics)) {
        SwitchItem(
            label = stringResource(id = R.string.synced_lyrics_summary),
            selected = selected,
            onClick = { onToggle(!selected) }
        )
    }
}