package pl.lambada.songsync.ui.screens.about.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import pl.lambada.songsync.R
import pl.lambada.songsync.ui.components.AboutItem
import pl.lambada.songsync.ui.components.SwitchItem


@Composable
fun MarqueeSwitch(selected: Boolean, onToggle: (Boolean) -> Unit) {
    AboutItem(label = stringResource(R.string.disable_marquee)) {
        SwitchItem(
            label = stringResource(R.string.disable_marquee_summary),
            selected = selected,
            onClick = { onToggle(!selected) }
        )
    }
}