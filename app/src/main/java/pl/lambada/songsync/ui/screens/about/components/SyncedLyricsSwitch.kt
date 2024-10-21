package pl.lambada.songsync.ui.screens.about.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import pl.lambada.songsync.R
import pl.lambada.songsync.ui.components.AboutItem
import pl.lambada.songsync.ui.components.SwitchItem

@Composable
fun SyncedLyricsSwitch(selected: Boolean, onToggle: (Boolean) -> Unit) {
    AboutItem(label = "Get Synced Lyrics") {
        SwitchItem(
            label = "Toggle to switch between synced and unsynced lyrics from Musixmatch.",
            selected = selected,
            onClick = { onToggle(!selected) }
        )
    }
}