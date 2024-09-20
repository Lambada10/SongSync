package pl.lambada.songsync.ui.screens.about.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
fun ContributorsSection(uriHandler: UriHandler) {
    AboutItem(stringResource(R.string.contributors)) {
        Contributor.entries.forEach {
            val additionalInfo = stringResource(id = it.contributionLevel.stringResource)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { it.github?.let { it1 -> uriHandler.openUri(it1) } }
                    .padding(horizontal = 22.dp, vertical = 16.dp)
            ) {
                Text(text = it.devName)
                Text(
                    text = additionalInfo,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 12.sp
                )
            }
        }
    }
}