package pl.lambada.songsync.ui.screens.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.lambada.songsync.R
import pl.lambada.songsync.ui.components.SettingsItem


@Composable
fun ExternalLinkSection(label: String, description: String, url: String, uriHandler: UriHandler) {
    SettingsItem(
        label,
        modifier = Modifier
            .clickable { uriHandler.openUri(url) }
            .padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        Text(stringResource(R.string.we_are_open_source))
        Text(
            text = description,
            color = MaterialTheme.colorScheme.outline,
            fontSize = 12.sp
        )
    }
}