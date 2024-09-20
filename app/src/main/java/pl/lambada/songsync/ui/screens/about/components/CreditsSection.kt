package pl.lambada.songsync.ui.screens.about.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.lambada.songsync.R
import pl.lambada.songsync.ui.components.AboutItem


@Composable
fun CreditsSection(uriHandler: UriHandler) {
    AboutItem(label = stringResource(id = R.string.thanks_to)) {
        val credits = mapOf(
            stringResource(R.string.spotify_api) to "https://developer.spotify.com/documentation/web-api",
            stringResource(R.string.spotifylyrics_api) to "https://github.com/akashrchandran/spotify-lyrics-api",
            stringResource(R.string.syncedlyrics_py) to "https://github.com/0x7d4/syncedlyrics",
            stringResource(R.string.statusbar_lyrics_ext) to "https://github.com/cjybyjk/StatusBarLyricExt"
        )
        credits.forEach { credit ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { uriHandler.openUri(credit.value) }
                    .padding(22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = stringResource(id = R.string.open_website)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = credit.key)
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}