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
import pl.lambada.songsync.R
import pl.lambada.songsync.ui.components.SettingsHeadLabel


@Composable
fun ExternalLinkSection(url: String, uriHandler: UriHandler) {
    Column(
        modifier = Modifier
            .clickable { uriHandler.openUri(url) }
            .padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        Text(stringResource(R.string.we_are_open_source))
        Text(
            text = stringResource(R.string.view_on_github),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )
    }
}