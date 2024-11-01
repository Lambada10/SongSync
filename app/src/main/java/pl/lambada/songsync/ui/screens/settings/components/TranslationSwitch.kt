package pl.lambada.songsync.ui.screens.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import pl.lambada.songsync.R
import pl.lambada.songsync.ui.components.SettingsItem
import pl.lambada.songsync.ui.components.SwitchItem


@Composable
fun TranslationSwitch(selected: Boolean, onToggle: (Boolean) -> Unit) {
    SettingsItem(label = stringResource(id = R.string.include_translation)) {
        SwitchItem(
            label = stringResource(id = R.string.include_translation_summary),
            selected = selected,
            onClick = { onToggle(!selected) }
        )
    }
}