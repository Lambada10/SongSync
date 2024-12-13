package pl.lambada.songsync.ui.screens.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.lambada.songsync.ui.components.SettingsItem
import pl.lambada.songsync.R

@Composable
fun TranslationSection(uriHandler: UriHandler) {
    SettingsItem(label = stringResource(id = R.string.translation)) {
        Column(
            modifier = Modifier
                .clickable { uriHandler.openUri("https://hosted.weblate.org/engage/songsync/") }
                .padding(horizontal = 22.dp, vertical = 16.dp)
        ) {
            Text(stringResource(id = R.string.help_us_translate))
            Text(
                text = stringResource(id = R.string.translation_website),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
        }
    }
}