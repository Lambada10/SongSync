package pl.lambada.songsync.ui.screens.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import pl.lambada.songsync.R
import pl.lambada.songsync.ui.components.SettingsItem
import pl.lambada.songsync.ui.components.SwitchItem

@Composable
fun ShowPathSwitch(selected: Boolean, onToggle: (Boolean) -> Unit) {
    SettingsItem(label = stringResource(R.string.song_path)) {
        SwitchItem(
            label = stringResource(R.string.song_path_description),
            selected = selected,
            onClick = { onToggle(!selected) }
        )
    }
}