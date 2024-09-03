package pl.lambada.songsync.ui.screens.about.components

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
import pl.lambada.songsync.ui.components.AboutItem


@Composable
fun SupportSection(uriHandler: UriHandler) {
    AboutItem(
        stringResource(R.string.support),
    ) {
        Column(
            modifier = Modifier
                .clickable { uriHandler.openUri("https://t.me/LambadaOT") }
                .padding(horizontal = 22.dp, vertical = 16.dp)
        ) {
            Text(stringResource(R.string.bugs_or_suggestions_contact_us))
            Text(
                text = stringResource(R.string.telegram_group),
                color = MaterialTheme.colorScheme.outline,
                fontSize = 12.sp
            )
        }
        Text(
            stringResource(R.string.create_issue),
            modifier = Modifier.padding(horizontal = 22.dp)
        )
    }
}