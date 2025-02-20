package pl.lambada.songsync.ui.screens.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import pl.lambada.songsync.R
import pl.lambada.songsync.ui.components.SwitchItem

@Composable
fun PureBlackThemeSwitch(selected: Boolean, onToggle: (Boolean) -> Unit) {
    SwitchItem(
        label = stringResource(R.string.pure_black_theme),
        description = stringResource(R.string.pure_black_theme_summary),
        selected = selected,
        onClick = { onToggle(!selected) }
    )
}