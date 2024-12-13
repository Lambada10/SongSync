package pl.lambada.songsync.ui.screens.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import pl.lambada.songsync.R
import pl.lambada.songsync.ui.components.SettingsItem
import pl.lambada.songsync.ui.components.SwitchItem

@Composable
fun OffsetModeSwitch(
    selected: Boolean,
    onToggle: (Boolean) -> Unit
) {
    SwitchItem(
        label = stringResource(R.string.offset_mode),
        description = stringResource(R.string.offset_mode_summary),
        selected = selected,
        onClick = { onToggle(!selected) }
    )
}