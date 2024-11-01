package pl.lambada.songsync.ui.screens.settings.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.lambada.songsync.R
import pl.lambada.songsync.ui.components.SettingsItem

@Composable
fun AppInfoSection(version: String, onCheckForUpdates: () -> Unit) {
    SettingsItem(
        label = stringResource(R.string.about_songsync),
        modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        Text(stringResource(R.string.what_is_songsync))
        Text(stringResource(R.string.extra_what_is_songsync))
        Text("")
        Text(stringResource(R.string.app_version, version))
        Row {
            Spacer(modifier = Modifier.weight(1f))
            Button(
                modifier = Modifier.padding(top = 8.dp),
                onClick = onCheckForUpdates
            ) {
                Text(stringResource(R.string.check_for_updates))
            }
        }
    }
}